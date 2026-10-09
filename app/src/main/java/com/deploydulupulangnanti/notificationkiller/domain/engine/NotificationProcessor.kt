package com.deploydulupulangnanti.notificationkiller.domain.engine

import com.deploydulupulangnanti.notificationkiller.domain.model.*

class NotificationProcessor {
    fun evaluate(
        payload: NotificationPayload,
        appFilter: AppFilter?,
        activeRules: List<KeywordRule>,
        isMasterAutoCleanEnabled: Boolean
    ): EvaluationResult {
        if (!isMasterAutoCleanEnabled) return EvaluationResult.Keep("Auto-Clean dinonaktifkan")
        if (payload.isOngoing || !payload.isClearable) return EvaluationResult.Keep("Ongoing / Non-clearable")
        if (payload.packageName == "android" || payload.packageName == "com.deploydulupulangnanti.notificationkiller") {
            return EvaluationResult.Keep("System/Self protected")
        }
        if (appFilter != null && appFilter.isWhitelisted) {
            return EvaluationResult.Keep("Whitelisted: ${appFilter.appName}")
        }
        if (appFilter != null && appFilter.isBlocked) {
            return EvaluationResult.Dismiss("Blocked app: ${appFilter.appName}", payload.packageName)
        }

        val title = payload.title ?: ""
        val text = payload.text ?: ""

        val matches = activeRules.filter { rule ->
            rule.isEnabled && rule.pattern.isNotBlank() && matchRule(rule, title, text)
        }
        val actions = matches.map { it.actionType }.distinct()
        if (actions.size > 1) return EvaluationResult.Keep("Conflicting keyword rules")
        val rule = matches.firstOrNull()
        if (rule != null) {
            return when (rule.actionType) {
                ActionType.AUTO_DISMISS -> EvaluationResult.Dismiss(rule.pattern, payload.packageName)
                ActionType.MARK_FOR_REVIEW -> EvaluationResult.MarkReview(rule.pattern, payload.packageName)
            }
        }
        return EvaluationResult.Keep("No rule matched")
    }

    private fun matchRule(rule: KeywordRule, title: String, text: String): Boolean {
        val targets = when (rule.scope) {
            RuleScope.TITLE_ONLY -> listOf(title)
            RuleScope.TEXT_ONLY -> listOf(text)
            RuleScope.TITLE_OR_TEXT -> listOf(title, text)
        }
        return targets.any { target ->
            if (target.isEmpty()) return@any false
            when (rule.ruleType) {
                RuleType.CONTAINS -> target.contains(rule.pattern, ignoreCase = !rule.isCaseSensitive)
                RuleType.EXACT_MATCH -> target.equals(rule.pattern, ignoreCase = !rule.isCaseSensitive)
                RuleType.STARTS_WITH -> target.startsWith(rule.pattern, ignoreCase = !rule.isCaseSensitive)
                RuleType.REGEX -> {
                    val p = RegexSafetyHelper.compileSafeRegex(rule.pattern, rule.isCaseSensitive)
                    p != null && RegexSafetyHelper.isSafeMatch(p, target)
                }
            }
        }
    }
}
