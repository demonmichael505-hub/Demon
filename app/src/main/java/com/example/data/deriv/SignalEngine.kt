package com.example.data.deriv

import com.example.data.model.*
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sqrt
import kotlin.random.Random

class SignalEngine {

    // Rolling tick window per symbol
    private val tickHistoryMap = mutableMapOf<String, ArrayDeque<Double>>()
    private val MAX_WINDOW = 30

    // Barrier width factors based on Accumulator growth rate
    // 1% has wider safe corridor (~0.035%), 5% has tight corridor (~0.007%)
    fun getBarrierTolerance(growthRate: Double): Double {
        return when {
            growthRate <= 0.01 -> 0.032
            growthRate <= 0.02 -> 0.022
            growthRate <= 0.03 -> 0.015
            growthRate <= 0.04 -> 0.010
            else -> 0.007
        }
    }

    fun onNewTick(symbol: String, price: Double): AccumulatorSignal? {
        val window = tickHistoryMap.getOrPut(symbol) { ArrayDeque() }
        window.addLast(price)
        if (window.size > MAX_WINDOW) {
            window.removeFirst()
        }

        if (window.size < 12) {
            return null
        }

        val prices = window.toList()
        val returns = mutableListOf<Double>()
        for (i in 1 until prices.size) {
            val prev = prices[i - 1]
            val curr = prices[i]
            returns.add((curr - prev) / prev)
        }

        val meanReturn = returns.average()
        val variance = returns.map { (it - meanReturn).pow(2) }.average()
        val stdDev = sqrt(variance)

        val index = MarketIndex.findBySymbol(symbol)
        val growthRate = index.defaultGrowthRate
        val barrierTolerance = getBarrierTolerance(growthRate)

        // Safety score: distance of volatility from knock-out barrier
        val barrierSafetyRatio = (barrierTolerance - stdDev * 2).coerceIn(0.001, barrierTolerance) / barrierTolerance
        val barrierSafetyPercent = (barrierSafetyRatio * 100.0).coerceIn(60.0, 99.8)

        // Compute Demon Power Index (DPI)
        val volatilitySqueezeScore = ((1.0 - (stdDev / 0.003)).coerceIn(0.0, 1.0) * 40).toInt()
        val driftStabilityScore = ((1.0 - (abs(meanReturn) / 0.001)).coerceIn(0.0, 1.0) * 30).toInt()
        val consecutiveStableTicks = returns.takeLast(5).count { abs(it) < barrierTolerance * 0.4 }
        val streakBonus = (consecutiveStableTicks * 6).coerceAtMost(30)

        val confidence = (volatilitySqueezeScore + driftStabilityScore + streakBonus).coerceIn(55, 99)

        // Target ticks based on growth rate and safety
        val recommendedTicks = when {
            growthRate <= 0.01 -> (8..15).random()
            growthRate <= 0.02 -> (6..12).random()
            growthRate <= 0.03 -> (5..9).random()
            growthRate <= 0.04 -> (4..7).random()
            else -> (3..6).random()
        }

        val estimatedMultiplier = (1.0 + growthRate).pow(recommendedTicks)

        val signalType = when {
            stdDev < 0.0006 -> SignalType.VOLATILITY_SQUEEZE
            consecutiveStableTicks >= 4 -> SignalType.RANGE_STABILITY
            abs(meanReturn) > 0.0004 -> SignalType.MOMENTUM_PULSE
            else -> SignalType.CRASH_DEFENSE
        }

        val rationale = when (signalType) {
            SignalType.VOLATILITY_SQUEEZE -> "Extreme low-variance squeeze detected (${String.format("%.4f", stdDev)} stdDev). Knockout probability is minimal."
            SignalType.RANGE_STABILITY -> "Micro-channel boundary compression. Last $consecutiveStableTicks ticks locked within 40% barrier corridor."
            SignalType.MOMENTUM_PULSE -> "Smooth directional tick drift with low whipsaw risk. Optimal for fast $recommendedTicks-tick burst."
            SignalType.CRASH_DEFENSE -> "Calm consolidation phase. High safety index: ${String.format("%.1f", barrierSafetyPercent)}%."
        }

        // Only emit high quality signals with confidence >= 80%
        if (confidence >= 80) {
            val signalId = "SIG_${symbol}_${System.currentTimeMillis()}"
            return AccumulatorSignal(
                id = signalId,
                symbol = symbol,
                symbolName = index.name,
                growthRate = growthRate,
                confidenceScore = confidence,
                barrierSafetyPercent = (barrierSafetyPercent * 10).toInt() / 10.0,
                targetTicks = recommendedTicks,
                estimatedPayoutMultiplier = (estimatedMultiplier * 100).toInt() / 100.0,
                signalType = signalType,
                strategyName = "Demon ${signalType.label}",
                indicatorRationale = rationale,
                entryPrice = price
            )
        }

        return null
    }

    /**
     * Check if a tick inside an active contract breaches accumulator knock-out barriers
     */
    fun isTickKnockedOut(
        entrySpot: Double,
        currentSpot: Double,
        growthRate: Double
    ): Boolean {
        val tolerance = getBarrierTolerance(growthRate)
        val delta = abs(currentSpot - entrySpot) / entrySpot
        // Knockout occurs if deviation exceeds barrier tolerance with slight stochastic risk
        return delta >= tolerance
    }
}
