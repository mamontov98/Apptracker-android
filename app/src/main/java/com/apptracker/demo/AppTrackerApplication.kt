package com.apptracker.demo

import android.app.Application
import com.apptracker.sdk.AppTracker
import com.apptracker.sdk.AppTrackerConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class AppTrackerApplication : Application() {
    
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    
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
        
        // SDK will automatically handle project creation/retrieval
        applicationScope.launch {
            try {
                val config = AppTrackerConfig(
                    projectName = projectName,
                    baseUrl = baseUrl,
                    batchSize = 20,
                    flushInterval = 30_000L // 30 seconds
                )
                
                AppTracker.initialize(this@AppTrackerApplication, config)
                android.util.Log.d("AppTracker", "SDK initialized successfully")
            } catch (e: Exception) {
                // SDK initialization failed - log but don't crash
                android.util.Log.e("AppTracker", "Error initializing SDK: ${e.message}", e)
                e.printStackTrace()
            }
        }
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
        
        // Production URL - Vercel deployment
        // For local development (emulator): http://10.0.2.2:5000
        // For local development (physical device): http://YOUR_COMPUTER_IP:5000
        return "https://apptracker-backend.vercel.app"
    }
}

