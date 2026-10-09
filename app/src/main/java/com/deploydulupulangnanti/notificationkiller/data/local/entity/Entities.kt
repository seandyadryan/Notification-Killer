package com.deploydulupulangnanti.notificationkiller.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.deploydulupulangnanti.notificationkiller.domain.model.ActionType
import com.deploydulupulangnanti.notificationkiller.domain.model.RuleScope
import com.deploydulupulangnanti.notificationkiller.domain.model.RuleType

@Entity(tableName = "keyword_rules")
data class RuleEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val pattern: String,
    val ruleType: RuleType,
    val scope: RuleScope,
    val isCaseSensitive: Boolean,
    val actionType: ActionType,
    val isEnabled: Boolean
)

@Entity(tableName = "app_filters")
data class AppFilterEntity(
    @PrimaryKey val packageName: String,
    val appName: String,
    val isBlocked: Boolean,
    val isWhitelisted: Boolean,
    val cleanedCount: Int = 0
)

@Entity(tableName = "notification_history")
data class HistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val packageName: String,
    val appName: String,
    val titlePreview: String,
    val matchedRule: String,
    val actionTaken: String,
    val timestamp: Long
)
