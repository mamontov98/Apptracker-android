package com.apptracker.sdk

/**
 * Configuration for AppTracker SDK.
 */
data class AppTrackerConfig(
    val projectKey: String,
    val baseUrl: String = "http://10.0.2.2:5000", // Default for Android emulator (localhost mapping)
    val batchSize: Int = 20,
    val flushInterval: Long = 30_000L // 30 seconds in milliseconds
) {
    init {
        require(projectKey.isNotBlank()) { "projectKey cannot be blank" }
        require(batchSize > 0) { "batchSize must be greater than 0" }
        require(flushInterval > 0) { "flushInterval must be greater than 0" }
    }
}

