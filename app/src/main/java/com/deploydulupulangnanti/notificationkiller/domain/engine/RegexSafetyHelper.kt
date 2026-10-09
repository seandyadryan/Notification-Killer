package com.deploydulupulangnanti.notificationkiller.domain.engine

import java.util.regex.Pattern
import java.util.regex.PatternSyntaxException

object RegexSafetyHelper {
    private const val MAX_REGEX_LENGTH = 100
    private const val MAX_INPUT_LENGTH = 500

    fun compileSafeRegex(rawPattern: String, caseSensitive: Boolean): Pattern? {
        if (rawPattern.isBlank() || rawPattern.length > MAX_REGEX_LENGTH) return null
        // Java's backtracking engine can take exponential time on nested or overlapping
        // quantifiers. This deliberately conservative subset rejects the usual ReDoS forms.
        if (Regex("\\([^)]*[+*][^)]*\\)[+*{]").containsMatchIn(rawPattern)) return null
        if (Regex("\\([^)]*\\|[^)]*\\)[+*{]").containsMatchIn(rawPattern)) return null
        if (Regex("\\\\[1-9]").containsMatchIn(rawPattern)) return null
        if (rawPattern.count { it == '+' || it == '*' || it == '{' } > 1) return null
        return try {
            val flags = if (caseSensitive) 0 else Pattern.CASE_INSENSITIVE
            Pattern.compile(rawPattern, flags)
        } catch (_: PatternSyntaxException) {
            null
        }
    }

    fun isSafeMatch(pattern: Pattern, targetText: String): Boolean {
        val truncated = if (targetText.length > MAX_INPUT_LENGTH) targetText.substring(0, MAX_INPUT_LENGTH) else targetText
        return try {
            pattern.matcher(truncated).find()
        } catch (_: RuntimeException) {
            false
        }
    }
}
