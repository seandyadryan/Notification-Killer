package com.deploydulupulangnanti.notificationkiller.domain.engine

import com.deploydulupulangnanti.notificationkiller.domain.model.*
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class NotificationProcessorTest {
    private val processor = NotificationProcessor()
    private fun notification(title: String? = null, text: String? = null, pkg: String = "example.app") =
        NotificationPayload("key", pkg, title, text, 1L, false, true)

    @Test fun whitelist_overrides_app_block() {
        val app = AppFilter("example.app", "Example", isBlocked = true, isWhitelisted = true)
        assertTrue(processor.evaluate(notification(), app, emptyList(), true) is EvaluationResult.Keep)
    }

    @Test fun app_without_explicit_filter_is_kept() {
        assertTrue(processor.evaluate(notification(), null, emptyList(), true) is EvaluationResult.Keep)
    }

    @Test fun app_filter_blocks_when_explicitly_enabled() {
        val app = AppFilter("example.app", "Example", isBlocked = true)
        assertTrue(processor.evaluate(notification(), app, emptyList(), true) is EvaluationResult.Dismiss)
    }

    @Test fun keyword_matching_is_case_insensitive_by_default() {
        val rule = KeywordRule(pattern = "promo", scope = RuleScope.TITLE_ONLY)
        assertTrue(processor.evaluate(notification(title = "PROMO today"), null, listOf(rule), true) is EvaluationResult.Dismiss)
    }

    @Test fun matching_supports_exact_and_prefix_rules() {
        val exact = KeywordRule(pattern = "Sale", ruleType = RuleType.EXACT_MATCH)
        val prefix = KeywordRule(pattern = "Game Update", ruleType = RuleType.STARTS_WITH)
        assertTrue(processor.evaluate(notification(title = "Sale"), null, listOf(exact), true) is EvaluationResult.Dismiss)
        assertTrue(processor.evaluate(notification(title = "Game Update 2.0"), null, listOf(prefix), true) is EvaluationResult.Dismiss)
    }

    @Test fun notification_without_title_or_text_does_not_crash_or_match() {
        val rule = KeywordRule(pattern = "OTP")
        assertTrue(processor.evaluate(notification(), null, listOf(rule), true) is EvaluationResult.Keep)
    }

    @Test fun disabled_rule_is_ignored() {
        val rule = KeywordRule(pattern = "sale", isEnabled = false)
        assertTrue(processor.evaluate(notification(title = "sale"), null, listOf(rule), true) is EvaluationResult.Keep)
    }

    @Test fun invalid_or_high_risk_regex_is_rejected() {
        assertTrue(RegexSafetyHelper.compileSafeRegex("[", false) == null)
        assertTrue(RegexSafetyHelper.compileSafeRegex("(a+)+$", false) == null)
        assertTrue(RegexSafetyHelper.compileSafeRegex("a*a*a*a*b", false) == null)
    }

    @Test fun conflicting_keyword_actions_are_kept_regardless_of_rule_order() {
        val review = KeywordRule(pattern = "sale", actionType = ActionType.MARK_FOR_REVIEW)
        val dismiss = KeywordRule(pattern = "sale", actionType = ActionType.AUTO_DISMISS)
        assertTrue(processor.evaluate(notification(title = "sale"), null, listOf(review, dismiss), true) is EvaluationResult.Keep)
        assertTrue(processor.evaluate(notification(title = "sale"), null, listOf(dismiss, review), true) is EvaluationResult.Keep)
    }

    @Test fun master_switch_off_keeps_notification() {
        val rule = KeywordRule(pattern = "sale")
        assertTrue(processor.evaluate(notification(title = "sale"), null, listOf(rule), false) is EvaluationResult.Keep)
    }

    @Test fun same_notification_event_is_deduplicated_without_unbounded_memory() {
        val deduplicator = EventDeduplicator(capacity = 2)
        assertTrue(deduplicator.markIfNew("one"))
        assertFalse(deduplicator.markIfNew("one"))
        assertTrue(deduplicator.markIfNew("two"))
        assertTrue(deduplicator.markIfNew("three"))
        assertTrue(deduplicator.markIfNew("one"))
    }

    @Test fun system_self_and_non_clearable_notifications_are_kept() {
        assertTrue(processor.evaluate(notification(pkg = "android"), null, emptyList(), true) is EvaluationResult.Keep)
        assertTrue(processor.evaluate(notification().copy(isClearable = false), null, emptyList(), true) is EvaluationResult.Keep)
        assertFalse(processor.evaluate(notification(title = "sale"), null, listOf(KeywordRule(pattern = "sale")), true) is EvaluationResult.Keep)
    }
}
