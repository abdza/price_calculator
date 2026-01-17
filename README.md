# Price Calculator - Android App

A floating overlay calculator for traders. Calculate profit and loss targets while viewing your charts in other apps.

## Features

- **Floating overlay** - Stays on top of your trading apps and charts
- **Collapsible** - Minimize to a small button when not in use
- **Draggable** - Position it anywhere on screen
- **Real-time calculation** - Results update as you type
- **Profit targets**: +10% and +20%
- **Stop loss levels**: -20% and -30%
- **Dark theme** - Easy on the eyes

## How It Works

1. Open the app and tap "Launch Floating Calculator"
2. Grant overlay permission when prompted (one-time setup)
3. A floating button (📊) appears on your screen
4. Tap the button to expand the calculator
5. Enter a price to see profit/loss targets
6. Drag the header to move it around
7. Tap the minimize button to collapse back to the button
8. Tap the X to close completely

## Screenshots

**Collapsed View:**
- Small circular button with 📊 icon
- Drag anywhere on screen

**Expanded View:**
- Input field for price entry
- Green section: +20% and +10% profit targets
- Red section: -20% and -30% stop loss levels
- Minimize and close buttons in header

## Building the App

### Prerequisites

- Android Studio Ladybug (2024.2.1) or newer
- JDK 17
- Android SDK with API 35

### Steps

1. Open Android Studio
2. Select "Open" and navigate to this project folder
3. Wait for Gradle sync to complete
4. Connect your Android device or start an emulator
5. Click "Run" (green play button) or press Shift+F10

### Building a Release APK

1. In Android Studio, go to **Build > Generate Signed Bundle / APK**
2. Select **APK**
3. Create a new keystore or use an existing one
4. Select **release** build variant
5. The APK will be generated in `app/release/`

Alternatively, from command line:
```bash
./gradlew assembleRelease
```

## Project Structure

```
app/
├── src/main/
│   ├── java/com/abdullahsolutions/pricecalculator/
│   │   └── MainActivity.kt          # Main app code with Compose UI
│   ├── res/
│   │   ├── drawable/                 # App icons
│   │   ├── mipmap-anydpi-v26/       # Adaptive icons
│   │   ├── values/                   # Colors, strings, themes
│   │   └── xml/                      # Backup rules
│   └── AndroidManifest.xml
├── build.gradle.kts                  # App build config
└── proguard-rules.pro               # ProGuard rules for release
```

## Tech Stack

- **Kotlin** - Modern Android development language
- **Jetpack Compose** - Declarative UI framework
- **Material 3** - Latest Material Design components
- **Edge-to-edge** - Full screen experience

## Customization

To modify the percentage values, edit the calculation formulas in `MainActivity.kt`:

```kotlin
// Current values
it * 1.20  // +20%
it * 1.10  // +10%
it * 0.80  // -20%
it * 0.70  // -30%
```

## License

© Abdullah Solutions. All rights reserved.
