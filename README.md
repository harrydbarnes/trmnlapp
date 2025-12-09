# TRMNL Display Android

This Android application emulates a TRMNL display, allowing you to view TRMNL content on your Android device. It can be used as a standalone app or as a screensaver (Daydream).

## Purpose

The primary purpose of this application is to serve as a client for the TRMNL API, fetching and displaying images configured in your TRMNL account. This allows you to repurpose old Android devices as dedicated TRMNL displays or simply view your TRMNL dashboard on your phone.

## Features

*   **TRMNL Emulation:** Fetches and displays images from the TRMNL API.
*   **Screensaver Support:** Runs as an Android Daydream (Screensaver), perfect for always-on displays.
*   **Immersive Mode:** Hides system bars for a clean, distraction-free viewing experience.
*   **Configurable Refresh Rate:** Automatically respects the refresh rate and reset duration settings from the TRMNL API.
*   **Settings Configuration:** Easy-to-use settings screen to input your API Key and MAC Address.

## Setup

### Prerequisites

*   An Android device running Android 7.0 (Nougat) or higher (API level 24+).
*   A TRMNL account (https://usetrmnl.com/).

### Installation

1.  Clone the repository.
2.  Open the project in Android Studio.
3.  Build and run the application on your device.

### Configuration

1.  **Launch the App:** Open the "TRMNL Display" app on your device.
2.  **Open Settings:** Tap the floating settings button (gear icon).
3.  **Enter Credentials:**
    *   **API Key:** Enter your TRMNL API Key. This can be found in your TRMNL account settings.
    *   **MAC Address:** Enter the MAC Address you want to emulate. This acts as the device identifier.
4.  **Save:** Tap the "Save" button.
5.  **Enjoy:** The app will now fetch and display your TRMNL content.

## Usage

### Standalone App

Simply open the app. It will keep the screen on and refresh the content automatically.

### Screensaver (Daydream)

1.  Go to your Android device's **Settings**.
2.  Navigate to **Display** -> **Screen saver** (or "Screensaver").
3.  Select **TRMNL Display**.
4.  Configure when to start the screensaver (e.g., "While charging" or "While docked").

## Development

### Project Structure

*   `MainActivity.kt`: The main entry point for the standalone application.
*   `TrmnlDreamService.kt`: The service responsible for the screensaver functionality.
*   `SettingsActivity.kt` & `SettingsScreen`: UI for configuring API credentials.
*   `TrmnlDisplayScreen.kt`: The core Composable that fetches and renders the TRMNL image.
*   `SettingsRepository.kt`: Manages data persistence using Jetpack DataStore.

### Building

The project uses Gradle for building. You can build the APK using the standard Android Studio build process or via command line:

```bash
./gradlew assembleDebug
```

## License

[Add License Here]
