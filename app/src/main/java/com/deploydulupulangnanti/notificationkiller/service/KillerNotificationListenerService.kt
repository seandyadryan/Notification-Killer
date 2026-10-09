package com.deploydulupulangnanti.notificationkiller.service

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import com.deploydulupulangnanti.notificationkiller.NotificationKillerApp
import com.deploydulupulangnanti.notificationkiller.data.local.entity.HistoryEntity
import com.deploydulupulangnanti.notificationkiller.domain.engine.NotificationProcessor
import com.deploydulupulangnanti.notificationkiller.domain.engine.EventDeduplicator
import com.deploydulupulangnanti.notificationkiller.domain.model.EvaluationResult
import com.deploydulupulangnanti.notificationkiller.domain.model.NotificationPayload
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.security.MessageDigest
import java.util.concurrent.ConcurrentHashMap

class KillerNotificationListenerService : NotificationListenerService() {
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val processor = NotificationProcessor()
    private val seenEvents = EventDeduplicator()
    private val dismissalRequests = ConcurrentHashMap.newKeySet<String>()
    @Volatile private var listenerReady = false

    override fun onListenerConnected() {
        super.onListenerConnected()
        listenerReady = true
        ListenerRuntime.isConnected = true
    }

    override fun onListenerDisconnected() {
        listenerReady = false
        ListenerRuntime.isConnected = false
        super.onListenerDisconnected()
        requestRebind(android.content.ComponentName(this, KillerNotificationListenerService::class.java))
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        if (!listenerReady) return
        val posted = sbn ?: return
        val app = application as? NotificationKillerApp ?: return
        // Copy only the fields needed for evaluation. Notification text is never persisted or logged.
        val extras = posted.notification.extras
        val title = extras.getCharSequence("android.title")?.toString()
        val text = extras.getCharSequence("android.text")?.toString()
        val payload = NotificationPayload(
            key = posted.key,
            packageName = posted.packageName,
            title = title,
            text = text,
            postTime = posted.postTime,
            isOngoing = posted.isOngoing,
            isClearable = posted.isClearable
        )
        val eventId = digest("${payload.packageName}|${payload.key}|${payload.postTime}")
        if (!seenEvents.markIfNew(eventId)) return

        scope.launch {
            try {
                if (!listenerReady) return@launch
                val repo = app.repository
                if (repo.hasHistoryEvent(eventId)) return@launch
                val masterEnabled = repo.isAutoCleanEnabled.first()
                val filter = repo.getAppFilter(payload.packageName)
                val rules = repo.getActiveRules()
                val outcome = processor.evaluate(payload, filter, rules, masterEnabled)
                val displayName = filter?.appName ?: payload.packageName
                val action: String
                val result: String
                val matchedRule: String
                when (outcome) {
                    is EvaluationResult.Dismiss -> {
                        action = "AUTO_DISMISSED"
                        matchedRule = outcome.matchedRule
                        result = try {
                            withContext(Dispatchers.Main.immediate) {
                                if (!listenerReady) throw IllegalStateException("Listener disconnected")
                                dismissalRequests.add(payload.key)
                                cancelNotification(payload.key)
                            }
                            scope.launch {
                                delay(DISMISSAL_CALLBACK_TIMEOUT_MS)
                                dismissalRequests.remove(payload.key)
                            }
                            "REQUESTED"
                        } catch (_: Exception) {
                            dismissalRequests.remove(payload.key)
                            "FAILED"
                        }
                    }
                    is EvaluationResult.MarkReview -> {
                        action = "REVIEW"
                        result = "RECORDED"
                        matchedRule = outcome.matchedRule
                    }
                    is EvaluationResult.Keep -> {
                        action = "KEPT"
                        result = outcome.reason
                        matchedRule = ""
                    }
                }
                val inserted = repo.recordHistory(
                    HistoryEntity(
                        eventId = eventId,
                        packageName = payload.packageName,
                        appName = displayName,
                        matchedRule = matchedRule,
                        actionTaken = action,
                        result = result,
                        timestamp = System.currentTimeMillis()
                    )
                )
                if (inserted && action == "AUTO_DISMISSED" && result == "REQUESTED") {
                    repo.incrementAppCleaned(payload.packageName)
                }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                // Notification callbacks must not crash the system process. No content is logged.
            }
        }
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification, rankingMap: RankingMap, reason: Int) {
        super.onNotificationRemoved(sbn, rankingMap, reason)
        // A removal callback is not a new auto-clean event. Only an outstanding key requested by
        // this listener is acknowledged here; manual removals are intentionally not counted.
        if (reason == REASON_LISTENER_CANCEL) dismissalRequests.remove(sbn.key)
    }

    override fun onDestroy() {
        listenerReady = false
        ListenerRuntime.isConnected = false
        scope.cancel()
        super.onDestroy()
    }

    private fun digest(value: String): String = MessageDigest.getInstance("SHA-256")
        .digest(value.toByteArray(Charsets.UTF_8))
        .joinToString("") { "%02x".format(it) }

    companion object {
        private const val DISMISSAL_CALLBACK_TIMEOUT_MS = 60_000L
    }
}

object ListenerRuntime {
    @Volatile var isConnected: Boolean = false
        internal set
}
