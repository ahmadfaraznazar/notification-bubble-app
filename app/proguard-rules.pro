# ProGuard / R8 Configuration for Notification Bubble App
# Target Binary Budget: < 5.0 MB

# 1. R8 Optimization & Code Shrinking
-optimizationpasses 5
-allowaccessmodification
-repackageclasses ''

# 2. Strip debug/verbose logs in production release builds to prevent data leakage
-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static int v(...);
    public static int d(...);
    public static int i(...);
}

-assumenosideeffects class com.bubble.core.logging.BubbleLog {
    public static void d(...);
    public static void v(...);
}

# 3. Preserve Room SQLite entities, DAOs, and database classes
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-keep @androidx.room.Dao interface *
-keep class com.bubble.data.local.entity.** { *; }
-keep class * extends com.bubble.data.local.dao.**
-dontwarn androidx.room.paging.**

# 4. Preserve Google Play Billing KTX
-keep class com.android.billingclient.api.** { *; }

# 5. Jetpack Compose Stability Rules
-keepclassmembers class * {
    @androidx.compose.runtime.Composable *;
}

# 6. Metadata Stripping & Line Number Mapping
-renamesourcefileattribute SourceFile
-keepattributes SourceFile,LineNumberTable
