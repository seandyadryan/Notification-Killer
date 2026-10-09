package com.deploydulupulangnanti.notificationkiller.data.repository

import com.deploydulupulangnanti.notificationkiller.data.local.AppDatabase
import com.deploydulupulangnanti.notificationkiller.data.local.entity.*
import com.deploydulupulangnanti.notificationkiller.data.local.preferences.PreferenceManager
import com.deploydulupulangnanti.notificationkiller.domain.model.*
import kotlinx.coroutines.flow.*
import java.util.Calendar

class NotificationKillerRepository(private val db: AppDatabase, private val prefs: PreferenceManager) {
    val isAutoCleanEnabled = prefs.isAutoCleanEnabled
    val isQuietHoursEnabled = prefs.isQuietHoursEnabled
    val retentionDays = prefs.retentionDays

    suspend fun setAutoClean(enabled: Boolean) = prefs.setAutoClean(enabled)
    suspend fun setQuietHours(enabled: Boolean) = prefs.setQuietHours(enabled)
    suspend fun setRetentionDays(days: Int) = prefs.setRetentionDays(days)

    fun getRules(): Flow<List<KeywordRule>> = db.ruleDao().getAllRules().map { it.map { e -> e.toDomain() } }
    suspend fun getActiveRules(): List<KeywordRule> = db.ruleDao().getActiveRules().map { it.toDomain() }
    suspend fun saveRule(rule: KeywordRule) = db.ruleDao().insertOrUpdate(rule.toEntity())
    suspend fun deleteRule(rule: KeywordRule) = db.ruleDao().delete(rule.toEntity())

    fun getAppFilters(): Flow<List<AppFilter>> = db.appFilterDao().getAllAppFilters().map { it.map { e -> e.toDomain() } }
    suspend fun getAppFilter(packageName: String): AppFilter? = db.appFilterDao().getAppFilter(packageName)?.toDomain()
    suspend fun saveAppFilter(app: AppFilter) = db.appFilterDao().insertOrUpdate(app.toEntity())
    suspend fun incrementAppCleaned(packageName: String) = db.appFilterDao().incrementCleanedCount(packageName)

    fun getHistory(): Flow<List<HistoryEntity>> = db.historyDao().getAllHistory()
    fun getTodayCleanedCount(): Flow<Int> {
        val cal = Calendar.getInstance().apply { set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0) }
        return db.historyDao().getCleanedCountSince(cal.timeInMillis)
    }
    suspend fun recordHistory(h: HistoryEntity) = db.historyDao().insert(h)
    suspend fun clearHistory() = db.historyDao().clearAll()
    suspend fun purgeOldHistory(days: Int) {
        val cutoff = System.currentTimeMillis() - (days * 24L * 60 * 60 * 1000)
        db.historyDao().deleteOlderThan(cutoff)
    }

    private fun RuleEntity.toDomain() = KeywordRule(id, pattern, ruleType, scope, isCaseSensitive, actionType, isEnabled)
    private fun KeywordRule.toEntity() = RuleEntity(id, pattern, ruleType, scope, isCaseSensitive, actionType, isEnabled)
    private fun AppFilterEntity.toDomain() = AppFilter(packageName, appName, isBlocked, isWhitelisted, cleanedCount)
    private fun AppFilter.toEntity() = AppFilterEntity(packageName, appName, isBlocked, isWhitelisted, cleanedCount)
}
