package com.deploydulupulangnanti.notificationkiller.data.local.dao

import androidx.room.*
import com.deploydulupulangnanti.notificationkiller.data.local.entity.*
import kotlinx.coroutines.flow.Flow

@Dao
interface RuleDao {
    @Query("SELECT * FROM keyword_rules ORDER BY id DESC")
    fun getAllRules(): Flow<List<RuleEntity>>

    @Query("SELECT * FROM keyword_rules WHERE isEnabled = 1")
    suspend fun getActiveRules(): List<RuleEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(rule: RuleEntity): Long

    @Delete
    suspend fun delete(rule: RuleEntity)
}

@Dao
interface AppFilterDao {
    @Query("SELECT * FROM app_filters ORDER BY appName ASC")
    fun getAllAppFilters(): Flow<List<AppFilterEntity>>

    @Query("SELECT * FROM app_filters WHERE packageName = :packageName LIMIT 1")
    suspend fun getAppFilter(packageName: String): AppFilterEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(filter: AppFilterEntity)

    @Query("UPDATE app_filters SET cleanedCount = cleanedCount + 1 WHERE packageName = :packageName")
    suspend fun incrementCleanedCount(packageName: String)
}

@Dao
interface HistoryDao {
    @Query("SELECT * FROM notification_history ORDER BY timestamp DESC LIMIT 500")
    fun getAllHistory(): Flow<List<HistoryEntity>>

    @Query("SELECT COUNT(*) FROM notification_history WHERE timestamp >= :sinceTimestamp")
    fun getCleanedCountSince(sinceTimestamp: Long): Flow<Int>

    @Insert
    suspend fun insert(history: HistoryEntity)

    @Query("DELETE FROM notification_history")
    suspend fun clearAll()

    @Query("DELETE FROM notification_history WHERE timestamp < :beforeTimestamp")
    suspend fun deleteOlderThan(beforeTimestamp: Long)
}
