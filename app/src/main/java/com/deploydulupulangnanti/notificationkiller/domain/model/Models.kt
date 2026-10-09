package com.deploydulupulangnanti.notificationkiller.domain.model

enum class RuleType { CONTAINS, EXACT_MATCH, STARTS_WITH, REGEX }
enum class RuleScope { TITLE_ONLY, TEXT_ONLY, TITLE_OR_TEXT }
enum class ActionType { AUTO_DISMISS, MARK_FOR_REVIEW }

data class KeywordRule(
    val id: Long = 0,
    val pattern: String,
    val ruleType: RuleType = RuleType.CONTAINS,
    val scope: RuleScope = RuleScope.TITLE_OR_TEXT,
    val isCaseSensitive: Boolean = false,
    val actionType: ActionType = ActionType.AUTO_DISMISS,
    val isEnabled: Boolean = true
)

data class AppFilter(
    val packageName: String,
    val appName: String,
    val isBlocked: Boolean = false,
    val isWhitelisted: Boolean = false,
    val cleanedCount: Int = 0
)

data class NotificationPayload(
    val key: String,
    val packageName: String,
    val title: String?,
    val text: String?,
    val postTime: Long,
    val isOngoing: Boolean,
    val isClearable: Boolean
)

sealed class EvaluationResult {
    data class Keep(val reason: String) : EvaluationResult()
    data class Dismiss(val matchedRule: String, val packageName: String) : EvaluationResult()
    data class MarkReview(val matchedRule: String, val packageName: String) : EvaluationResult()
}
