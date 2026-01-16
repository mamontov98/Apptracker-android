# 🚀 Deployment Guide - AppTracker Android

This guide covers preparing the Android SDK and app for production use with a cloud-deployed backend.

## 📋 Overview

The Android SDK connects to the AppTracker backend API to send analytics events. This guide shows how to configure the SDK to work with a cloud-deployed backend.

## 🔧 Configuration for Cloud Backend

### Step 1: Get Your Backend URL

First, deploy your backend to a cloud service (see backend DEPLOYMENT.md):
- Heroku: `https://your-app.herokuapp.com`
- Railway: `https://your-app.railway.app`
- Render: `https://your-app.onrender.com`
- Custom domain: `https://api.apptracker.com`

### Step 2: Update SDK Configuration

#### Option 1: Edit AppTrackerApplication.kt (Recommended)

Update the `getBaseUrl()` function in your Application class:

```kotlin
private fun getBaseUrl(): String {
    return "https://apptracker-backend.herokuapp.com" // Your cloud backend URL
}
```

#### Option 2: Use Build Config (Best for Production)

1. **Add to `app/build.gradle.kts`:**

```kotlin
android {
    defaultConfig {
        buildConfigField("String", "APP_TRACKER_BASE_URL", "\"https://apptracker-backend.herokuapp.com\"")
    }
}
```

2. **Use in Application class:**

```kotlin
private fun getBaseUrl(): String {
    return BuildConfig.APP_TRACKER_BASE_URL
}
```

#### Option 3: Environment-based Configuration

For different environments (dev/staging/prod):

```kotlin
android {
    buildTypes {
        debug {
            buildConfigField("String", "APP_TRACKER_BASE_URL", "\"http://10.0.2.2:5000\"")
        }
        release {
            buildConfigField("String", "APP_TRACKER_BASE_URL", "\"https://apptracker-backend.herokuapp.com\"")
        }
    }
}
```

### Step 3: Ensure HTTPS Support

**Important:** Android requires proper SSL certificates for HTTPS connections:

1. **Production backend must use HTTPS**
2. **Valid SSL certificate required**
3. **Android will reject self-signed certificates** (unless configured to trust them)

If you're using a cloud service (Heroku, Railway, Render), they provide valid SSL certificates automatically.

### Step 4: Update Default SDK Config

Edit `sdk/src/main/java/com/apptracker/sdk/AppTrackerConfig.kt`:

```kotlin
data class AppTrackerConfig(
    val projectKey: String,
    val baseUrl: String = "https://apptracker-backend.herokuapp.com", // Default to cloud
    val batchSize: Int = 20,
    val flushInterval: Long = 30_000L
)
```

**Note:** The demo app overrides this in `AppTrackerApplication.kt`, so update that file instead.

## 🧪 Testing with Cloud Backend

1. **Deploy backend to cloud**
2. **Update `baseUrl` in your app**
3. **Test on emulator** (can still use `http://10.0.2.2:5000` for local backend)
4. **Test on physical device** (must use cloud URL)
5. **Verify events arrive** in backend dashboard

## 📱 Publishing Your App

### Building Release APK/AAB

```bash
# Build release APK
./gradlew assembleRelease

# Build release AAB (for Google Play)
./gradlew bundleRelease
```

### Before Publishing Checklist

- [ ] Backend deployed to cloud with HTTPS
- [ ] `baseUrl` points to cloud backend
- [ ] Tested on physical device
- [ ] Events successfully sent to cloud backend
- [ ] No hardcoded localhost URLs
- [ ] Network security config allows HTTPS (if needed)

### Network Security Config (if needed)

If you need to allow specific certificates or configurations, create `res/xml/network_security_config.xml`:

```xml
<?xml version="1.0" encoding="utf-8"?>
<network-security-config>
    <base-config cleartextTrafficPermitted="false">
        <trust-anchors>
            <certificates src="system" />
        </trust-anchors>
    </base-config>
</network-security-config>
```

Then reference in `AndroidManifest.xml`:

```xml
<application
    android:networkSecurityConfig="@xml/network_security_config"
    ...>
```

**Note:** Cloud services provide valid certificates, so this is usually not needed.

## 🔍 Troubleshooting

### Events Not Reaching Cloud Backend

1. **Check baseUrl**: Verify it's correct and uses HTTPS
2. **Check network**: Device must have internet connection
3. **Check logs**: `adb logcat | grep AppTracker`
4. **Check backend**: Verify backend is accessible (open URL in browser)
5. **Check CORS**: Backend CORS should allow Android app (though Android doesn't use CORS, verify backend is up)

### SSL/Certificate Errors

1. **Use HTTPS**: Never use HTTP in production
2. **Valid certificate**: Cloud services provide valid certificates
3. **Check date/time**: Device date/time must be correct for SSL validation

### Connection Timeout

1. **Check backend URL**: Must be correct and accessible
2. **Check internet**: Device must have internet connection
3. **Check firewall**: Network must allow HTTPS connections
4. **Increase timeout**: Update `ApiClient.kt` timeout settings if needed

## 📚 Additional Resources

- [Android Network Security Config](https://developer.android.com/training/articles/security-config)
- [Android HTTPS Best Practices](https://developer.android.com/training/articles/security-ssl)
- Backend deployment guides in backend/DEPLOYMENT.md

## ✅ Production Checklist

- [ ] Backend deployed to cloud
- [ ] Backend URL configured in app
- [ ] HTTPS enabled (no HTTP in production)
- [ ] Tested on physical device
- [ ] Events successfully sent to cloud
- [ ] Release build created
- [ ] Google Play Console configured (if publishing to Play Store)
