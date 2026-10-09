package com.deploydulupulangnanti.notificationkiller.data.local.preferences

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "settings_prefs")

class PreferenceManager(private val context: Context) {
    companion object {
        val KEY_AUTO_CLEAN = booleanPreferencesKey("is_auto_clean_enabled")
        val KEY_RETENTION_DAYS = intPreferencesKey("retention_days")
    }

    val isAutoCleanEnabled: Flow<Boolean> = context.dataStore.data.map { it[KEY_AUTO_CLEAN] ?: false }
    val retentionDays: Flow<Int> = context.dataStore.data.map { it[KEY_RETENTION_DAYS] ?: 7 }

    suspend fun setAutoClean(enabled: Boolean) = context.dataStore.edit { it[KEY_AUTO_CLEAN] = enabled }
    suspend fun setRetentionDays(days: Int) = context.dataStore.edit { it[KEY_RETENTION_DAYS] = days }
}
