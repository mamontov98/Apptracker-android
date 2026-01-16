package com.apptracker.sdk

import android.content.Context
import android.content.SharedPreferences
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
    
    // Pending events and user data before initialization
    private val pendingEvents = mutableListOf<Event>()
    private var pendingUserId: String? = null
    private var pendingAnonymousId: String? = null

    /**
     * Initialize the SDK with configuration.
     * Automatically ensures project exists and gets/creates project key.
     * Must be called before using any other methods.
     * 
     * @param context Application context
     * @param config SDK configuration with projectName and baseUrl
     * @throws IllegalStateException if SDK is already initialized or if project key cannot be obtained
     */
    suspend fun initialize(context: Context, config: AppTrackerConfig) {
        if (isInitialized) {
            throw IllegalStateException("AppTracker is already initialized")
        }

        this.config = config
        
        // Get or create project key automatically
        val projectKey = ensureProjectAndGetKeyWithContext(
            context = context.applicationContext,
            projectName = config.projectName,
            baseUrl = config.baseUrl,
            providedProjectKey = config.projectKey
        )
        
        if (projectKey == null) {
            throw IllegalStateException("Failed to get project key. Please check your baseUrl and network connection.")
        }

        eventQueue = EventQueue(
            context = context.applicationContext,
            projectKey = projectKey,
            baseUrl = config.baseUrl,
            batchSize = config.batchSize,
            flushInterval = config.flushInterval
        )

        // Apply pending user identification if any
        pendingUserId?.let { 
            eventQueue?.setUserId(it)
            android.util.Log.d("AppTracker", "Applied pending user ID: $it")
        }
        pendingAnonymousId?.let { 
            eventQueue?.setAnonymousId(it)
            android.util.Log.d("AppTracker", "Applied pending anonymous ID: $it")
        }
        
        // Transfer all pending events to the queue
        val pendingCount = synchronized(pendingEvents) {
            pendingEvents.size
        }
        
        if (pendingCount > 0) {
            withContext(Dispatchers.IO) {
                synchronized(pendingEvents) {
                    pendingEvents.forEach { event ->
                        eventQueue?.enqueue(event)
                    }
                    pendingEvents.clear()
                }
            }
            android.util.Log.d("AppTracker", "Transferred $pendingCount pending events to queue")
        }

        isInitialized = true
        android.util.Log.d("AppTracker", "SDK initialization completed")
    }

    /**
     * Track an event.
     * Can be called before initialization - events will be queued and sent after initialization.
     * @param eventName Name of the event
     * @param properties Optional properties map
     */
    fun track(eventName: String, properties: Map<String, Any>? = null) {
        val timestamp = Instant.now().toString()
        val event = Event(
            eventName = eventName,
            timestamp = timestamp,
            properties = properties
        )

        if (isInitialized) {
            // SDK is initialized - send directly
            CoroutineScope(Dispatchers.IO).launch {
                eventQueue?.enqueue(event)
            }
        } else {
            // SDK not initialized yet - save to pending queue
            synchronized(pendingEvents) {
                pendingEvents.add(event)
            }
            android.util.Log.d("AppTracker", "Event queued (SDK not initialized yet): $eventName. Total pending: ${pendingEvents.size}")
        }
    }

    /**
     * Identify a user.
     * Can be called before initialization - will be applied after initialization.
     * @param userId User identifier
     */
    fun identify(userId: String) {
        if (isInitialized) {
            eventQueue?.setUserId(userId)
        } else {
            pendingUserId = userId
            android.util.Log.d("AppTracker", "User ID queued (SDK not initialized yet): $userId")
        }
    }

    /**
     * Set anonymous ID.
     * Can be called before initialization - will be applied after initialization.
     * @param anonymousId Anonymous identifier
     */
    fun setAnonymousId(anonymousId: String) {
        if (isInitialized) {
            eventQueue?.setAnonymousId(anonymousId)
        } else {
            pendingAnonymousId = anonymousId
            android.util.Log.d("AppTracker", "Anonymous ID queued (SDK not initialized yet): $anonymousId")
        }
    }

    /**
     * Get current user ID.
     */
    fun getUserId(): String? {
        return if (isInitialized) {
            eventQueue?.getUserId()
        } else {
            pendingUserId
        }
    }

    /**
     * Flush all pending events to the server immediately.
     * If SDK is not initialized yet, this will do nothing (events are already queued).
     */
    fun flush() {
        if (isInitialized) {
            CoroutineScope(Dispatchers.IO).launch {
                eventQueue?.flush()
            }
        } else {
            android.util.Log.d("AppTracker", "Flush called but SDK not initialized yet. Events will be sent after initialization.")
        }
    }

    /**
     * Get the number of pending events.
     * Includes both events in the queue (if initialized) and pending events (if not initialized).
     */
    suspend fun getPendingCount(): Int {
        return if (isInitialized) {
            eventQueue?.getPendingCount() ?: 0
        } else {
            synchronized(pendingEvents) {
                pendingEvents.size
            }
        }
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
     * Ensure project exists and get its key, using SharedPreferences for persistence.
     * This is the main method used by initialize().
     * 
     * @param context Application context for SharedPreferences
     * @param projectName Project name
     * @param baseUrl Base URL of the backend
     * @param providedProjectKey Optional project key provided by user (takes precedence)
     * @return Project key if exists or was created successfully, null otherwise
     */
    private suspend fun ensureProjectAndGetKeyWithContext(
        context: Context,
        projectName: String,
        baseUrl: String,
        providedProjectKey: String? = null
    ): String? {
        return withContext(Dispatchers.IO) {
            val prefs = context.getSharedPreferences("apptracker_config", Context.MODE_PRIVATE)
            
            android.util.Log.d("AppTracker", "ensureProjectAndGetKeyWithContext called with name: $projectName, baseUrl: $baseUrl")
            
            // Priority 1: Use provided project key if given
            if (!providedProjectKey.isNullOrEmpty()) {
                android.util.Log.d("AppTracker", "Using provided project key: $providedProjectKey")
                if (projectExists(providedProjectKey, baseUrl)) {
                    // Save it for next time
                    prefs.edit().putString("project_key", providedProjectKey).apply()
                    return@withContext providedProjectKey
                } else {
                    android.util.Log.w("AppTracker", "Provided project key doesn't exist, will try saved key or create new")
                }
            }
            
            // Priority 2: Check saved project key
            val savedKey = prefs.getString("project_key", null)
            if (!savedKey.isNullOrEmpty()) {
                android.util.Log.d("AppTracker", "Checking saved project key: $savedKey")
                if (projectExists(savedKey, baseUrl)) {
                    android.util.Log.d("AppTracker", "Saved project key exists, using it")
                    return@withContext savedKey
                } else {
                    android.util.Log.d("AppTracker", "Saved project key doesn't exist, will create new project")
                }
            } else {
                android.util.Log.d("AppTracker", "No saved project key, will create new project")
            }
            
            // Priority 3: Create new project
            android.util.Log.d("AppTracker", "Creating new project...")
            val newProjectKey = createProject(projectName, baseUrl)
            
            if (newProjectKey != null) {
                // Save the new project key
                prefs.edit().putString("project_key", newProjectKey).apply()
                android.util.Log.d("AppTracker", "Project created and saved: $newProjectKey")
            }
            
            newProjectKey
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

