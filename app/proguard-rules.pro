# Keep Room entities
-keep class com.smartcallblocker.app.data.db.entities.** { *; }

# Keep Hilt
-keep class dagger.hilt.** { *; }
-keep class * extends dagger.hilt.android.HiltAndroidApp { *; }
-keepclassmembers,allowobfuscation class * {
    @dagger.hilt.android.AndroidEntryPoint <fields>;
}

# Gson
-keepattributes Signature
-keepattributes *Annotation*
-keep class com.google.gson.** { *; }
-keep class com.smartcallblocker.app.data.backup.** { *; }

# libphonenumber
-keep class com.google.i18n.phonenumbers.** { *; }

# Keep Service
-keep class com.smartcallblocker.app.service.** { *; }
