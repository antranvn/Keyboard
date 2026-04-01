# Sample app ProGuard rules

# Keep SecureKey SDK public API
-keep class com.securekey.sdk.** { *; }

# Suppress StringConcatFactory warning
-dontwarn java.lang.invoke.StringConcatFactory
