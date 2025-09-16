# 🌟 Astro AutoTapper

**Automated screen tapping for astrophotography**

An Android application designed to automate screen taps at regular intervals, perfect for astrophotography sessions where you need to take photos continuously without manual intervention.

## 📱 Features

- **🎯 Target Selection**: Tap anywhere on screen to set your target location
- **⏱️ Customizable Timer**: Set intervals between taps (in seconds)
- **🖼️ Floating Overlay**: Convenient overlay controls that stay on top of other apps
- **🔄 Start/Stop Control**: Easy start and stop functionality
- **🌙 Astrophotography Optimized**: Designed specifically for long astrophotography sessions
- **📱 Modern UI**: Clean, intuitive interface with Material Design

## 🛠️ Technical Stack

- **Language**: Kotlin
- **Architecture**: MVVM with Repository Pattern
- **Dependency Injection**: Dagger Hilt
- **UI**: View Binding with Material Design Components
- **Services**: Accessibility Service for automated tapping
- **Permissions**: Overlay and Accessibility permissions

## 🏗️ Project Structure

```
app/src/main/java/com/astro/autotapper/
├── AutoTapperApplication.kt          # Application class with Hilt
├── model/
│   └── TapTarget.kt                  # Data model for tap coordinates
├── repository/
│   ├── AutoTapRepository.kt          # Handles tap logic
│   └── PermissionRepository.kt       # Manages app permissions
├── service/
│   ├── AutoTapAccessibilityService.kt # Accessibility service for tapping
│   ├── OverlayService.kt             # Floating overlay controls
│   └── OverlayTouchListener.kt       # Touch handling for overlay
├── ui/
│   ├── MainActivity.kt               # Main app screen
│   ├── TargetSelectionActivity.kt    # Target selection screen
│   ├── TimerDialogActivity.kt        # Timer configuration screen
│   └── viewmodel/                    # ViewModels for each screen
├── util/
│   └── CoordinateUtils.kt            # Coordinate calculation utilities
```

## 🚀 Getting Started

### Prerequisites

- Android Studio Arctic Fox or later
- Android SDK 21+ (Android 5.0+)
- Kotlin 1.9+

### Installation

1. **Clone the repository**
   ```bash
   git clone https://github.com/ashuchauhan2/AstroAutotapper.git
   cd AstroAutotapper
   ```

2. **Open in Android Studio**
   - Open Android Studio
   - Select "Open an existing project"
   - Navigate to the cloned directory

3. **Build the project**
   ```bash
   ./gradlew build
   ```

4. **Install on device**
   ```bash
   ./gradlew installDebug
   ```

## 📋 Required Permissions

The app requires two critical permissions to function:

### 1. 🖼️ Overlay Permission (SYSTEM_ALERT_WINDOW)
- **Purpose**: Display floating controls over other apps
- **Grant**: Settings → Apps → Astro AutoTapper → Display over other apps

### 2. ♿ Accessibility Permission
- **Purpose**: Perform automated screen taps
- **Grant**: Settings → Accessibility → Astro AutoTapper → Enable

## 🎮 How to Use

1. **Launch the app** and grant required permissions
2. **Tap "Start Overlay"** to show floating controls
3. **Select Target**: Tap the target button and choose where to tap
4. **Set Timer**: Configure the interval between taps
5. **Start Tapping**: Press the play button to begin automation
6. **Stop**: Use the stop button or close overlay to end

## 🔧 Configuration

### Timer Settings
- **Default**: 5 seconds
- **Range**: 1-3600 seconds (1 second to 1 hour)
- **Precision**: 1 second increments

### Target Selection
- **Method**: Single tap to set coordinates
- **Visual**: Crosshair indicator shows selected location
- **Accuracy**: Pixel-perfect positioning

## 🏗️ Architecture

The app follows **MVVM (Model-View-ViewModel)** architecture with:

- **Models**: Data classes for app state
- **Views**: Activities with View Binding
- **ViewModels**: Business logic and state management
- **Repositories**: Data access layer
- **Services**: Background operations (overlay, accessibility)

### Key Components

- **MainActivity**: Permission management and app entry point
- **OverlayService**: Floating controls implementation
- **AutoTapAccessibilityService**: Automated tapping functionality
- **Hilt Modules**: Dependency injection configuration

## 🛡️ Security & Privacy

- **No Network Access**: App works completely offline
- **No Data Collection**: No user data is collected or transmitted
- **Local Storage Only**: All settings stored locally on device
- **Open Source**: Full source code available for review

## 🧪 Testing

### Manual Testing
1. Test overlay display and positioning
2. Verify target selection accuracy
3. Check timer interval precision
4. Test start/stop functionality

### Debug Features
- Long press permissions button for debug info
- Accessibility service status logging
- Overlay positioning diagnostics

## 🚨 Troubleshooting

### Common Issues

**Overlay not appearing:**
- Check overlay permission is granted
- Restart the app
- Try the debug overlay test (long press accessibility button)

**Taps not working:**
- Verify accessibility service is enabled
- Check service is running in accessibility settings
- Restart accessibility service

**Permission denied:**
- Manually grant permissions in system settings
- Restart app after granting permissions

## 🤝 Contributing

Contributions are welcome! Please follow these steps:

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

### Development Guidelines
- Follow Kotlin coding conventions
- Use meaningful commit messages
- Add comments for complex logic
- Test on multiple Android versions

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🌟 Acknowledgments

- **Material Design**: UI components and guidelines
- **Android Accessibility**: Framework for automated interactions
- **Dagger Hilt**: Dependency injection framework
- **Kotlin Coroutines**: Asynchronous programming

## 📞 Support

If you encounter any issues or have questions:

1. Check the [Issues](https://github.com/ashuchauhan2/AstroAutotapper/issues) page
2. Create a new issue with detailed description
3. Include device model, Android version, and app version

---
