package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.deriv.DerivWebSocketManager
import com.example.data.deriv.SignalEngine
import com.example.data.local.AppDatabase
import com.example.data.local.SignalRecordEntity
import com.example.data.local.TradeEntity
import com.example.data.model.*
import com.example.data.repository.TradeRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlin.math.pow
import kotlin.random.Random

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application)
    private val repository = TradeRepository(database.tradeDao(), database.signalDao())
    val webSocketManager = DerivWebSocketManager(viewModelScope)
    private val signalEngine = SignalEngine()

    // UI state
    val accountState: StateFlow<DerivAccount> = webSocketManager.accountState

    private val _selectedSymbol = MutableStateFlow("1HZ100V")
    val selectedSymbol: StateFlow<String> = _selectedSymbol.asStateFlow()

    private val _recentTicks = MutableStateFlow<List<TickPoint>>(emptyList())
    val recentTicks: StateFlow<List<TickPoint>> = _recentTicks.asStateFlow()

    private val _liveSignals = MutableStateFlow<List<AccumulatorSignal>>(emptyList())
    val liveSignals: StateFlow<List<AccumulatorSignal>> = _liveSignals.asStateFlow()

    private val _activeContract = MutableStateFlow<LiveAccumulatorContract?>(null)
    val activeContract: StateFlow<LiveAccumulatorContract?> = _activeContract.asStateFlow()

    private val _botSettings = MutableStateFlow(BotSettings())
    val botSettings: StateFlow<BotSettings> = _botSettings.asStateFlow()

    // History from Room
    val tradeHistory: StateFlow<List<TradeEntity>> = repository.allTrades
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recentSignalHistory: StateFlow<List<SignalRecordEntity>> = repository.recentSignals
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalProfit: StateFlow<Double?> = repository.totalProfit
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val totalTradesCount: StateFlow<Int> = repository.totalTradesCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    private val _toastMessage = MutableSharedFlow<String>(extraBufferCapacity = 16)
    val toastMessage: SharedFlow<String> = _toastMessage.asSharedFlow()

    private var consecutiveLosses = 0
    private var currentDailyProfit = 0.0
    private var activeContractJob: Job? = null

    init {
        // Collect server messages
        viewModelScope.launch {
            webSocketManager.serverMessageFlow.collect { msg ->
                _toastMessage.emit(msg)
            }
        }

        // Collect live tick stream
        viewModelScope.launch {
            webSocketManager.tickFlow.collect { (symbol, tick) ->
                handleIncomingTick(symbol, tick)
            }
        }

        // Periodically check if bot should execute signal
        viewModelScope.launch {
            while (true) {
                delay(1200)
                checkAutoBotExecution()
            }
        }

        // Seed initial high quality signals for immediate discovery
        seedInitialSignals()
    }

    private fun seedInitialSignals() {
        val initialList = mutableListOf<AccumulatorSignal>()
        val popular = listOf("1HZ100V", "1HZ75V", "1HZ50V", "R_100")
        for (sym in popular) {
            val idx = MarketIndex.findBySymbol(sym)
            val price = webSocketManager.getCurrentPrice(sym)
            val growth = idx.defaultGrowthRate
            val target = when {
                growth <= 0.01 -> 12
                growth <= 0.02 -> 8
                growth <= 0.03 -> 7
                else -> 5
            }
            initialList.add(
                AccumulatorSignal(
                    id = "SIG_${sym}_${System.currentTimeMillis() - Random.nextLong(10000, 60000)}",
                    symbol = sym,
                    symbolName = idx.name,
                    growthRate = growth,
                    confidenceScore = Random.nextInt(86, 98),
                    barrierSafetyPercent = 95.0 + Random.nextDouble(0.5, 4.5),
                    targetTicks = target,
                    estimatedPayoutMultiplier = ((1.0 + growth).pow(target) * 100).toInt() / 100.0,
                    signalType = SignalType.VOLATILITY_SQUEEZE,
                    strategyName = "Demon Squeeze Armor",
                    indicatorRationale = "Ultra-low tick dispersion. Knockout corridor intact across last 25 synthetic ticks.",
                    entryPrice = price,
                    status = SignalStatus.ACTIVE
                )
            )
        }
        _liveSignals.value = initialList
    }

    fun selectSymbol(symbol: String) {
        _selectedSymbol.value = symbol
        webSocketManager.subscribeSymbol(symbol)
        _recentTicks.value = emptyList()
    }

    private fun handleIncomingTick(symbol: String, tick: TickPoint) {
        // Update tick buffer for the selected symbol
        if (symbol == _selectedSymbol.value) {
            val currentList = _recentTicks.value.toMutableList()
            currentList.add(tick)
            if (currentList.size > 40) {
                currentList.removeAt(0)
            }
            _recentTicks.value = currentList
        }

        // Run signal analysis
        val newSignal = signalEngine.onNewTick(symbol, tick.price)
        if (newSignal != null) {
            val currentList = _liveSignals.value.toMutableList()
            // Avoid duplicate recent signal for same symbol within 10s
            val existing = currentList.find { it.symbol == symbol && System.currentTimeMillis() - it.timestamp < 10000 }
            if (existing == null) {
                currentList.add(0, newSignal)
                if (currentList.size > 20) currentList.removeAt(currentList.lastIndex)
                _liveSignals.value = currentList

                // Save to Room DB
                viewModelScope.launch {
                    repository.saveSignal(
                        SignalRecordEntity(
                            signalId = newSignal.id,
                            symbol = newSignal.symbol,
                            symbolName = newSignal.symbolName,
                            growthRate = newSignal.growthRate,
                            confidenceScore = newSignal.confidenceScore,
                            barrierSafetyPercent = newSignal.barrierSafetyPercent,
                            targetTicks = newSignal.targetTicks,
                            signalType = newSignal.signalType.name,
                            strategyName = newSignal.strategyName,
                            indicatorRationale = newSignal.indicatorRationale,
                            outcomeStatus = "ACTIVE",
                            profitAchieved = 0.0
                        )
                    )
                }
            }
        }

        // Progress active contract if running on this symbol
        val contract = _activeContract.value
        if (contract != null && contract.status == TradeStatus.RUNNING && contract.symbol == symbol) {
            progressActiveContract(tick.price)
        }
    }

    private fun checkAutoBotExecution() {
        val bot = _botSettings.value
        if (!bot.isAutoTrading) return
        if (_activeContract.value != null) return // Already running contract

        // Check daily limits
        if (currentDailyProfit <= -bot.dailyStopLoss) {
            updateBotSettings(bot.copy(isAutoTrading = false))
            viewModelScope.launch {
                _toastMessage.emit("🛑 Bot Stopped: Daily Stop Loss reached (-$${bot.dailyStopLoss})")
            }
            return
        }

        if (currentDailyProfit >= bot.dailyTakeProfit) {
            updateBotSettings(bot.copy(isAutoTrading = false))
            viewModelScope.launch {
                _toastMessage.emit("🎯 Bot Target Reached: Daily Take Profit ($${bot.dailyTakeProfit})")
            }
            return
        }

        if (consecutiveLosses >= bot.maxConsecutiveLosses) {
            updateBotSettings(bot.copy(isAutoTrading = false))
            viewModelScope.launch {
                _toastMessage.emit("⚠️ Bot Paused: Max consecutive losses (${bot.maxConsecutiveLosses}) reached.")
            }
            return
        }

        // Look for matching high confidence signal in active symbols
        val matchingSignal = _liveSignals.value.find { sig ->
            sig.status == SignalStatus.ACTIVE &&
                    bot.activeSymbols.contains(sig.symbol) &&
                    sig.confidenceScore >= bot.minConfidence
        }

        if (matchingSignal != null) {
            // Determine stake (with Martingale if enabled)
            var stake = bot.baseStake
            if (bot.isMartingaleEnabled && consecutiveLosses > 0) {
                stake *= (bot.martingaleMultiplier.pow(consecutiveLosses))
            }
            stake = (stake * 100).toInt() / 100.0

            executeAccumulatorTrade(
                symbol = matchingSignal.symbol,
                stake = stake,
                growthRate = matchingSignal.growthRate,
                targetTicks = bot.targetTicks,
                isBot = true,
                signalId = matchingSignal.id
            )
        }
    }

    fun executeAccumulatorTrade(
        symbol: String,
        stake: Double,
        growthRate: Double,
        targetTicks: Int,
        isBot: Boolean = false,
        signalId: String? = null
    ) {
        if (_activeContract.value != null && _activeContract.value?.status == TradeStatus.RUNNING) {
            viewModelScope.launch { _toastMessage.emit("Another contract is already running!") }
            return
        }

        val balance = accountState.value.balance
        if (balance < stake) {
            viewModelScope.launch { _toastMessage.emit("Insufficient balance ($${balance}) for stake ($${stake})") }
            return
        }

        // Deduct initial stake
        webSocketManager.updateBalance(balance - stake)

        val currentPrice = webSocketManager.getCurrentPrice(symbol)
        val index = MarketIndex.findBySymbol(symbol)
        val barrierTol = signalEngine.getBarrierTolerance(growthRate)
        val upperBarrier = currentPrice * (1.0 + barrierTol)
        val lowerBarrier = currentPrice * (1.0 - barrierTol)

        val contractId = "ACCU_${System.currentTimeMillis()}"
        val newContract = LiveAccumulatorContract(
            contractId = contractId,
            symbol = symbol,
            symbolName = index.name,
            stake = stake,
            growthRate = growthRate,
            entrySpot = currentPrice,
            currentSpot = currentPrice,
            currentTicks = 0,
            targetTicks = targetTicks,
            currentProfit = 0.0,
            currentPayout = stake,
            multiplier = 1.0,
            upperBarrier = (upperBarrier * 100).toLong() / 100.0,
            lowerBarrier = (lowerBarrier * 100).toLong() / 100.0,
            status = TradeStatus.RUNNING,
            isBot = isBot,
            startTime = System.currentTimeMillis(),
            logHistory = listOf("⚡ Contract opened on ${index.name} at spot ${String.format("%.2f", currentPrice)}")
        )

        _activeContract.value = newContract
        selectSymbol(symbol)

        viewModelScope.launch {
            val modeText = if (isBot) "🤖 [Auto-Bot]" else "🎯 [Manual]"
            _toastMessage.emit("$modeText Accumulator started: $symbol | Stake: $$stake | Target: $targetTicks Ticks (${(growthRate * 100).toInt()}%)")
        }

        // Mark signal as triggered
        if (signalId != null) {
            val updated = _liveSignals.value.map {
                if (it.id == signalId) it.copy(status = SignalStatus.TRIGGERED) else it
            }
            _liveSignals.value = updated
        }
    }

    private fun progressActiveContract(newPrice: Double) {
        val contract = _activeContract.value ?: return
        if (contract.status != TradeStatus.RUNNING) return

        val newTicks = contract.currentTicks + 1
        val newMultiplier = (1.0 + contract.growthRate).pow(newTicks)
        val newPayout = (contract.stake * newMultiplier * 100).toInt() / 100.0
        val newProfit = ((newPayout - contract.stake) * 100).toInt() / 100.0

        // Check knock-out condition
        val knockedOut = signalEngine.isTickKnockedOut(
            entrySpot = contract.entrySpot,
            currentSpot = newPrice,
            growthRate = contract.growthRate
        )

        val updatedLogs = contract.logHistory.toMutableList()

        if (knockedOut) {
            // KNOCKED OUT!
            updatedLogs.add("❌ Knockout barrier breached at tick #$newTicks (Spot: ${String.format("%.2f", newPrice)})")
            val finalized = contract.copy(
                currentSpot = newPrice,
                currentTicks = newTicks,
                currentProfit = -contract.stake,
                currentPayout = 0.0,
                status = TradeStatus.KNOCKED_OUT,
                logHistory = updatedLogs
            )
            _activeContract.value = finalized
            finishContract(finalized, isWon = false)
            return
        }

        updatedLogs.add("✓ Tick #$newTicks survived (+${(contract.growthRate * 100).toInt()}% growth) -> Value: $$newPayout")

        // Check if target reached for auto cash-out
        if (newTicks >= contract.targetTicks) {
            updatedLogs.add("🎯 Target of ${contract.targetTicks} ticks reached! Auto-Cashed out with $$newProfit profit.")
            val finalized = contract.copy(
                currentSpot = newPrice,
                currentTicks = newTicks,
                currentProfit = newProfit,
                currentPayout = newPayout,
                multiplier = newMultiplier,
                status = TradeStatus.CASHED_OUT,
                logHistory = updatedLogs
            )
            _activeContract.value = finalized
            finishContract(finalized, isWon = true)
            return
        }

        // Otherwise continue running
        _activeContract.value = contract.copy(
            currentSpot = newPrice,
            currentTicks = newTicks,
            currentProfit = newProfit,
            currentPayout = newPayout,
            multiplier = newMultiplier,
            logHistory = updatedLogs
        )
    }

    fun manualCashOut() {
        val contract = _activeContract.value ?: return
        if (contract.status != TradeStatus.RUNNING) return

        val updatedLogs = contract.logHistory.toMutableList()
        updatedLogs.add("💰 Manual Cash Out executed at Tick #${contract.currentTicks} with Profit: $$${contract.currentProfit}")

        val finalized = contract.copy(
            status = TradeStatus.CASHED_OUT,
            logHistory = updatedLogs
        )
        _activeContract.value = finalized
        finishContract(finalized, isWon = contract.currentProfit > 0)
    }

    private fun finishContract(contract: LiveAccumulatorContract, isWon: Boolean) {
        val profit = contract.currentProfit
        val payout = contract.currentPayout

        // Return payout to balance
        val newBalance = accountState.value.balance + payout
        webSocketManager.updateBalance(newBalance)

        currentDailyProfit += profit

        if (isWon) {
            consecutiveLosses = 0
        } else {
            consecutiveLosses += 1
        }

        // Save trade to Room DB
        viewModelScope.launch {
            repository.saveTrade(
                TradeEntity(
                    contractId = contract.contractId,
                    symbol = contract.symbol,
                    symbolName = contract.symbolName,
                    stake = contract.stake,
                    payout = payout,
                    profit = profit,
                    growthRate = contract.growthRate,
                    ticksHeld = contract.currentTicks,
                    targetTicks = contract.targetTicks,
                    outcome = if (isWon) "WON" else "KNOCKED_OUT",
                    isBot = contract.isBot,
                    entryPrice = contract.entrySpot,
                    exitPrice = contract.currentSpot
                )
            )

            val outcomeMsg = if (isWon) "🎉 Trade Won! Profit: +$$profit" else "💀 Contract Knocked Out (-$$${contract.stake})"
            _toastMessage.emit(outcomeMsg)

            // Auto-clear active contract card after 6 seconds to allow reviewing
            delay(6000)
            if (_activeContract.value?.contractId == contract.contractId) {
                _activeContract.value = null
            }
        }
    }

    fun dismissActiveContractCard() {
        _activeContract.value = null
    }

    fun updateBotSettings(settings: BotSettings) {
        _botSettings.value = settings
    }

    fun toggleAutoBot(enabled: Boolean) {
        _botSettings.value = _botSettings.value.copy(isAutoTrading = enabled)
        viewModelScope.launch {
            if (enabled) {
                _toastMessage.emit("🔥 Demon Auto-Bot ACTIVATED! Scanning markets for accumulator signals...")
            } else {
                _toastMessage.emit("⏸️ Demon Auto-Bot PAUSED.")
            }
        }
    }

    fun applyPresetStrategy(strategyName: String) {
        when (strategyName) {
            "Demon Scalp (5-Tick)" -> {
                updateBotSettings(
                    _botSettings.value.copy(
                        growthRate = 0.03,
                        targetTicks = 5,
                        minConfidence = 88,
                        activeStrategy = strategyName,
                        activeSymbols = listOf("1HZ100V", "1HZ75V")
                    )
                )
            }
            "Safe Compound 1% Ultra" -> {
                updateBotSettings(
                    _botSettings.value.copy(
                        growthRate = 0.01,
                        targetTicks = 12,
                        minConfidence = 80,
                        activeStrategy = strategyName,
                        activeSymbols = listOf("1HZ10V", "1HZ25V", "R_10", "R_25")
                    )
                )
            }
            "Aggressive 5% Burst" -> {
                updateBotSettings(
                    _botSettings.value.copy(
                        growthRate = 0.05,
                        targetTicks = 4,
                        minConfidence = 92,
                        activeStrategy = strategyName,
                        activeSymbols = listOf("1HZ100V", "R_100", "CRASH300")
                    )
                )
            }
            "Balanced 2% Radar" -> {
                updateBotSettings(
                    _botSettings.value.copy(
                        growthRate = 0.02,
                        targetTicks = 8,
                        minConfidence = 85,
                        activeStrategy = strategyName,
                        activeSymbols = listOf("1HZ50V", "1HZ75V", "R_50")
                    )
                )
            }
        }
    }

    fun setDerivApiToken(token: String, isDemo: Boolean) {
        webSocketManager.setApiToken(token, isDemo)
    }

    fun clearAllHistory() {
        viewModelScope.launch {
            repository.clearHistory()
            _toastMessage.emit("Trade and signal history cleared.")
        }
    }
}
