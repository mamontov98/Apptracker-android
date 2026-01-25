package com.apptracker.sdk

// Represents an analytics event to be tracked
data class Event(
    val eventName: String,
    val timestamp: String, // ISO 8601 format
    val anonymousId: String? = null,
    val userId: String? = null,
    val sessionId: String? = null,
    val properties: Map<String, Any>? = null,
    // Process tracking fields (optional)
    val processName: String? = null,
    val processId: String? = null,
    val processStep: String? = null // "START" or "END"
)



