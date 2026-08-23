package com.example.data.model

data class MarketIndex(
    val symbol: String,
    val name: String,
    val description: String,
    val tickDecimals: Int = 2,
    val defaultGrowthRate: Double = 0.03, // 3%
    val volatilityLevel: String = "Medium",
    val isPopular: Boolean = false
) {
    companion object {
        val ALL_INDICES = listOf(
            MarketIndex("1HZ100V", "Volatility 100 (1s) Index", "High frequency 1-second ticks with active volatility swings", 2, 0.03, "High", true),
            MarketIndex("1HZ75V", "Volatility 75 (1s) Index", "Fast-paced 1s synthetic index with balanced price velocity", 2, 0.02, "High", true),
            MarketIndex("1HZ50V", "Volatility 50 (1s) Index", "Moderate 1s synthetic index ideal for consistent accumulation", 2, 0.02, "Medium", true),
            MarketIndex("1HZ25V", "Volatility 25 (1s) Index", "Controlled volatility 1s index for high barrier safety", 2, 0.01, "Low", false),
            MarketIndex("1HZ10V", "Volatility 10 (1s) Index", "Ultra smooth 1s index with minimum knockout spikes", 2, 0.01, "Very Low", false),
            MarketIndex("R_100", "Volatility 100 Index", "Standard continuous synthetic index with high variance", 2, 0.04, "High", false),
            MarketIndex("R_75", "Volatility 75 Index", "Standard continuous synthetic index with standard drift", 2, 0.03, "Medium", false),
            MarketIndex("R_50", "Volatility 50 Index", "Standard continuous synthetic index with stable channels", 2, 0.02, "Medium", false),
            MarketIndex("R_25", "Volatility 25 Index", "Gentle trends with wide accumulator safety corridors", 2, 0.01, "Low", false),
            MarketIndex("R_10", "Volatility 10 Index", "Maximum barrier width preservation for low risk runs", 2, 0.01, "Very Low", false),
            MarketIndex("CRASH300", "Crash 300 Index", "Accumulate in calm phases; exit before drop events", 2, 0.05, "Extreme", false),
            MarketIndex("BOOM500", "Boom 500 Index", "Accumulate in downward drift; take fast profit spikes", 2, 0.05, "Extreme", false),
            MarketIndex("STP", "Step Index", "Equiprobable discrete steps with predictable range boundaries", 2, 0.02, "Medium", false)
        )

        fun findBySymbol(symbol: String): MarketIndex {
            return ALL_INDICES.find { it.symbol == symbol } ?: ALL_INDICES.first()
        }
    }
}

data class TickPoint(
    val timestamp: Long,
    val price: Double,
    val epoch: Long = timestamp / 1000,
    val isUp: Boolean = true
)

enum class SignalType(val label: String, val badgeColorHex: Long) {
    RANGE_STABILITY("Range Stability", 0xFF00E5FF),
    VOLATILITY_SQUEEZE("Volatility Squeeze", 0xFF00F090),
    MOMENTUM_PULSE("Momentum Pulse", 0xFFFFB300),
    CRASH_DEFENSE("Crash Defense", 0xFFFF2A55)
}

enum class SignalStatus {
    ACTIVE,
    TRIGGERED,
    SUCCESS_COMPLETED,
    KNOCKED_OUT,
    EXPIRED
}

data class AccumulatorSignal(
    val id: String,
    val symbol: String,
    val symbolName: String,
    val growthRate: Double, // 0.01, 0.02, 0.03, 0.04, 0.05
    val confidenceScore: Int, // 0 - 100
    val barrierSafetyPercent: Double, // e.g. 96.5%
    val targetTicks: Int, // e.g. 7 ticks
    val estimatedPayoutMultiplier: Double, // (1 + growthRate)^targetTicks
    val signalType: SignalType,
    val strategyName: String,
    val indicatorRationale: String,
    val entryPrice: Double,
    val timestamp: Long = System.currentTimeMillis(),
    val status: SignalStatus = SignalStatus.ACTIVE
)

enum class TradeStatus {
    PENDING,
    RUNNING,
    CASHED_OUT,
    KNOCKED_OUT,
    FAILED
}

data class LiveAccumulatorContract(
    val contractId: String,
    val symbol: String,
    val symbolName: String,
    val stake: Double,
    val growthRate: Double,
    val entrySpot: Double,
    val currentSpot: Double,
    val currentTicks: Int = 0,
    val targetTicks: Int = 8,
    val currentProfit: Double = 0.0,
    val currentPayout: Double = stake,
    val multiplier: Double = 1.0,
    val upperBarrier: Double = 0.0,
    val lowerBarrier: Double = 0.0,
    val status: TradeStatus = TradeStatus.RUNNING,
    val isBot: Boolean = false,
    val startTime: Long = System.currentTimeMillis(),
    val logHistory: List<String> = emptyList()
)

data class BotSettings(
    val isAutoTrading: Boolean = false,
    val activeSymbols: List<String> = listOf("1HZ100V", "1HZ75V", "1HZ50V"),
    val growthRate: Double = 0.03,
    val baseStake: Double = 5.0,
    val targetTicks: Int = 8,
    val minConfidence: Int = 85,
    val maxConsecutiveLosses: Int = 3,
    val dailyStopLoss: Double = 50.0,
    val dailyTakeProfit: Double = 100.0,
    val martingaleMultiplier: Double = 1.5,
    val isMartingaleEnabled: Boolean = false,
    val activeStrategy: String = "Demon Pulse Scalper"
)

data class DerivAccount(
    val apiToken: String = "",
    val isDemo: Boolean = true,
    val balance: Double = 10000.0,
    val currency: String = "USD",
    val isConnected: Boolean = true,
    val latencyMs: Long = 42,
    val pingStatus: String = "Online (Deriv WS)",
    val totalTicksProcessed: Long = 0,
    val accountId: String = "CR-DEMO99482"
)
