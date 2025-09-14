# Add project specific ProGuard rules here.

# Keep line numbers for debugging
-keepattributes SourceFile,LineNumberTable

# Keep Application class
-keep class com.example.accountbook.WaveAccountApplication { *; }
-keep class com.example.accountbook.MainActivity { *; }

# Hilt
-keepclasseswithmembers class * {
    @dagger.hilt.* <methods>;
}
-keepclasseswithmembers class * {
    @javax.inject.* <methods>;
}
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-keep class * extends dagger.hilt.android.internal.managers.ViewComponentManager$FragmentContextWrapper { *; }

# Room
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-dontwarn androidx.room.paging.**
-keep class com.example.accountbook.data.** { *; }

# Compose
-keep class androidx.compose.** { *; }
-keepclassmembers class androidx.compose.** { *; }

# MPAndroidChart
-keep class com.github.mikephil.charting.** { *; }

# Keep all models and data classes
-keep class com.example.accountbook.model.** { *; }
-keep class com.example.accountbook.viewmodel.** { *; }

# Keep enums
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# Keep Parcelable
-keepclassmembers class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator CREATOR;
}