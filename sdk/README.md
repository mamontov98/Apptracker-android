# AppTracker Android SDK

Android SDK for tracking analytics events and sending them to the AppTracker backend.

## Installation

### Via JitPack

Add JitPack repository to your root `build.gradle.kts`:

```kotlin
allprojects {
    repositories {
        maven { url = uri("https://jitpack.io") }
    }
}
```

Or in `settings.gradle.kts`:

```kotlin
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
    }
}
```

Add the dependency to your app's `build.gradle.kts`:

```kotlin
dependencies {
    implementation("com.github.YOUR_GITHUB_USERNAME:Apptracker-android:sdk:1.0.0")
}
```

**Note:** Replace `YOUR_GITHUB_USERNAME` with your actual GitHub username.

## Quick Start

### 1. Add Internet Permission

Add to your `AndroidManifest.xml`:

```xml
<uses-permission android:name="android.permission.INTERNET" />
```

### 2. Initialize the SDK

In your `Application` class:

```kotlin
import android.app.Application
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.apptracker.sdk.AppTracker
import com.apptracker.sdk.AppTrackerConfig
import kotlinx.coroutines.launch

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        
        ProcessLifecycleOwner.get().lifecycleScope.launch {
            val config = AppTrackerConfig(
                projectName = "My App",
                baseUrl = "https://your-backend.vercel.app"
            )
            AppTracker.initialize(this@MyApplication, config)
        }
    }
}
```

**Important:** The SDK will automatically:
- Check if a project with the given name exists
- Create a new project if it doesn't exist
- Save the project key for future use
- Handle all the backend communication

### 3. Track Events

```kotlin
// Track a simple event
AppTracker.track("button_click")

// Track an event with properties
AppTracker.track("screen_view", mapOf(
    "screen_name" to "HomeScreen",
    "screen_class" to "MainActivity"
))
```

### 4. Identify Users

```kotlin
// Identify a user
AppTracker.identify("user-12345")
```

### 5. Flush Events (Optional)

```kotlin
// Manually flush pending events to the server
AppTracker.flush()
```

## Configuration

### AppTrackerConfig Parameters

- **projectName** (required): Name of your project. The SDK will create or find a project with this name.
- **baseUrl** (optional): Backend server URL. Default: `"https://your-app.vercel.app"`
- **projectKey** (optional): If you already have a project key, you can provide it directly.
- **batchSize** (optional): Number of events to batch before sending. Default: `20`
- **flushInterval** (optional): Time in milliseconds between automatic flushes. Default: `30000` (30 seconds)

### Example with Custom Configuration

```kotlin
val config = AppTrackerConfig(
    projectName = "My Awesome App",
    baseUrl = "https://my-backend.vercel.app",
    batchSize = 50,
    flushInterval = 60_000L // 1 minute
)
AppTracker.initialize(context, config)
```

## Features

- ✅ **Automatic Project Management** - SDK handles project creation and key management
- ✅ **Offline Support** - Events are queued locally when offline
- ✅ **Automatic Batching** - Events are batched for efficient network usage
- ✅ **Periodic Flushing** - Events are automatically sent at intervals
- ✅ **Session Management** - Automatic session tracking
- ✅ **Anonymous ID Tracking** - Automatic anonymous user identification
- ✅ **User Identification** - Support for user identification
- ✅ **Local Storage** - Events stored locally using Room database

## How It Works

1. **Initialization**: When you call `AppTracker.initialize()`, the SDK:
   - Checks if a project key is saved locally
   - If found, verifies it exists on the server
   - If not found or invalid, creates a new project
   - Saves the project key for future use

2. **Event Tracking**: When you call `AppTracker.track()`:
   - Event is created with timestamp and metadata
   - Event is saved to local database (Room)
   - If batch size is reached, events are sent immediately
   - Otherwise, events wait for periodic flush

3. **Automatic Flushing**: Every `flushInterval` milliseconds:
   - SDK collects pending events from database
   - Sends them as a batch to the server
   - Removes sent events from database

## Requirements

- Android API 24+ (Android 7.0+)
- Kotlin
- Internet permission

## License

See main project LICENSE file.
