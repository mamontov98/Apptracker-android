package com.apptracker.sdk

import android.content.Context
import com.apptracker.sdk.network.ApiClient
import com.apptracker.sdk.network.CreateProjectRequest
import com.apptracker.sdk.queue.EventQueue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.Instant

/**
 * Main AppTracker SDK class.
 * Singleton instance for tracking analytics events.
 */
object AppTracker {
    private var isInitialized = false
    private var eventQueue: EventQueue? = null
    private var config: AppTrackerConfig? = null

    /**
     * Initialize the SDK with configuration.
     * Must be called before using any other methods.
     */
    fun initialize(context: Context, config: AppTrackerConfig) {
        if (isInitialized) {
            throw IllegalStateException("AppTracker is already initialized")
        }

        this.config = config
        eventQueue = EventQueue(
            context = context.applicationContext,
            projectKey = config.projectKey,
            baseUrl = config.baseUrl,
            batchSize = config.batchSize,
            flushInterval = config.flushInterval
        )

        isInitialized = true
    }

    /**
     * Track an event.
     * @param eventName Name of the event
     * @param properties Optional properties map
     */
    fun track(eventName: String, properties: Map<String, Any>? = null) {
        requireInitialized()

        val timestamp = Instant.now().toString()
        val event = Event(
            eventName = eventName,
            timestamp = timestamp,
            properties = properties
        )

        CoroutineScope(Dispatchers.IO).launch {
            eventQueue?.enqueue(event)
        }
    }

    /**
     * Identify a user.
     * @param userId User identifier
     */
    fun identify(userId: String) {
        requireInitialized()
        eventQueue?.setUserId(userId)
    }

    /**
     * Set anonymous ID.
     * @param anonymousId Anonymous identifier
     */
    fun setAnonymousId(anonymousId: String) {
        requireInitialized()
        eventQueue?.setAnonymousId(anonymousId)
    }

    /**
     * Get current user ID.
     */
    fun getUserId(): String? {
        requireInitialized()
        return eventQueue?.getUserId()
    }

    /**
     * Flush all pending events to the server immediately.
     */
    fun flush() {
        requireInitialized()
        CoroutineScope(Dispatchers.IO).launch {
            eventQueue?.flush()
        }
    }

    /**
     * Get the number of pending events.
     */
    suspend fun getPendingCount(): Int {
        requireInitialized()
        return eventQueue?.getPendingCount() ?: 0
    }

    /**
     * Check if SDK is initialized.
     */
    fun isInitialized(): Boolean = isInitialized

    /**
     * Get current configuration.
     */
    fun getConfig(): AppTrackerConfig? = config

    /**
     * Check if a project exists in the backend.
     * Can be called before SDK initialization.
     * @param projectKey Project key to check
     * @param baseUrl Base URL of the backend
     * @return true if project exists, false otherwise
     */
    suspend fun projectExists(projectKey: String, baseUrl: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                android.util.Log.d("AppTracker", "Checking if project exists: $projectKey")
                val api = ApiClient.create(baseUrl)
                val response = api.getProjects(projectKey)
                val exists = response.isSuccessful && response.body()?.projects?.isNotEmpty() == true
                android.util.Log.d("AppTracker", "Project exists check result: $exists (response code: ${response.code()})")
                exists
            } catch (e: Exception) {
                android.util.Log.e("AppTracker", "Error checking project existence: ${e.message}", e)
                false
            }
        }
    }

    /**
     * Create a project in the backend.
     * Can be called before SDK initialization.
     * @param projectName Name of the project
     * @param baseUrl Base URL of the backend
     * @return Project key if created successfully, null otherwise
     */
    suspend fun createProject(projectName: String, baseUrl: String): String? {
        return withContext(Dispatchers.IO) {
            try {
                android.util.Log.d("AppTracker", "Creating project: $projectName")
                val api = ApiClient.create(baseUrl)
                val response = api.createProject(CreateProjectRequest(projectName))
                if (response.isSuccessful) {
                    val projectKey = response.body()?.projectKey
                    android.util.Log.d("AppTracker", "Project created successfully: $projectKey")
                    projectKey
                } else {
                    android.util.Log.e("AppTracker", "Failed to create project. Response code: ${response.code()}, message: ${response.message()}")
                    null
                }
            } catch (e: Exception) {
                android.util.Log.e("AppTracker", "Error creating project: ${e.message}", e)
                null
            }
        }
    }

    /**
     * Ensure project exists and get its key.
     * Can be called before SDK initialization.
     * @param projectName Project name
     * @param baseUrl Base URL of the backend
     * @param savedProjectKey Optional saved project key to check first
     * @return Project key if exists or was created successfully, null otherwise
     */
    suspend fun ensureProjectAndGetKey(
        projectName: String,
        baseUrl: String,
        savedProjectKey: String? = null
    ): String? {
        return withContext(Dispatchers.IO) {
            android.util.Log.d("AppTracker", "ensureProjectAndGetKey called with name: $projectName, baseUrl: $baseUrl")
            
            // First, check if saved project key exists
            if (!savedProjectKey.isNullOrEmpty()) {
                android.util.Log.d("AppTracker", "Checking saved project key: $savedProjectKey")
                if (projectExists(savedProjectKey, baseUrl)) {
                    android.util.Log.d("AppTracker", "Saved project key exists, using it")
                    return@withContext savedProjectKey
                } else {
                    android.util.Log.d("AppTracker", "Saved project key doesn't exist, will create new project")
                }
            } else {
                android.util.Log.d("AppTracker", "No saved project key, will create new project")
            }
            
            // Project doesn't exist, create it
            android.util.Log.d("AppTracker", "Creating new project...")
            createProject(projectName, baseUrl)
        }
    }

    private fun requireInitialized() {
        if (!isInitialized) {
            throw IllegalStateException("AppTracker must be initialized before use. Call AppTracker.initialize() first.")
        }
    }
}

