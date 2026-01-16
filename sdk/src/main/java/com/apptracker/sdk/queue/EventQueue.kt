package com.apptracker.sdk.queue

import android.content.Context
import android.content.SharedPreferences
import com.apptracker.sdk.Event
import com.apptracker.sdk.network.ApiClient
import com.apptracker.sdk.network.BatchEventsRequest
import com.apptracker.sdk.storage.AppTrackerDatabase
import com.apptracker.sdk.storage.EventEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

/**
 * Manages event queue, batching, and sending to the backend.
 */
class EventQueue(
    private val context: Context,
    private val projectKey: String,
    private val baseUrl: String,
    private val batchSize: Int,
    private val flushInterval: Long
) {
    private val database = AppTrackerDatabase.getDatabase(context)
    private val eventDao = database.eventDao()
    private val apiClient = ApiClient.create(baseUrl, enableLogging = true)
    private val prefs: SharedPreferences = context.getSharedPreferences("apptracker_prefs", Context.MODE_PRIVATE)

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var flushJob: Job? = null

    private val anonymousId: String
        get() {
            val stored = prefs.getString("anonymous_id", null)
            return if (stored != null) {
                stored
            } else {
                val newId = "anon-${UUID.randomUUID()}"
                prefs.edit().putString("anonymous_id", newId).apply()
                newId
            }
        }

    private val sessionId: String
        get() {
            val stored = prefs.getString("session_id", null)
            val lastActivity = prefs.getLong("last_activity", 0)
            val now = System.currentTimeMillis()
            val sessionTimeout = 30 * 60 * 1000L // 30 minutes

            return if (stored != null && (now - lastActivity) < sessionTimeout) {
                stored
            } else {
                val newSessionId = "session-${UUID.randomUUID()}"
                prefs.edit()
                    .putString("session_id", newSessionId)
                    .putLong("last_activity", now)
                    .apply()
                newSessionId
            }
        }

    private var currentUserId: String? = null

    init {
        startPeriodicFlush()
    }

    /**
     * Add an event to the queue.
     */
    suspend fun enqueue(event: Event) {
        withContext(Dispatchers.IO) {
            val enrichedEvent = event.copy(
                anonymousId = event.anonymousId ?: anonymousId,
                userId = event.userId ?: currentUserId,
                sessionId = event.sessionId ?: sessionId
            )

            val entity = EventEntity.fromEvent(enrichedEvent)
            eventDao.insert(entity)

            // Update last activity for session management
            prefs.edit().putLong("last_activity", System.currentTimeMillis()).apply()

            // Check if we should flush immediately
            val count = eventDao.getEventCount()
            if (count >= batchSize) {
                flush()
            }
        }
    }

    /**
     * Set the current user ID.
     */
    fun setUserId(userId: String?) {
        currentUserId = userId
        prefs.edit().putString("user_id", userId).apply()
    }

    /**
     * Get the current user ID.
     */
    fun getUserId(): String? {
        return currentUserId ?: prefs.getString("user_id", null)
    }

    /**
     * Set anonymous ID.
     */
    fun setAnonymousId(anonymousId: String) {
        prefs.edit().putString("anonymous_id", anonymousId).apply()
    }

    /**
     * Flush all pending events to the server.
     */
    suspend fun flush() {
        withContext(Dispatchers.IO) {
            try {
                val pendingEvents = eventDao.getPendingEvents(batchSize)
                if (pendingEvents.isEmpty()) {
                    return@withContext
                }

                val events = pendingEvents.map { it.toEvent() }
                val request = BatchEventsRequest(
                    projectKey = projectKey,
                    events = events
                )

                val response = apiClient.sendBatchEvents(request)

                if (response.isSuccessful) {
                    // Delete sent events
                    val ids = pendingEvents.map { it.id }
                    eventDao.deleteByIds(ids)
                } else {
                    // Keep events for retry
                    // Could implement exponential backoff here
                }
            } catch (e: Exception) {
                // Network error - events remain in queue for retry
                e.printStackTrace()
            }
        }
    }

    /**
     * Start periodic flush job.
     */
    private fun startPeriodicFlush() {
        flushJob?.cancel()
        flushJob = scope.launch {
            while (isActive) {
                delay(flushInterval)
                flush()
            }
        }
    }

    /**
     * Stop the periodic flush job.
     */
    fun stop() {
        flushJob?.cancel()
    }

    /**
     * Get the number of pending events.
     */
    suspend fun getPendingCount(): Int {
        return withContext(Dispatchers.IO) {
            eventDao.getEventCount()
        }
    }
}

