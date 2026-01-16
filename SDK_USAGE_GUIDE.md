# AppTracker Android SDK - Installation & Usage Guide

Simple step-by-step guide for installing and using the AppTracker SDK in your Android app.

---

## Step 1: Add SDK to Your Project

### Copy the SDK Module

1. **Copy the `sdk` folder** to your Android project:
   ```
   YourProject/
   ├── app/
   ├── sdk/          ← Copy SDK folder here
   └── build.gradle.kts
   ```

### Add to Gradle

2. **Open `settings.gradle.kts`** and add:
   ```kotlin
   include(":app")
   include(":sdk")  // Add this line
   ```

3. **Open `app/build.gradle.kts`** and add dependency:
   ```kotlin
   dependencies {
       implementation(project(":sdk"))  // Add this line
       
       // ... your other dependencies
   }
   ```

4. **Sync Gradle** (Android Studio will prompt you, or click "Sync Now")

---

## Step 2: Add Internet Permission

Open `app/src/main/AndroidManifest.xml` and add:

```xml
<manifest xmlns:android="http://schemas.android.com/apk/res/android">
    <!-- Add this line -->
    <uses-permission android:name="android.permission.INTERNET" />
    
    <application>
        <!-- ... -->
    </application>
</manifest>
```

---

## Step 3: Create Application Class

If you don't have one, create `MyApplication.kt`:

```kotlin
package com.yourpackage.yourapp

import android.app.Application

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
    }
}
```

**Register it in `AndroidManifest.xml`**:

```xml
<application
    android:name=".MyApplication"  <!-- Add this -->
    ...>
    <!-- Your activities -->
</application>
```

---

## Step 4: Initialize the SDK

The SDK automatically creates a project and handles everything. You just need to provide your app name and the AppTracker service URL.

### Simple Initialization

**In `MyApplication.kt`**, add this code:

```kotlin
package com.yourpackage.yourapp

import android.app.Application
import com.apptracker.sdk.AppTracker
import com.apptracker.sdk.AppTrackerConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        
        // Initialize SDK
        initializeAppTracker()
    }
    
    private fun initializeAppTracker() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Your app name (SDK will create project automatically)
                val projectName = "My Awesome App"  // ← Change this to your app name
                
                // AppTracker service URL (provided by AppTracker team)
                val baseUrl = "https://api.apptracker.com"  // ← Use the URL provided by AppTracker
                
                // The SDK will automatically create/get project key
                val projectKey = AppTracker.ensureProjectAndGetKey(
                    projectName = projectName,
                    baseUrl = baseUrl,
                    savedProjectKey = null
                )
                
                if (projectKey != null) {
                    // Save project key for next time
                    val prefs = getSharedPreferences("apptracker_config", MODE_PRIVATE)
                    prefs.edit().putString("project_key", projectKey).apply()
                    
                    // Initialize SDK
                    val config = AppTrackerConfig(
                        projectKey = projectKey,
                        baseUrl = baseUrl,
                        batchSize = 20,
                        flushInterval = 30_000L  // 30 seconds
                    )
                    
                    AppTracker.initialize(this@MyApplication, config)
                    android.util.Log.d("AppTracker", "✅ SDK initialized successfully!")
                }
            } catch (e: Exception) {
                android.util.Log.e("AppTracker", "❌ Failed to initialize SDK: ${e.message}", e)
            }
        }
    }
}
```

**That's it!** The SDK will automatically:
- ✅ Create a project in AppTracker service (first time only)
- ✅ Save the project key for future use
- ✅ Start tracking events

---

## Step 5: Use the SDK

### Track an Event

In any Activity, Fragment, or anywhere in your app:

```kotlin
import com.apptracker.sdk.AppTracker

// Simple event
AppTracker.track("button_click")

// Event with properties
AppTracker.track("screen_view", mapOf(
    "screen_name" to "Home",
    "screen_class" to "MainActivity"
))
```

### Check if SDK is Ready

Before tracking, check if SDK is initialized:

```kotlin
if (AppTracker.isInitialized()) {
    AppTracker.track("my_event")
} else {
    // SDK not ready yet, wait a bit
    android.util.Log.w("AppTracker", "SDK not initialized yet")
}
```

---

## Configuration

### AppTracker Service URL (baseUrl)

The `baseUrl` is the URL of the AppTracker service (provided by AppTracker team):

```kotlin
val baseUrl = "https://api.apptracker.com"  // Provided by AppTracker team
```

**Important**: 
- Use the URL provided by AppTracker
- Must use `https://` (secure connection)
- The URL should not have a trailing slash (no `/` at the end)

### App Name

The `projectName` is used to create/identify your project in AppTracker:

```kotlin
val projectName = "My Awesome App"  // Your app name
```

**Note**: The SDK automatically saves the project key after first creation, so you don't need to worry about it!

---

## Complete Example

Here's a complete working example:

### MyApplication.kt

```kotlin
package com.example.myapp

import android.app.Application
import com.apptracker.sdk.AppTracker
import com.apptracker.sdk.AppTrackerConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Your app name (SDK will create project automatically)
                val projectName = "My App"
                
                // AppTracker service URL (provided by AppTracker)
                val baseUrl = "https://api.apptracker.com"
                
                // SDK automatically creates/gets project
                val projectKey = AppTracker.ensureProjectAndGetKey(
                    projectName = projectName,
                    baseUrl = baseUrl,
                    savedProjectKey = null
                )
                
                projectKey?.let {
                    // Save for next time
                    getSharedPreferences("apptracker_config", MODE_PRIVATE)
                        .edit()
                        .putString("project_key", it)
                        .apply()
                    
                    // Initialize SDK
                    val config = AppTrackerConfig(
                        projectKey = it,
                        baseUrl = baseUrl,
                        batchSize = 20,
                        flushInterval = 30_000L
                    )
                    
                    AppTracker.initialize(this@MyApplication, config)
                    android.util.Log.d("AppTracker", "SDK initialized!")
                }
            } catch (e: Exception) {
                android.util.Log.e("AppTracker", "Error: ${e.message}", e)
            }
        }
    }
}
```

### AndroidManifest.xml

```xml
<manifest xmlns:android="http://schemas.android.com/apk/res/android">
    <uses-permission android:name="android.permission.INTERNET" />
    
    <application
        android:name=".MyApplication"
        ...>
        <activity android:name=".MainActivity" ... />
    </application>
</manifest>
```

### MainActivity.kt

```kotlin
package com.example.myapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.apptracker.sdk.AppTracker

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        // Track screen view
        if (AppTracker.isInitialized()) {
            AppTracker.track("screen_view", mapOf(
                "screen_name" to "Main"
            ))
        }
        
        // Track button click
        findViewById<Button>(R.id.myButton).setOnClickListener {
            if (AppTracker.isInitialized()) {
                AppTracker.track("button_click", mapOf(
                    "button_id" to "my_button",
                    "button_text" to "Click Me"
                ))
            }
            
            // Your button logic here
        }
    }
}
```

---

## Common Event Examples

### Screen View (Track when screen opens)

```kotlin
class ProductActivity : AppCompatActivity() {
    override fun onResume() {
        super.onResume()
        
        if (AppTracker.isInitialized()) {
            AppTracker.track("screen_view", mapOf(
                "screen_name" to "ProductDetails",
                "screen_class" to "ProductActivity"
            ))
        }
    }
}
```

### Button Click

```kotlin
button.setOnClickListener {
    if (AppTracker.isInitialized()) {
        AppTracker.track("button_click", mapOf(
            "button_id" to "add_to_cart",
            "button_text" to "Add to Cart",
            "screen_name" to "ProductActivity"
        ))
    }
    
    // Your button logic
    addToCart()
}
```

### View Product

```kotlin
fun showProduct(product: Product) {
    if (AppTracker.isInitialized()) {
        AppTracker.track("view_item", mapOf(
            "item_id" to product.id,
            "item_name" to product.name,
            "item_price" to product.price,
            "item_category" to product.category
        ))
    }
    
    // Show product
    navigateToProduct(product)
}
```

### Add to Cart

```kotlin
fun addToCart(product: Product, quantity: Int = 1) {
    if (AppTracker.isInitialized()) {
        AppTracker.track("add_to_cart", mapOf(
            "item_id" to product.id,
            "item_name" to product.name,
            "item_price" to product.price,
            "quantity" to quantity
        ))
    }
    
    // Add to cart logic
    cartManager.addItem(product, quantity)
    showToast("Added to cart!")
}
```

### Purchase

```kotlin
fun completePurchase(order: Order) {
    if (AppTracker.isInitialized()) {
        AppTracker.track("purchase_success", mapOf(
            "order_id" to order.id,
            "order_value" to order.total,
            "item_count" to order.items.size,
            "currency" to "USD"
        ))
    }
    
    // Complete purchase
    orderService.confirmOrder(order)
}
```

---

## Advanced: Additional Features

### Identify User

When a user logs in, identify them:

```kotlin
AppTracker.identify("user-12345")
```

All future events will be associated with this user ID.

### Set Anonymous ID

For anonymous users (before login):

```kotlin
AppTracker.setAnonymousId("anon-67890")
```

The SDK automatically generates an anonymous ID if you don't set one.

### Manual Flush

Force immediate send of all pending events:

```kotlin
AppTracker.flush()
```

**Note**: Usually not needed - SDK sends automatically every 30 seconds or when batch size is reached.

### Check Pending Events

Check how many events are queued:

```kotlin
lifecycleScope.launch {
    val count = AppTracker.getPendingCount()
    Log.d("AppTracker", "Pending events: $count")
}
```

### Custom Configuration

If you need to customize batch settings:

```kotlin
val config = AppTrackerConfig(
    projectKey = projectKey,
    baseUrl = baseUrl,
    batchSize = 20,           // Send 20 events at once (default)
    flushInterval = 30_000L   // Auto-send every 30 seconds (default)
)
```

---

## Troubleshooting

### SDK Not Initialized

**Problem**: Getting "SDK not initialized" warnings

**Solution**:
- Make sure you're initializing in `Application.onCreate()`
- Wait a bit after app starts (initialization is async)
- Check Logcat: `adb logcat | grep AppTracker`
- Make sure service URL is correct and accessible

### Events Not Sending

**Problem**: Events aren't appearing in AppTracker dashboard

**Solutions**:
1. **Check service URL**: Make sure `baseUrl` is correct (provided by AppTracker)
2. **Check internet permission** in `AndroidManifest.xml`
3. **Check network connectivity**: Make sure device has internet connection
4. **Check Logcat**: `adb logcat | grep AppTracker` for errors
5. **Wait for batch**: Events are sent every 30 seconds or when 20 events are queued

### Cannot Connect to Service

**Problem**: SDK can't connect to AppTracker service

**Solutions**:
1. **Check service URL**: Verify `baseUrl` is correct (e.g., `https://api.apptracker.com`)
2. **Check SSL certificate**: Service must have valid HTTPS certificate
3. **Check network**: Make sure device has internet connection
4. **Check firewall**: Make sure device/network allows HTTPS connections
5. **Check Logcat**: `adb logcat | grep AppTracker` for connection errors
6. **Contact AppTracker support**: If service is down or URL is incorrect

### Project Not Created

**Problem**: Project key is null after initialization

**Solutions**:
1. Check service URL is correct and accessible
2. Check Logcat for errors: `adb logcat | grep AppTracker`
3. Try restarting the app (project key is saved after first creation)
4. Make sure service is online and accessible
5. Contact AppTracker support if issue persists

---

## Quick Checklist

Before using the SDK:

- [ ] SDK module added to project (`settings.gradle.kts` and `app/build.gradle.kts`)
- [ ] Internet permission added to `AndroidManifest.xml`
- [ ] Application class created and registered
- [ ] SDK initialized in `Application.onCreate()`
- [ ] App name set (will be used as project name)
- [ ] AppTracker service URL set (provided by AppTracker team)
- [ ] Device has internet connection

---

## That's It! 🎉

You're done! The SDK will automatically:
- ✅ Create a project in AppTracker service (first time)
- ✅ Save the project key for future use
- ✅ Send events automatically every 30 seconds
- ✅ Queue events when offline
- ✅ Retry sending failed events

Just use `AppTracker.track("event_name")` anywhere in your app!

View your analytics in the AppTracker dashboard using the service URL provided by AppTracker team.

---

## Need Help?

- Check Logcat: `adb logcat | grep AppTracker`
- Verify service is accessible: Open service URL in browser
- Contact AppTracker support if you need the service URL or have issues
- Review the SDK code in the `sdk` folder for more details

**Happy Tracking! 📊**
