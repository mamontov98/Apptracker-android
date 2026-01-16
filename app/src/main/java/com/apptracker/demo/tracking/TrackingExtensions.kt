package com.apptracker.demo.tracking

import android.app.Activity
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.apptracker.sdk.AppTracker

/**
 * Extension function to track screen view automatically
 */
fun AppCompatActivity.trackScreenView(screenName: String) {
    if (AppTracker.isInitialized()) {
        AppTracker.track("screen_view", mapOf(
            "screen_name" to screenName,
            "screen_class" to this::class.java.simpleName
        ))
        android.util.Log.d("AppTracker", "Tracked screen_view: $screenName")
    }
}

/**
 * Extension function to set click listener with tracking annotation support
 * This allows using annotations on methods that are called from click listeners
 */
fun View.setOnClickListenerWithTracking(
    activity: Activity,
    methodName: String,
    vararg args: Any?
) {
    setOnClickListener {
        try {
            val method = activity::class.java.getDeclaredMethod(methodName, *args.map { 
                it?.javaClass ?: Any::class.java 
            }.toTypedArray())
            method.isAccessible = true
            TrackingInterceptor.processMethod(activity, method, *args)
            method.invoke(activity, *args)
        } catch (e: Exception) {
            android.util.Log.e("AppTracker", "Error in setOnClickListenerWithTracking: ${e.message}", e)
        }
    }
}
