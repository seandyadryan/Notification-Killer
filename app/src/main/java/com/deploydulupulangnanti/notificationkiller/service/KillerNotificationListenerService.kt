package com.deploydulupulangnanti.notificationkiller.service

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import com.deploydulupulangnanti.notificationkiller.NotificationKillerApp
import com.deploydulupulangnanti.notificationkiller.data.local.entity.HistoryEntity
import com.deploydulupulangnanti.notificationkiller.domain.engine.NotificationProcessor
import com.deploydulupulangnanti.notificationkiller.domain.model.EvaluationResult
import com.deploydulupulangnanti.notificationkiller.domain.model.NotificationPayload
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first

class KillerNotificationListenerService : NotificationListenerService() {
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val processor = NotificationProcessor()

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        if (sbn == null) return
        val app = application as? NotificationKillerApp ?: return
        val repo = app.repository

        scope.launch {
            try {
                val extras = sbn.notification.extras
                val title = extras.getCharSequence("android.title")?.toString()
                val text = extras.getCharSequence("android.text")?.toString()

                val payload = NotificationPayload(
                    key = sbn.key,
                    packageName = sbn.packageName,
                    title = title,
                    text = text,
                    postTime = sbn.postTime,
                    isOngoing = sbn.isOngoing,
                    isClearable = sbn.isClearable
                )

                val isMaster = repo.isAutoCleanEnabled.first()
                val filter = repo.getAppFilter(sbn.packageName)
                val rules = repo.getActiveRules()

                when (val result = processor.evaluate(payload, filter, rules, isMaster)) {
                    is EvaluationResult.Dismiss -> {
                        cancelNotification(sbn.key)
                        repo.incrementAppCleaned(sbn.packageName)
                        repo.recordHistory(
                            HistoryEntity(
                                packageName = sbn.packageName,
                                appName = filter?.appName ?: sbn.packageName,
                                titlePreview = if (!title.isNullOrBlank()) title.take(30) else "No Title",
                                matchedRule = result.matchedRule,
                                actionTaken = "AUTO_DISMISSED",
                                timestamp = System.currentTimeMillis()
                            )
                        )
                    }
                    is EvaluationResult.MarkReview -> {
                        repo.recordHistory(
                            HistoryEntity(
                                packageName = sbn.packageName,
                                appName = filter?.appName ?: sbn.packageName,
                                titlePreview = title?.take(30) ?: "No Title",
                                matchedRule = result.matchedRule,
                                actionTaken = "REVIEW",
                                timestamp = System.currentTimeMillis()
                            )
                        )
                    }
                    is EvaluationResult.Keep -> {}
                }
            } catch (_: Throwable) {}
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
    }
}
