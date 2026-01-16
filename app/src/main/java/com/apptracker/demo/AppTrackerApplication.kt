package com.apptracker.demo

import android.app.Application
import com.apptracker.sdk.AppTracker
import com.apptracker.sdk.AppTrackerConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AppTrackerApplication : Application() {
    
    override fun onCreate() {
        super.onCreate()
        
        // Initialize SDK automatically in the background
        initializeSDK()
    }
    
    private fun initializeSDK() {
        val baseUrl = getBaseUrl()
        val projectName = getProjectName()
        
        android.util.Log.d("AppTracker", "Starting SDK initialization...")
        android.util.Log.d("AppTracker", "Base URL: $baseUrl")
        android.util.Log.d("AppTracker", "Project Name: $projectName")
        
        // Ensure project exists and get/create project key
        CoroutineScope(Dispatchers.IO).launch {
            try {
                android.util.Log.d("AppTracker", "Ensuring project exists...")
                val projectKey = ensureProject(projectName, baseUrl)
                
                if (projectKey != null) {
                    android.util.Log.d("AppTracker", "Got project key: $projectKey")
                    val config = AppTrackerConfig(
                        projectKey = projectKey,
                        baseUrl = baseUrl,
                        batchSize = 20,
                        flushInterval = 30_000L // 30 seconds
                    )
                    
                    AppTracker.initialize(this@AppTrackerApplication, config)
                    android.util.Log.d("AppTracker", "SDK initialized successfully with projectKey: $projectKey")
                } else {
                    android.util.Log.e("AppTracker", "Failed to get project key, SDK not initialized")
                }
            } catch (e: Exception) {
                // SDK initialization failed - log but don't crash
                android.util.Log.e("AppTracker", "Error initializing SDK: ${e.message}", e)
                e.printStackTrace()
            }
        }
    }
    
    /**
     * Ensure project exists in backend, create if it doesn't.
     * Returns the project key to use.
     */
    private suspend fun ensureProject(projectName: String, baseUrl: String): String? {
        val prefs = getSharedPreferences("apptracker_config", MODE_PRIVATE)
        
        try {
            android.util.Log.d("AppTracker", "Checking for saved project key...")
            // Get saved project key if exists
            val savedKey = prefs.getString("project_key", null)
            if (savedKey != null) {
                android.util.Log.d("AppTracker", "Found saved project key: $savedKey")
            } else {
                android.util.Log.d("AppTracker", "No saved project key found")
            }
            
            android.util.Log.d("AppTracker", "Calling ensureProjectAndGetKey...")
            // Ensure project exists and get key
            val projectKey = AppTracker.ensureProjectAndGetKey(
                projectName = projectName,
                baseUrl = baseUrl,
                savedProjectKey = savedKey
            )
            
            if (projectKey != null) {
                android.util.Log.d("AppTracker", "Got project key: $projectKey, saving...")
                // Save the project key for next time
                prefs.edit().putString("project_key", projectKey).apply()
                return projectKey
            } else {
                android.util.Log.e("AppTracker", "ensureProjectAndGetKey returned null")
            }
        } catch (e: Exception) {
            android.util.Log.e("AppTracker", "Error in ensureProject: ${e.message}", e)
            e.printStackTrace()
        }
        
        return null
    }
    
    private fun getProjectName(): String {
        // TODO: Change this to your project name
        // In production, load from build config, environment variables, or remote config
        return "Android Demo App"
    }
    
    private fun getBaseUrl(): String {
        // Try to get from SharedPreferences first
        val prefs = getSharedPreferences("apptracker_config", MODE_PRIVATE)
        val savedUrl = prefs.getString("base_url", null)
        if (!savedUrl.isNullOrEmpty()) {
            return savedUrl
        }
        
        // TODO: Change this to your backend URL
        // For emulator: http://10.0.2.2:5000
        // For physical device: http://YOUR_COMPUTER_IP:5000
        // For production: https://your-api-domain.com
        return "http://10.0.2.2:5000"
    }
}

