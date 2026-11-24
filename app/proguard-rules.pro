# ProGuard / R8 Configuration for Notification Bubble App

# 1. Strip debug logs in production release builds to prevent data leakage
-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static int v(...);
    public static int d(...);
    public static int i(...);
}

# 2. Preserve Room SQLite annotations and database schema rules
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.paging.**

# 3. Preserve Google Play Billing KTX
-keep class com.android.billingclient.api.** { *; }

# 4. Compose stability rules
-keepclassmembers class * {
    @androidx.compose.runtime.Composable *;
}
