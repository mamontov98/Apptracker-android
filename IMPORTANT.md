# Important Setup Notes

## Gradle Wrapper JAR

The `gradle/wrapper/gradle-wrapper.jar` file is a binary file that needs to be downloaded separately. 

**Option 1 (Recommended)**: When you open the project in Android Studio, it will automatically download the Gradle wrapper files.

**Option 2**: Download manually:
```bash
cd android
curl -L https://raw.githubusercontent.com/gradle/gradle/v8.2.0/gradle/wrapper/gradle-wrapper.jar -o gradle/wrapper/gradle-wrapper.jar
```

## Opening in Android Studio

1. Open Android Studio
2. Select "Open" or "Open an Existing Project"
3. Navigate to the `android` folder (not the root project folder)
4. Click "OK"
5. Android Studio will sync Gradle and download dependencies automatically

## First Time Setup

After opening in Android Studio:

1. Wait for Gradle sync to complete
2. If you see errors about missing Gradle wrapper, let Android Studio fix them automatically
3. Build the project: `Build > Make Project`
4. Run the demo app on an emulator or device

## Testing with Backend

1. Make sure the backend is running on `http://127.0.0.1:5000`
2. For Android Emulator, use base URL: `http://10.0.2.2:5000`
3. For physical device, use your computer's IP address (e.g., `http://192.168.1.100:5000`)

## Project Key

You need a valid project key from your AppTracker backend. Create a project in the backend first, then use its `projectKey` when initializing the SDK in the demo app.



