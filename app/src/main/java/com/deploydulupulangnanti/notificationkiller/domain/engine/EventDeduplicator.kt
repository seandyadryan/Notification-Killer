package com.deploydulupulangnanti.notificationkiller.domain.engine

/** Keeps a bounded in-memory set of notification update events already handled in this process. */
class EventDeduplicator(private val capacity: Int = 500) {
    private val events = LinkedHashMap<String, Unit>()

    @Synchronized
    fun markIfNew(eventId: String): Boolean {
        if (events.containsKey(eventId)) return false
        events[eventId] = Unit
        while (events.size > capacity) events.remove(events.keys.first())
        return true
    }
}
