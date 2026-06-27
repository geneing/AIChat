# Add project specific ProGuard rules here.
-keep class com.eugene.aichat.** { *; }
-keepattributes *Annotation*

# Hilt
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-keep @dagger.hilt.android.AndroidEntryPoint class * { *; }

# kotlinx.serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keepclassmembers class **$$serializer { *; }
-keepclassmembers class * {
    @kotlinx.serialization.Serializable <methods>;
}
