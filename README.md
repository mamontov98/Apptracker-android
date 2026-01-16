# AppTracker Android SDK & Demo App

Android SDK for tracking analytics events and sending them to the AppTracker backend.

**🏗️ Standalone Repository** - This is an independent repository containing the SDK and a demo application.

## Project Structure

```
android/
├── sdk/          # SDK library module
└── app/          # Demo application
```

## Setup

### Prerequisites

- Android Studio Hedgehog (2023.1.1) or later
- JDK 8 or later
- Android SDK (API 24+)

### Opening the Project

1. Open Android Studio
2. Select "Open an Existing Project"
3. Navigate to the `android` directory
4. Click "OK"

Android Studio will automatically sync the Gradle files and download dependencies.

## SDK Usage

### 1. Initialize the SDK

```kotlin
import com.apptracker.sdk.AppTracker
import com.apptracker.sdk.AppTrackerConfig

// In your Application class or Activity
val config = AppTrackerConfig(
    projectKey = "your-project-key",
    baseUrl = "http://10.0.2.2:5000", // For emulator
    batchSize = 20,
    flushInterval = 30_000L // 30 seconds
)

AppTracker.initialize(context, config)
```

### 2. Track Events

```kotlin
// Track a simple event
AppTracker.track("button_click")

// Track an event with properties
AppTracker.track("screen_view", mapOf(
    "screen_name" to "HomeScreen",
    "screen_class" to "MainActivity"
))
```

### 3. Identify Users

```kotlin
// Identify a user
AppTracker.identify("user-12345")
```

### 4. Flush Events

```kotlin
// Manually flush pending events
AppTracker.flush()
```

## Configuration

### Base URL

- **Development (Android Emulator)**: Use `http://10.0.2.2:5000` (maps to host's localhost)
- **Development (Physical Device)**: Use your computer's IP address (e.g., `http://192.168.1.100:5000`)
- **Production (Cloud)**: Use your production server URL (e.g., `https://apptracker-backend.herokuapp.com`)

**Important for Production:**
- Always use HTTPS in production
- Use the cloud-deployed backend URL (provided by your AppTracker service)
- Update `AppTrackerConfig.baseUrl` to point to your production backend

### Batch Settings

- `batchSize`: Number of events to batch before sending (default: 20)
- `flushInterval`: Time in milliseconds between automatic flushes (default: 30000)

## Features

- ✅ Offline event queuing
- ✅ Automatic batching
- ✅ Periodic flushing
- ✅ Session management
- ✅ Anonymous ID tracking
- ✅ User identification
- ✅ Local storage with Room

## Demo App

The `app` module contains a demo application that demonstrates SDK usage:

1. Initialize SDK with project key
2. Track custom events
3. Identify users
4. Flush events manually
5. View SDK status and pending event count

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

## User Guide

**📖 New to the SDK?** Check out the complete [SDK Usage Guide](SDK_USAGE_GUIDE.md) for step-by-step instructions on:
- Adding the SDK to your project
- Initializing the SDK
- Tracking events
- Common use cases
- Troubleshooting

## API Reference

See [API_SPEC.md](../API_SPEC.md) for the backend API specification.

## Troubleshooting

### Events Not Sending

1. Check that the backend is running
2. Verify the base URL is correct
3. For emulator, use `http://10.0.2.2:5000`
4. For physical device, use your computer's IP address
5. Check AndroidManifest.xml has INTERNET permission

### CORS Errors

CORS is only for browser requests. Android apps make direct HTTP requests, so CORS shouldn't be an issue. If you see CORS errors, they're likely from the frontend, not the Android app.

## License

See main project LICENSE file.

