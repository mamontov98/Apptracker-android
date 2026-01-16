package com.apptracker.sdk

/**
 * Configuration for AppTracker SDK.
 */
data class AppTrackerConfig(
    val projectName: String,
    val baseUrl: String = "https://apptracker-backend.vercel.app", // Default production URL
    val projectKey: String? = null, // Optional: if provided, will use this key instead of creating/finding one
    val batchSize: Int = 20,
    val flushInterval: Long = 30_000L // 30 seconds in milliseconds
) {
    init {
        require(projectName.isNotBlank()) { "projectName cannot be blank" }
        require(batchSize > 0) { "batchSize must be greater than 0" }
        require(flushInterval > 0) { "flushInterval must be greater than 0" }
    }
}

