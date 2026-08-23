package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "trade_records")
data class TradeEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val contractId: String,
    val symbol: String,
    val symbolName: String,
    val stake: Double,
    val payout: Double,
    val profit: Double,
    val growthRate: Double,
    val ticksHeld: Int,
    val targetTicks: Int,
    val outcome: String, // WON, KNOCKED_OUT, MANUAL_CASH_OUT
    val isBot: Boolean,
    val entryPrice: Double,
    val exitPrice: Double,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "signal_records")
data class SignalRecordEntity(
    @PrimaryKey
    val signalId: String,
    val symbol: String,
    val symbolName: String,
    val growthRate: Double,
    val confidenceScore: Int,
    val barrierSafetyPercent: Double,
    val targetTicks: Int,
    val signalType: String,
    val strategyName: String,
    val indicatorRationale: String,
    val outcomeStatus: String, // WON, KNOCKED_OUT, EXPIRED
    val profitAchieved: Double,
    val timestamp: Long = System.currentTimeMillis()
)
