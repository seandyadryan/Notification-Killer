package com.deploydulupulangnanti.notificationkiller.domain.engine

import com.deploydulupulangnanti.notificationkiller.domain.model.*
import org.junit.Assert.assertTrue
import org.junit.Test

class NotificationProcessorTest {
    private val processor = NotificationProcessor()

    @Test
    fun whitelist_overrides_block_rule() {
        val payload = NotificationPayload("k1", "com.bank.app", "OTP", "1234", System.currentTimeMillis(), false, true)
        val app = AppFilter("com.bank.app", "Bank", isBlocked = true, isWhitelisted = true)
        val res = processor.evaluate(payload, app, emptyList(), true)
        assertTrue(res is EvaluationResult.Keep)
    }

    @Test
    fun keyword_contains_case_insensitive_matches() {
        val payload = NotificationPayload("k2", "com.promo.app", "PROMO SALE", "Diskon", System.currentTimeMillis(), false, true)
        val rules = listOf(KeywordRule(1, "promo", RuleType.CONTAINS, isCaseSensitive = false, isEnabled = true))
        val res = processor.evaluate(payload, null, rules, true)
        assertTrue(res is EvaluationResult.Dismiss)
    }
}
