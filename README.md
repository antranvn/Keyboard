# SecureKey SDK

Bank-level secure in-app keyboard SDK for Android. Replaces the system keyboard with a fully custom, canvas-rendered keyboard that prevents keystroke interception, screen capture, and clipboard attacks.

Built for banking, fintech, and any application handling sensitive user input like passwords, PINs, OTPs, and financial amounts.

## Features

- **Canvas-Rendered Keyboard** -- All keys drawn directly on a single Canvas view. No individual `View` buttons, eliminating view hierarchy attacks.
- **AES-256-GCM Encryption** -- Every keystroke is encrypted with per-session ephemeral keys using ECDH key exchange and HKDF-SHA256 derivation.
- **Secure Memory** -- Input stored in off-heap `DirectByteBuffer`, zeroed on dismiss. No heap copies, no `String` objects for sensitive data.
- **Anti-Keylogging** -- Detects `AccessibilityService` instances with `FLAG_REQUEST_FILTER_KEY_EVENTS`.
- **Clipboard Protection** -- Disables copy/paste/cut/share/autofill on secure fields. Clears clipboard on activation.
- **Screenshot Protection** -- Applies `FLAG_SECURE` to prevent screen capture and recording.
- **4 Keyboard Modes** -- QWERTY (full), Numeric PIN, Numeric OTP (auto-advance), Amount Pad (currency + decimal).
- **Gboard-Style UI** -- Material Design rendering with light/dark/custom themes.
- **Bank Branding** -- Logo bar or watermark overlay, configurable position, "Secured by SecureKey" badge.
- **Cross-Platform Bridges** -- Interface contracts for React Native, Flutter, and WebView.
- **Zero Third-Party Dependencies** -- Only AndroidX Core and AppCompat.

## Requirements

- Android API 26+ (Android 8.0 Oreo)
- Kotlin 1.9+ / Java 17
- AndroidX

## Installation

### Gradle (Maven Local / Private Repository)

Add the dependency to your app's `build.gradle.kts`:

```kotlin
dependencies {
    implementation("com.securekey:securekey-sdk:1.0.0")
}
```

### AAR File

1. Build the AAR:
   ```bash
   ./gradlew :securekey-core:assembleRelease
   ```
2. Copy `securekey-core/build/outputs/aar/securekey-core-release.aar` to your project's `libs/` directory.
3. Add to your `build.gradle.kts`:
   ```kotlin
   dependencies {
       implementation(files("libs/securekey-core-release.aar"))
       implementation("androidx.core:core-ktx:1.12.0")
       implementation("androidx.appcompat:appcompat:1.6.1")
   }
   ```

### Publish to Maven Local

```bash
./gradlew :securekey-core:publishToMavenLocal
```

Then reference it from any project:

```kotlin
repositories {
    mavenLocal()
}

dependencies {
    implementation("com.securekey:securekey-sdk:1.0.0")
}
```

## Quick Start

Integrate the secure keyboard in 5 lines:

```kotlin
// 1. Configure (in Application.onCreate or Activity.onCreate)
val config = SecureKeyConfig.Builder()
    .theme(ThemePresets.system(this))
    .build()

// 2. Initialize
SecureKey.init(this, config)

// 3. Bind to Activity (required for keyboard view attachment)
SecureKey.getInstance().bind(this)

// 4. Attach to an EditText
SecureKey.getInstance().attachTo(editText, KeyboardMode.QWERTY_FULL)
```

That's it. The system keyboard is automatically suppressed on attached fields, and the secure keyboard appears on focus.

## Integration Guide

### Application Setup

Initialize once in your `Application` class or main `Activity`:

```kotlin
class BankingApp : Application() {
    override fun onCreate() {
        super.onCreate()

        val config = SecureKeyConfig.Builder()
            .theme(ThemePresets.system(this))
            .shuffleNumericKeys(false)
            .hapticFeedback(true)
            .keyPreview(true)
            .securityLevel(SecurityLevel.STRICT)
            .allowScreenshot(false)
            .doneButtonLabel("Done")
            .currency(Currency.USD)
            .onSecurityThreat { threat, details ->
                Log.w("Security", "Threat: $threat - $details")
            }
            .build()

        SecureKey.init(this, config)
    }
}
```

### Activity Lifecycle

Bind to the Activity and forward lifecycle events:

```kotlin
class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        SecureKey.getInstance().bind(this)

        val passwordField = findViewById<SecureEditText>(R.id.password_field)
        SecureKey.getInstance().attachTo(passwordField, KeyboardMode.QWERTY_FULL)
    }

    override fun onResume() {
        super.onResume()
        SecureKey.getInstance().onResume()
    }

    override fun onPause() {
        super.onPause()
        SecureKey.getInstance().onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        SecureKey.getInstance().onDestroy()
    }
}
```

### SecureEditText

Always use `SecureEditText` instead of `EditText` for sensitive fields. It automatically:

- Disables copy, paste, cut, share, and autofill
- Blocks text selection handles and long-press menus
- Disables keyboard suggestions
- Applies `FLAG_SECURE` to prevent screenshots
- Suppresses the system keyboard

**XML Layout:**

```xml
<com.securekey.sdk.ui.SecureEditText
    android:id="@+id/password_field"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:hint="Password"
    android:inputType="textPassword" />
```

**Programmatic:**

```kotlin
val secureField = SecureEditText(context).apply {
    hint = "Enter PIN"
}
```

---

## Keyboard Modes

### QWERTY_FULL

Full keyboard with letters, numbers row, symbols, shift (tap = one capital, double-tap = caps lock), backspace, space, and a configurable Done button. Two symbol layers accessible via the `?123` toggle.

```kotlin
SecureKey.getInstance().attachTo(editText, KeyboardMode.QWERTY_FULL)
```

### NUMERIC_PIN

3x4 numeric grid for PIN entry. Supports optional key position randomization via `shuffleNumericKeys(true)` in config.

```kotlin
SecureKey.getInstance().attachTo(pinField, KeyboardMode.NUMERIC_PIN)
```

### NUMERIC_OTP

Numeric pad with auto-advance between fields. Attach multiple `SecureEditText` fields and they will auto-advance as the user types each digit.

```kotlin
// Option 1: Attach individually
otpFields.forEach { field ->
    SecureKey.getInstance().attachTo(field, KeyboardMode.NUMERIC_OTP)
}

// Option 2: Use the convenience method
SecureKey.getInstance().attachOtpFields(
    field1, field2, field3, field4, field5, field6,
    otpLength = 6
)
```

### AMOUNT_PAD

Numeric pad with decimal point and currency symbol display. Supports quick amount suggestion chips, thousand separator formatting, max amount validation, and configurable decimal places.

```kotlin
SecureKey.getInstance().attachAmountField(
    editText = amountField,
    currency = Currency.INR,
    suggestions = listOf(500.0, 1000.0, 5000.0, 10000.0),
    maxAmount = 100000.0,
    decimalPlaces = 2
)
```

---

## Jetpack Compose Integration

Since SecureKey uses Android `View` types (`SecureEditText`), wrap them with `AndroidView` in Compose:

```kotlin
@Composable
fun SecurePasswordField(
    onTextChanged: (String) -> Unit
) {
    AndroidView(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        factory = { context ->
            SecureEditText(context).apply {
                hint = "Password"
                inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                textSize = 16f

                addTextChangedListener(object : TextWatcher {
                    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                    override fun afterTextChanged(s: Editable?) {
                        onTextChanged(s?.toString() ?: "")
                    }
                })

                SecureKey.getInstance().attachTo(this, KeyboardMode.QWERTY_FULL)
            }
        }
    )
}
```

### OTP Fields in Compose

```kotlin
@Composable
fun OtpInput(otpLength: Int = 6, onComplete: (String) -> Unit) {
    AndroidView(
        modifier = Modifier.fillMaxWidth(),
        factory = { context ->
            LinearLayout(context).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER

                val fields = mutableListOf<SecureEditText>()

                for (i in 0 until otpLength) {
                    val field = SecureEditText(context).apply {
                        inputType = InputType.TYPE_CLASS_NUMBER
                        gravity = Gravity.CENTER
                        textSize = 24f
                        maxLines = 1
                        val size = (48 * context.resources.displayMetrics.density).toInt()
                        layoutParams = LinearLayout.LayoutParams(size, size).apply {
                            marginStart = (4 * context.resources.displayMetrics.density).toInt()
                            marginEnd = (4 * context.resources.displayMetrics.density).toInt()
                        }
                        isCursorVisible = false
                    }

                    field.addTextChangedListener(object : TextWatcher {
                        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                        override fun afterTextChanged(s: Editable?) {
                            val text = s?.toString() ?: ""
                            if (text.length == 1 && i < otpLength - 1) {
                                fields[i + 1].requestFocus()
                            }
                            val fullOtp = fields.joinToString("") { it.text.toString() }
                            if (fullOtp.length == otpLength) {
                                onComplete(fullOtp)
                            }
                        }
                    })

                    fields.add(field)
                    addView(field)
                }

                fields.forEach { field ->
                    SecureKey.getInstance().attachTo(field, KeyboardMode.NUMERIC_OTP)
                }

                fields.firstOrNull()?.requestFocus()
            }
        }
    )
}
```

---

## Configuration Reference

All configuration is done through `SecureKeyConfig.Builder`:

```kotlin
val config = SecureKeyConfig.Builder()
    // Theme
    .theme(ThemePresets.LIGHT)              // Light, Dark, or custom theme

    // Bank branding
    .bankLogo(logoBitmap)                   // Bank logo bitmap (max 120x32dp)
    .bankLogoPosition(LogoPosition.TOP_LEFT) // TOP_LEFT, CENTER, TOP_RIGHT
    .watermarkEnabled(true)                 // Show logo as watermark overlay
    .watermarkOpacity(0.08f)               // Watermark transparency (0.0-1.0)

    // Input behavior
    .shuffleNumericKeys(false)             // Randomize PIN/OTP key positions
    .hapticFeedback(true)                  // Vibrate on key press
    .soundFeedback(false)                  // Play sound on key press
    .keyPreview(true)                      // Show key magnification popup
    .doneButtonLabel("Done")               // Custom label for Done key
    .allowPaste(false)                     // Allow paste on secure fields
    .allowScreenshot(false)                // Allow screenshots (FLAG_SECURE)

    // Security
    .securityLevel(SecurityLevel.STRICT)   // STRICT, MODERATE, or RELAXED
    .onSecurityThreat { threat, details -> // Threat detection callback
        // Handle threat
    }

    // OTP
    .otpAutoRead(true)                     // Auto-read OTP from SMS

    // Amount pad
    .currency(Currency.USD)                // Currency for amount pad
    .amountSuggestions(listOf(100.0, 500.0, 1000.0, 5000.0))
    .maxAmount(999999999.99)               // Maximum allowed amount
    .decimalPlaces(2)                      // Decimal places for amount

    // Locale
    .locale(Locale.getDefault())           // Locale for formatting

    .build()
```

### Configuration Options

| Option | Type | Default | Description |
|--------|------|---------|-------------|
| `theme` | `SecureKeyTheme` | `ThemePresets.LIGHT` | Keyboard visual theme |
| `bankLogo` | `Bitmap?` | `null` | Bank logo bitmap |
| `bankLogoPosition` | `LogoPosition` | `TOP_LEFT` | Logo placement |
| `watermarkEnabled` | `Boolean` | `false` | Show logo as centered watermark |
| `watermarkOpacity` | `Float` | `0.08` | Watermark alpha (0.0-1.0) |
| `shuffleNumericKeys` | `Boolean` | `false` | Randomize numeric key positions |
| `hapticFeedbackEnabled` | `Boolean` | `true` | Haptic feedback on key press |
| `soundFeedbackEnabled` | `Boolean` | `false` | Sound feedback on key press |
| `keyPreviewEnabled` | `Boolean` | `true` | Show key preview popup |
| `securityLevel` | `SecurityLevel` | `STRICT` | Threat enforcement level |
| `onSecurityThreat` | `OnSecurityThreatListener?` | `null` | Threat callback |
| `doneButtonLabel` | `String` | `"Done"` | Custom Done key label |
| `allowPaste` | `Boolean` | `false` | Allow paste on secure fields |
| `allowScreenshot` | `Boolean` | `false` | Allow screenshots |
| `otpAutoRead` | `Boolean` | `true` | Auto-read OTP from SMS |
| `amountSuggestions` | `List<Double>` | `[100, 500, 1000, 5000]` | Quick amount chips |
| `currency` | `Currency` | `USD` | Amount pad currency |
| `maxAmount` | `Double` | `999999999.99` | Maximum input amount |
| `decimalPlaces` | `Int` | `2` | Decimal places for amounts |
| `locale` | `Locale` | System default | Formatting locale |

---

## Theming

### Built-in Themes

```kotlin
// Light theme (default)
.theme(ThemePresets.LIGHT)

// Dark theme
.theme(ThemePresets.DARK)

// Follows system light/dark mode
.theme(ThemePresets.system(context))
```

### Custom Theme

```kotlin
val bankTheme = SecureKeyTheme(
    keyBackgroundColor = Color.WHITE,
    keyPressedColor = Color.parseColor("#E0E0E0"),
    keyTextColor = Color.parseColor("#1A237E"),         // Navy text
    keyboardBackgroundColor = Color.parseColor("#F5F5F5"),
    actionKeyColor = Color.parseColor("#FFB300"),        // Gold accent
    actionKeyTextColor = Color.WHITE,
    modifierKeyColor = Color.parseColor("#CFD8DC"),
    modifierKeyTextColor = Color.parseColor("#1A237E"),
    modifierKeyPressedColor = Color.parseColor("#B0BEC5"),
    spaceBarColor = Color.WHITE,
    keyCornerRadius = 16f,
    keyElevation = 1.5f,
    keyTextSize = 48f,
    accentColor = Color.parseColor("#FFB300"),
    popupBackgroundColor = Color.parseColor("#1A237E"),
    popupTextColor = Color.WHITE
)

val config = SecureKeyConfig.Builder()
    .theme(bankTheme)
    .build()
```

### Theme Properties

| Property | Type | Description |
|----------|------|-------------|
| `keyBackgroundColor` | `Int` | Character key background |
| `keyPressedColor` | `Int` | Key background when pressed |
| `keyTextColor` | `Int` | Character key text color |
| `keyboardBackgroundColor` | `Int` | Overall keyboard background |
| `actionKeyColor` | `Int` | Done/Return key background |
| `actionKeyTextColor` | `Int` | Done/Return key text |
| `modifierKeyColor` | `Int` | Shift/Backspace/Symbol key background |
| `modifierKeyTextColor` | `Int` | Modifier key text |
| `modifierKeyPressedColor` | `Int` | Modifier key pressed state |
| `spaceBarColor` | `Int` | Space bar background |
| `keyCornerRadius` | `Float` | Key corner radius in px |
| `keyElevation` | `Float` | Key shadow elevation |
| `keyTextSize` | `Float` | Key text size in px |
| `keyFontFamily` | `Typeface` | Font for key labels |
| `accentColor` | `Int` | General accent color |
| `popupBackgroundColor` | `Int` | Key preview popup background |
| `popupTextColor` | `Int` | Key preview popup text |
| `keyStrokeColor` | `Int` | Key border color (`TRANSPARENT` for none) |

### Bank Branding

```kotlin
// Load your bank logo
val logo = BitmapFactory.decodeResource(resources, R.drawable.bank_logo)

val config = SecureKeyConfig.Builder()
    .bankLogo(logo)
    .bankLogoPosition(BrandingOverlay.LogoPosition.CENTER)
    .watermarkEnabled(false) // Use logo bar mode
    .build()
```

Branding modes:
- **LOGO_BAR** -- Opaque strip above the keyboard with the bank logo (max 120x32dp).
- **WATERMARK** -- Semi-transparent logo centered behind keys (alpha configurable via `watermarkOpacity`).

The "Secured by SecureKey" badge is always shown at the bottom of the keyboard.

---

## Security

### Threat Detection

The SDK detects three categories of runtime threats:

| Threat | Description |
|--------|-------------|
| `ACCESSIBILITY_KEYLOGGER` | An `AccessibilityService` with `FLAG_REQUEST_FILTER_KEY_EVENTS` is active, capable of intercepting key events. |
| `SCREEN_OVERLAY` | A screen overlay is drawn on top of the keyboard, potentially capturing touch coordinates. |
| `SCREEN_RECORDING` | The screen is being recorded or captured. |

### Security Levels

| Level | Behavior |
|-------|----------|
| `STRICT` | Block input and alert on any detected threat. |
| `MODERATE` | Warn the user but allow continued input. |
| `RELAXED` | Log silently with no user-visible action. |

### Threat Listener

```kotlin
SecureKey.getInstance().setThreatListener { threat, details ->
    when (threat) {
        ThreatType.ACCESSIBILITY_KEYLOGGER -> {
            // details contains the keylogger package name(s)
            showSecurityAlert("Keylogger detected: $details")
        }
        ThreatType.SCREEN_OVERLAY -> {
            showSecurityAlert("Screen overlay detected")
        }
        ThreatType.SCREEN_RECORDING -> {
            showSecurityAlert("Screen recording detected")
        }
    }
}
```

### Security Report

Query the current security state at any time:

```kotlin
val report = SecureKey.getInstance().getSecurityReport()

if (!report.isSecure) {
    report.detectedThreats.forEach { threat ->
        Log.w("Security", "Active threat: $threat")
    }

    if (report.activeKeyloggers.isNotEmpty()) {
        Log.w("Security", "Keyloggers: ${report.activeKeyloggers}")
    }
}
```

### Built-in Protections

These protections are always active and require no configuration:

- **System keyboard suppression** -- The system IME is hidden on all attached fields, including handling manufacturer-specific quirks (e.g., Samsung aggressive keyboard show).
- **Clipboard guard** -- Clipboard is cleared when a secure field is activated. Copy/paste/cut/share actions are blocked.
- **FLAG_SECURE** -- Applied to the Activity window to prevent screenshots and screen recording (unless `allowScreenshot(true)` is set).
- **No autofill** -- `SecureEditText` sets `IMPORTANT_FOR_AUTOFILL_NO_EXCLUDE_DESCENDANTS` on API 26+.
- **No text suggestions** -- `TYPE_TEXT_FLAG_NO_SUGGESTIONS` is set. `isSuggestionsEnabled()` returns `false`.
- **No text selection** -- Selection handles, long-press menu, and action mode callbacks are all blocked.
- **Encrypted keystrokes** -- Each key press is encrypted with AES-256-GCM using a per-session ephemeral key before dispatch.
- **Secure memory** -- All input data is stored in `DirectByteBuffer` (off-heap), zeroed on keyboard dismiss and Activity lifecycle transitions.

---

## Supported Currencies

| Code | Symbol | Constant |
|------|--------|----------|
| USD | $ | `Currency.USD` |
| EUR | &euro; | `Currency.EUR` |
| GBP | &pound; | `Currency.GBP` |
| JPY | &yen; | `Currency.JPY` |
| INR | &#8377; | `Currency.INR` |

Custom currencies are supported via `Currency("BRL")`, which will display the currency code as-is unless mapped (BRL shows `R$`, AUD shows `A$`, CAD shows `C$`, CNY shows `&yen;`, KRW shows `&#8361;`).

---

## Cross-Platform Bridges

The SDK includes bridge interface contracts for cross-platform integration. These are abstract classes/interfaces that define the API contract. The actual platform-specific plugin packages (React Native module, Flutter plugin) are separate projects that extend these bridges.

### React Native

Extend `ReactNativeBridge` in your React Native module:

```kotlin
class SecureKeyModule(reactContext: ReactApplicationContext)
    : ReactNativeBridge(), ReactContextBaseJavaModule(reactContext) {

    override fun getName() = "SecureKeyModule"

    override fun initialize(config: Map<String, Any>) { /* ... */ }
    override fun attachToInput(reactTag: Int, mode: String) { /* ... */ }
    override fun attachOtpFields(reactTags: List<Int>, otpLength: Int) { /* ... */ }
    override fun attachAmountField(reactTag: Int, currencyCode: String,
        suggestions: List<Double>, maxAmount: Double, decimalPlaces: Int) { /* ... */ }
    override fun show() { secureKey.show() }
    override fun dismiss() { secureKey.dismiss() }
    override fun getSecurityReport(callback: (Map<String, Any>) -> Unit) {
        callback(reportToMap(secureKey.getSecurityReport()))
    }
}
```

### Flutter

Extend `FlutterBridge` and wire it to a `MethodChannel`:

```kotlin
class SecureKeyPlugin : FlutterBridge(), FlutterPlugin {
    // MethodChannel name: "com.securekey.sdk/keyboard"
    override fun initialize(config: Map<String, Any>) { /* ... */ }
    override fun attachToInput(viewId: Int, mode: String) { /* ... */ }
    // ... implement remaining methods
}
```

### WebView

Add the `WebViewBridge` as a JavaScript interface:

```kotlin
val webView: WebView = findViewById(R.id.web_view)
webView.addJavascriptInterface(
    WebViewBridge(SecureKey.getInstance()),
    "SecureKey"
)
```

Then call from JavaScript:

```javascript
// Show/dismiss the keyboard
window.SecureKey.show();
window.SecureKey.dismiss();

// Get security report as JSON string
const report = JSON.parse(window.SecureKey.getSecurityReport());
if (!report.isSecure) {
    console.warn("Threats detected:", report.threats);
}
```

---

## API Reference

### SecureKey

| Method | Description |
|--------|-------------|
| `SecureKey.init(context, config)` | Initialize the SDK singleton. Call once from `Application` or `Activity`. |
| `SecureKey.getInstance()` | Get the singleton instance. Throws if `init()` hasn't been called. |
| `bind(activity)` | Bind to an Activity for keyboard view attachment and `FLAG_SECURE`. Call from `onCreate()`. |
| `attachTo(editText, mode)` | Attach the secure keyboard to an `EditText` with the specified mode. |
| `attachOtpFields(vararg editTexts, otpLength)` | Attach multiple `EditText` fields for OTP entry. |
| `attachAmountField(editText, currency, suggestions, maxAmount, decimalPlaces)` | Attach an `EditText` for amount entry with currency and formatting options. |
| `show()` | Programmatically show the keyboard. |
| `dismiss()` | Dismiss the keyboard with animation. |
| `getSecurityReport()` | Get the current `IntegrityReport` with threat detection results. |
| `setThreatListener(listener)` | Set a callback for real-time threat notifications. |
| `onResume()` | Forward from `Activity.onResume()`. Re-checks threats. |
| `onPause()` | Forward from `Activity.onPause()`. Wipes secure memory. |
| `onDestroy()` | Forward from `Activity.onDestroy()`. Destroys the SDK instance and all resources. |

### IntegrityReport

| Property | Type | Description |
|----------|------|-------------|
| `isSecure` | `Boolean` | `true` if no threats detected |
| `detectedThreats` | `List<ThreatType>` | List of active threats |
| `activeKeyloggers` | `List<String>` | Package names of detected keyloggers |
| `hasScreenOverlay` | `Boolean` | Screen overlay detected |
| `isScreenBeingRecorded` | `Boolean` | Screen recording active |
| `timestamp` | `Long` | Report generation timestamp |

---

## ProGuard / R8

The SDK includes consumer ProGuard rules that are automatically applied. All public API classes are preserved. Internal implementation classes are obfuscated in release builds.

If you encounter issues, add these rules to your app's `proguard-rules.pro`:

```proguard
-keep class com.securekey.sdk.** { *; }
```

---

## Architecture

```
securekey-core/
  src/main/java/com/securekey/sdk/
    SecureKey.kt              # Main SDK entry point (singleton)
    SecureKeyConfig.kt        # Builder-pattern configuration
    OnSecurityThreatListener.kt
    Currency.kt

    core/
      Key.kt                  # Key data model (bounds, label, type)
      KeyboardLayout.kt       # Layout model (rows of keys, bounds calculation)
      KeyboardMode.kt         # Mode enum (QWERTY, PIN, OTP, AMOUNT)
      LayoutEngine.kt         # Layout factory
      InputProcessor.kt       # Touch -> key -> encrypt -> dispatch pipeline
      ShiftState.kt           # Shift state machine (OFF/SINGLE/CAPS_LOCK)

    security/
      SecureBuffer.kt         # DirectByteBuffer-backed storage
      SecureString.kt         # Off-heap string storage
      SecureMemoryManager.kt  # Lifecycle-aware memory management
      SecureKeyDispatcher.kt  # AES-256-GCM encrypted keystroke dispatch
      AntiKeylogProtection.kt # AccessibilityService detection
      ClipboardGuard.kt       # Clipboard monitoring and blocking
      IntegrityGuard.kt       # Threat detection orchestrator
      ThreatType.kt           # Threat type enum
      SecurityLevel.kt        # Enforcement level enum
      IntegrityReport.kt      # Security report data class

    ui/
      SecureKeyboardView.kt   # Canvas-rendered keyboard View
      SecureEditText.kt       # Hardened EditText
      KeyboardRenderer.kt     # Canvas drawing engine
      TouchHandler.kt         # Touch event processing
      KeyboardAnimator.kt     # Show/dismiss animations
      KeyPreviewPopup.kt      # Key magnification popup
      FocusManager.kt         # Focus-based keyboard show/dismiss
      SystemKeyboardSuppressor.kt
      SecureKeyTheme.kt       # Theme data class
      ThemePresets.kt          # Built-in themes
      BrandingOverlay.kt      # Bank logo rendering

    bridge/
      ReactNativeBridge.kt    # React Native interface contract
      FlutterBridge.kt        # Flutter interface contract
      WebViewBridge.kt        # WebView JavaScript bridge
```

---

## Sample App

The `sample-app` module demonstrates all features across 5 screens:

| Screen | Keyboard Mode | Demonstrates |
|--------|--------------|--------------|
| Login | `QWERTY_FULL` | Password entry with shift/caps |
| OTP Verification | `NUMERIC_OTP` | 6-digit OTP with auto-advance |
| PIN Setup | `NUMERIC_PIN` | PIN entry with confirmation flow |
| Transfer | `AMOUNT_PAD` | Amount entry with currency and quick chips |
| Settings | -- | Live configuration toggling |

Build and install:

```bash
./gradlew :sample-app:installDebug
```

---

## License

[Add your license here]
