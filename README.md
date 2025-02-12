# Doorbell Notification Android App
This is the second part of my doorbell audio detection project ([first part is audio classification using tensorflow](https://github.com/onurds/Doorbell_AudioML)), implementing an Android application that receives and manages notifications from the Raspberry Pi doorbell detection system. The app utilizes Firebase Cloud Messaging (FCM) for receiving notifications and provides a comprehensive logging system to track doorbell events.

## Features
- Real-time notifications when doorbell is detected
- Notification logging system with timestamps
- Date-based log filtering
- Log management capabilities
- Material3 design & Native Kotlin implementation
- Support for Android 12+ devices

## Screenshots
<img src="https://github.com/user-attachments/assets/e95c0167-47c8-40ca-bf78-cbc710f825a2" alt="App Main Screen" width="379" height="800">


## Requirements
- Android Studio
- Android SDK 31 (Android 12) or higher
- Firebase project setup

## Setup Guide
1. Clone the repository
```bash
git clone https://github.com/onurds/Doorbell_AndroidApp.git
```

2. Firebase Configuration
- Create a new Firebase project in the [Firebase Console](https://console.firebase.google.com/)
- Add an Android app to your Firebase project
- Download the `google-services.json` file
- Place the file in the app directory of your project

3. Build Dependencies

The project uses Gradle with version catalog. Some of the needed libraries included are:


```toml
[versions]
androidx-room = "2.6.1"
firebase-bom = "32.7.1"

[libraries]
androidx-room-runtime = { group = "androidx.room", name = "room-runtime", version.ref = "androidx-room" }
androidx-room-compiler = { group = "androidx.room", name = "room-compiler", version.ref = "androidx-room" }
androidx-room-ktx = { group = "androidx.room", name = "room-ktx", version.ref = "androidx-room" }
firebase-messaging = { group = "com.google.firebase", name = "firebase-messaging-ktx" }
androidx-material3 = { group = "androidx.compose.material3", name = "material3" }
```

4. Android Manifest Permissions
```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
```

## Architecture
- **Room Database**: For local storage of notification logs
- **Firebase Cloud Messaging**: For receiving real-time notifications
- **Kotlin Coroutines**: For asynchronous operations
- **Material 3**: For modern UI components and theming
- **ConstraintLayout**: For responsive layouts


## Feature Details
### Notification System
- Receives real-time notifications via FCM
- Displays notifications even when app is in background
- Supports Android 12+ notification permissions

### Logging System
- Stores notification timestamps and messages
- Supports filtering by date
- Allows clearing of log history
- Persistent storage using Room database

## License
This project is licensed under the MIT License.
