package com.apptracker.sdk.storage

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.apptracker.sdk.Event

/**
 * Room entity for storing events locally.
 */
@Entity(tableName = "events")
data class EventEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val eventName: String,
    val timestamp: String,
    val anonymousId: String?,
    val userId: String?,
    val sessionId: String?,
    val propertiesJson: String?, // Stored as JSON string
    val createdAt: Long = System.currentTimeMillis()
) {
    fun toEvent(): Event {
        return Event(
            eventName = eventName,
            timestamp = timestamp,
            anonymousId = anonymousId,
            userId = userId,
            sessionId = sessionId,
            properties = propertiesJson?.let { parsePropertiesJson(it) }
        )
    }

    companion object {
        fun fromEvent(event: Event): EventEntity {
            return EventEntity(
                eventName = event.eventName,
                timestamp = event.timestamp,
                anonymousId = event.anonymousId,
                userId = event.userId,
                sessionId = event.sessionId,
                propertiesJson = event.properties?.let { serializeProperties(it) }
            )
        }

        private fun serializeProperties(properties: Map<String, Any>): String {
            return com.google.gson.Gson().toJson(properties)
        }

        private fun parsePropertiesJson(json: String): Map<String, Any>? {
            return try {
                @Suppress("UNCHECKED_CAST")
                com.google.gson.Gson().fromJson(json, Map::class.java) as? Map<String, Any>
            } catch (e: Exception) {
                null
            }
        }
    }
}



