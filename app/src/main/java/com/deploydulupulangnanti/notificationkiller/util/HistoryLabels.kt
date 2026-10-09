package com.deploydulupulangnanti.notificationkiller.util

import androidx.annotation.StringRes
import com.deploydulupulangnanti.notificationkiller.R

@StringRes
fun historyActionResource(action: String): Int = when (action) {
    "AUTO_DISMISSED" -> R.string.history_auto_dismissed
    "REVIEW" -> R.string.history_review
    "KEPT" -> R.string.history_kept
    else -> R.string.history_unknown
}

@StringRes
fun historyResultResource(result: String): Int = when (result) {
    "REQUESTED" -> R.string.history_requested
    "RECORDED" -> R.string.history_recorded
    "FAILED" -> R.string.history_failed
    else -> R.string.history_unknown
}
