package com.deploydulupulangnanti.notificationkiller.domain.engine

import java.util.regex.Pattern
import java.util.regex.PatternSyntaxException

object RegexSafetyHelper {
    private const val MAX_REGEX_LENGTH = 100

    fun compileSafeRegex(rawPattern: String, caseSensitive: Boolean): Pattern? {
        if (rawPattern.isBlank() || rawPattern.length > MAX_REGEX_LENGTH) return null
        if (rawPattern.contains("(.*)+") || rawPattern.contains("(.+)+")) return null
        return try {
            val flags = if (caseSensitive) 0 else Pattern.CASE_INSENSITIVE
            Pattern.compile(rawPattern, flags)
        } catch (_: PatternSyntaxException) {
            null
        }
    }

    fun isSafeMatch(pattern: Pattern, targetText: String): Boolean {
        val truncated = if (targetText.length > 500) targetText.substring(0, 500) else targetText
        return try {
            pattern.matcher(truncated).find()
        } catch (_: Throwable) {
            false
        }
    }
}
