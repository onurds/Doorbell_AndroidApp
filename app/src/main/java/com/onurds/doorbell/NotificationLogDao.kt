package com.onurds.doorbell

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

@Dao
interface NotificationLogDao {
    @Query("SELECT * FROM notification_logs ORDER BY timestamp DESC")
    fun getAllLogs(): Flow<List<NotificationLogEntity>>

    // Date filtering
    @Query("SELECT * FROM notification_logs WHERE date(timestamp) = date(:selectedDate) ORDER BY timestamp DESC")
    fun getLogsByDate(selectedDate: LocalDateTime): Flow<List<NotificationLogEntity>>

    @Insert
    suspend fun insertLog(log: NotificationLogEntity)

    // Delete operation
    @Query("DELETE FROM notification_logs")
    suspend fun deleteAllLogs()
}
