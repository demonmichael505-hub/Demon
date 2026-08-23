package com.example.data.repository

import com.example.data.local.SignalDao
import com.example.data.local.SignalRecordEntity
import com.example.data.local.TradeDao
import com.example.data.local.TradeEntity
import kotlinx.coroutines.flow.Flow

class TradeRepository(
    private val tradeDao: TradeDao,
    private val signalDao: SignalDao
) {
    val allTrades: Flow<List<TradeEntity>> = tradeDao.getAllTrades()
    val recentSignals: Flow<List<SignalRecordEntity>> = signalDao.getRecentSignals()
    val totalTradesCount: Flow<Int> = tradeDao.getTradeCount()
    val totalProfit: Flow<Double?> = tradeDao.getTotalProfit()

    suspend fun saveTrade(trade: TradeEntity): Long {
        return tradeDao.insertTrade(trade)
    }

    suspend fun saveSignal(signal: SignalRecordEntity) {
        signalDao.insertSignal(signal)
    }

    suspend fun updateSignalOutcome(signalId: String, status: String, profit: Double) {
        signalDao.updateSignalOutcome(signalId, status, profit)
    }

    suspend fun clearHistory() {
        tradeDao.clearAllTrades()
        signalDao.clearSignals()
    }
}
