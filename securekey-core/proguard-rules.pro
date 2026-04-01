# SecureKey SDK ProGuard Rules

# Keep public API
-keep class com.securekey.sdk.SecureKey { *; }
-keep class com.securekey.sdk.SecureKeyConfig { *; }
-keep class com.securekey.sdk.SecureKeyConfig$Builder { *; }
-keep class com.securekey.sdk.OnSecurityThreatListener { *; }
-keep class com.securekey.sdk.Currency { *; }
-keep class com.securekey.sdk.core.KeyboardMode { *; }
-keep class com.securekey.sdk.security.ThreatType { *; }
-keep class com.securekey.sdk.security.SecurityLevel { *; }
-keep class com.securekey.sdk.security.IntegrityReport { *; }
-keep class com.securekey.sdk.ui.SecureEditText { *; }
-keep class com.securekey.sdk.ui.SecureKeyboardView { *; }
-keep class com.securekey.sdk.ui.SecureKeyTheme { *; }
-keep class com.securekey.sdk.ui.ThemePresets { *; }
-keep class com.securekey.sdk.ui.BrandingOverlay { *; }

# Keep bridge interfaces for cross-platform usage
-keep class com.securekey.sdk.bridge.** { *; }

# Suppress StringConcatFactory warning (Java 9+ bytecode on Android)
-dontwarn java.lang.invoke.StringConcatFactory

# Obfuscate internals
-repackageclasses 'com.securekey.sdk.internal'
-allowaccessmodification
