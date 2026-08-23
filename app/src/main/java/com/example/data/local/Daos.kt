package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface TradeDao {
    @Query("SELECT * FROM trade_records ORDER BY timestamp DESC")
    fun getAllTrades(): Flow<List<TradeEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrade(trade: TradeEntity): Long

    @Query("SELECT COUNT(*) FROM trade_records")
    fun getTradeCount(): Flow<Int>

    @Query("SELECT SUM(profit) FROM trade_records")
    fun getTotalProfit(): Flow<Double?>

    @Query("DELETE FROM trade_records")
    suspend fun clearAllTrades()
}

@Dao
interface SignalDao {
    @Query("SELECT * FROM signal_records ORDER BY timestamp DESC LIMIT 50")
    fun getRecentSignals(): Flow<List<SignalRecordEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSignal(signal: SignalRecordEntity)

    @Query("UPDATE signal_records SET outcomeStatus = :status, profitAchieved = :profit WHERE signalId = :signalId")
    suspend fun updateSignalOutcome(signalId: String, status: String, profit: Double)

    @Query("DELETE FROM signal_records")
    suspend fun clearSignals()
}
