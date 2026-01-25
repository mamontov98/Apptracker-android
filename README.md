# AppTracker Android SDK & Demo App

Android SDK for tracking analytics events and sending them to the AppTracker backend.

**🏗️ Standalone Repository** - This is an independent repository containing the SDK and a demo application.

## What is the SDK?

The **AppTracker SDK** is a lightweight Android library that automatically tracks analytics events in your app and sends them to the AppTracker backend. 

### What the SDK Does Behind the Scenes:

- ✅ **Automatic Project Management** - Creates/finds your project on the server automatically by name
- ✅ **Offline Support** - Events are queued locally when offline, sent when connection is restored
- ✅ **Automatic Batching** - Groups events together for efficient network usage
- ✅ **Periodic Flushing** - Automatically sends events every 30 seconds (configurable)
- ✅ **Session Management** - Tracks user sessions automatically
- ✅ **Anonymous ID Tracking** - Automatically generates and tracks anonymous user IDs
- ✅ **User Identification** - Links events to specific users
- ✅ **Local Storage** - Uses Room database to store events locally
- ✅ **Smart Queue** - Events can be tracked even before SDK initialization completes

**You don't need to know about any of this!** Just initialize and track events - the SDK handles everything automatically.

## Installation

### Via JitPack

**Step 1: Add JitPack repository**

In your `settings.gradle.kts` (or root `build.gradle`):

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

**Step 2: Add dependency**

In your app's `build.gradle.kts`:

```kotlin
dependencies {
    implementation("com.github.mamontov98:Apptracker-android:v1.0.5")
}
```

**Note:** Use `v1.0.5` (with `v`) because the Git tag is `v1.0.5`. For other versions, check the tag name in your repository.

**Step 3: Add Internet permission**

In your `AndroidManifest.xml`:

```xml
<uses-permission android:name="android.permission.INTERNET" />
```

That's it! You're ready to use the SDK.

## Quick Start

### 1. Initialize the SDK

Create an `Application` class (if you don't have one):

```kotlin
import android.app.Application
import com.apptracker.sdk.AppTracker
import com.apptracker.sdk.AppTrackerConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class MyApplication : Application() {
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    
    override fun onCreate() {
        super.onCreate()
        
        applicationScope.launch {
            val config = AppTrackerConfig(
                projectName = "My App",
                baseUrl = "https://apptracker-backend.vercel.app"
            )
            AppTracker.initialize(this@MyApplication, config)
        }
    }
}
```

Register it in `AndroidManifest.xml`:

```xml
<application
    android:name=".MyApplication"
    ...>
</application>
```

**That's it!** The SDK will automatically:
- Check if your project exists on the server
- Create it if it doesn't exist
- Save the project key for future use
- Start tracking events

### 2. Track Events

You can start tracking immediately - even before initialization completes!

```kotlin
// Track a simple event
AppTracker.track("button_click")

// Track an event with properties
AppTracker.track("screen_view", mapOf(
    "screen_name" to "HomeScreen",
    "screen_class" to "MainActivity"
))

// Track purchase
AppTracker.track("purchase", mapOf(
    "product_id" to "123",
    "price" to 99.99,
    "currency" to "USD"
))
```

### 3. Identify Users (Optional)

```kotlin
// When user logs in
AppTracker.identify("user-12345")
```

### 4. Track Process Events (Optional)

Process tracking allows you to track multiple processes/flows per user, which is useful for scenarios like:
- Multiple checkout sessions per user
- Parallel onboarding flows
- Multiple workflows running simultaneously

```kotlin
// Start a process (e.g., checkout)
val processId = UUID.randomUUID().toString()
AppTracker.trackProcess(
    eventName = "checkout_started",
    processName = "checkout",
    processId = processId,
    processStep = "START",
    properties = mapOf(
        "cart_value" to 99.99,
        "item_count" to 3
    )
)

// End a process (e.g., purchase completed)
AppTracker.trackProcess(
    eventName = "purchase_success",
    processName = "checkout",
    processId = processId,  // Same processId as START
    processStep = "END",
    properties = mapOf(
        "order_id" to "order-12345",
        "total_amount" to 99.99
    )
)
```

**Important:** 
- `processStep` must be either `"START"` or `"END"`
- Use the same `processId` for START and END events of the same process instance
- `processName` should be consistent across related events (e.g., "checkout", "onboarding")

### 5. Flush Events (Optional)

```kotlin
// Manually flush pending events to the server
AppTracker.flush()
```

## Configuration

### AppTrackerConfig Parameters

- **projectName** (required): Name of your project. The SDK will create or find a project with this name.
- **baseUrl** (optional): Backend server URL. Default: `"https://apptracker-backend.vercel.app"`
- **projectKey** (optional): If you already have a project key, you can provide it directly.
- **batchSize** (optional): Number of events to batch before sending. Default: `20`
- **flushInterval** (optional): Time in milliseconds between automatic flushes. Default: `30000` (30 seconds)

### Example with Custom Configuration

```kotlin
val config = AppTrackerConfig(
    projectName = "My Awesome App",
    baseUrl = "https://apptracker-backend.vercel.app",
    batchSize = 50,
    flushInterval = 60_000L // 1 minute
)
AppTracker.initialize(context, config)
```

## What Does the Demo App Demonstrate?

The `app` module contains a complete e-commerce demo application that shows:

### 1. SDK Initialization
- How to set up the SDK in your Application class (`AppTrackerApplication`)
- Automatic initialization in the background
- Error handling during initialization

### 2. Event Tracking Examples

The demo app tracks various user actions automatically:

#### Screen Views
- **Home Screen** - When user opens the home screen
- **Product Details** - When user views a product
- **Cart Screen** - When user opens the shopping cart
- **Profile Screen** - When user opens their profile

#### User Actions
- **Button Clicks** - Navigation buttons, "View Details", "Add to Cart", "Checkout"
- **Product Views** - When user views product details
- **Add to Cart** - When user adds a product to cart
- **Remove from Cart** - When user removes a product from cart
- **Checkout Started** - When user starts the checkout process
- **Purchase Initiated** - When user clicks "Buy Now"

#### E-commerce Events
- **View Item** - Product details with product ID, name, and price
- **Add to Cart** - Product added to cart with full product information
- **Remove from Cart** - Product removed from cart
- **View Cart** - Cart view with item count and total value
- **Checkout Started** - Checkout initiation with total amount
- **Purchase Initiated** - Purchase flow started

### 3. Real-world Usage

The demo app demonstrates a complete shopping flow:
- **Product Catalog** - Browse products on the home screen
- **Product Details** - View detailed product information
- **Shopping Cart** - Add/remove items, view total
- **Checkout** - Start checkout process
- **Navigation** - Bottom navigation between screens

### 4. Automatic Tracking with Annotations

The demo app uses custom annotations to automatically track events:

```kotlin
@TrackScreenView(screenName = "Home")
override fun onResume() { ... }

@TrackButtonClick(buttonId = "add_to_cart", buttonText = "Add to Cart")
@TrackAddToCart
private fun onAddToCartClick(product: Product) { ... }
```

### 5. User Identification

- Example of how to identify users when they log in
- Anonymous ID tracking (automatic)

## Project Structure

```
Apptracker-android/
├── sdk/          # SDK library module (published to JitPack)
│   ├── src/main/java/com/apptracker/sdk/
│   │   ├── AppTracker.kt           # Main SDK class
│   │   ├── AppTrackerConfig.kt     # Configuration
│   │   ├── Event.kt                # Event model
│   │   ├── network/                # Network layer (Retrofit)
│   │   ├── queue/                  # Event queue management
│   │   └── storage/                # Local storage (Room)
│   └── build.gradle.kts
│
└── app/          # Demo application
    ├── src/main/java/com/apptracker/demo/
    │   ├── AppTrackerApplication.kt    # SDK initialization
    │   ├── ui/                         # Activities (Home, Cart, Details, Profile)
    │   ├── data/                       # Models and managers
    │   ├── tracking/                   # Tracking annotations and interceptors
    │   └── annotations/                # Custom tracking annotations
    └── build.gradle.kts
```

## Features

- ✅ **Zero Configuration** - Just provide project name and base URL
- ✅ **Automatic Project Management** - SDK handles project creation and key management, automatically finds existing projects by name
- ✅ **Works Before Initialization** - Track events immediately, SDK queues them automatically
- ✅ **Offline Support** - Events are queued locally when offline
- ✅ **Automatic Batching** - Events are batched for efficient network usage
- ✅ **Periodic Flushing** - Events are automatically sent at intervals
- ✅ **Session Management** - Automatic session tracking
- ✅ **Anonymous ID Tracking** - Automatic anonymous user identification
- ✅ **User Identification** - Support for user identification
- ✅ **Local Storage** - Events stored locally using Room database

## How It Works

1. **Initialization**: When you call `AppTracker.initialize()`, the SDK:
   - Checks if a project key is saved locally (Priority 1)
   - If found, verifies it exists on the server (Priority 2)
   - If not found, searches for existing project by name (Priority 3)
   - If no project found, creates a new project (Priority 4)
   - Saves the project key for future use
   - Transfers any events tracked before initialization
   
   **Important:** All devices using the same `projectName` will automatically use the same project, ensuring unified analytics across all users.

2. **Event Tracking**: When you call `AppTracker.track()`:
   - If SDK is initialized: Event is saved to local database (Room)
   - If SDK not initialized yet: Event is saved to pending queue
   - If batch size is reached, events are sent immediately
   - Otherwise, events wait for periodic flush

3. **Automatic Flushing**: Every `flushInterval` milliseconds:
   - SDK collects pending events from database
   - Sends them as a batch to the server
   - Removes sent events from database

## Building

### Build SDK Library

```bash
./gradlew :sdk:assembleRelease
```

### Build Demo App

```bash
./gradlew :app:assembleDebug
```

### Install Demo App

```bash
./gradlew :app:installDebug
```

## Requirements

- Android API 24+ (Android 7.0+)
- Kotlin
- Internet permission

## Troubleshooting

### Events Not Sending

1. Check that the backend is running at `https://apptracker-backend.vercel.app`
2. Verify the base URL is correct in your config
3. Check AndroidManifest.xml has INTERNET permission
4. Check Logcat for SDK initialization messages

### SDK Not Initializing

1. Make sure you have an Application class registered in AndroidManifest.xml
2. Check that you're calling `AppTracker.initialize()` in `Application.onCreate()`
3. Verify your baseUrl is accessible
4. Check Logcat for error messages

### Dependency Not Found

If you get "Could not find" error with JitPack:

1. Make sure JitPack repository is added to `settings.gradle.kts`
2. Try syncing Gradle files (File → Sync Project with Gradle Files)
3. Check the JitPack build status: https://jitpack.io/#mamontov98/Apptracker-android
4. Verify the version number (use the latest version with green checkmark)

## License

See main project LICENSE file.
