# Jomra AI Agent System - ProGuard Rules

# ===== GENERAL OPTIMIZATION =====
-optimizationpasses 5
-dontusemixedcaseclassnames
-dontskipnonpubliclibraryclasses
-verbose

# Keep line numbers for debugging crashes
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Remove logging in release
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
    public static *** w(...);
}

# ===== APPLICATION CLASSES =====
-keep public class com.jomra.ai.** { *; }

# Keep agent interfaces and implementations
-keep interface com.jomra.ai.agents.Agent { *; }
-keep class * implements com.jomra.ai.agents.Agent { *; }

# Keep agent response classes (used with reflection)
-keep class com.jomra.ai.agents.AgentResponse { *; }
-keep class com.jomra.ai.agents.AgentResponse$* { *; }
-keep class com.jomra.ai.agents.AgentContext { *; }
-keep class com.jomra.ai.agents.AgentInput { *; }
-keep class com.jomra.ai.agents.Action { *; }
-keep class com.jomra.ai.agents.AppState { *; }

# Keep tool interfaces
-keep interface com.jomra.ai.tools.Tool { *; }
-keep class * implements com.jomra.ai.tools.Tool { *; }
-keep class com.jomra.ai.tools.ToolResult { *; }
-keep class com.jomra.ai.tools.ToolParameter { *; }

# Keep RL components
-keep class com.jomra.ai.rl.** { *; }

# ===== TENSORFLOW LITE =====
-keep class org.tensorflow.lite.** { *; }
-keep interface org.tensorflow.lite.** { *; }
-keepclassmembers class org.tensorflow.lite.** { *; }

# TFLite GPU delegate
-keep class org.tensorflow.lite.gpu.** { *; }

# TFLite Support Library
-keep class org.tensorflow.lite.support.** { *; }

# ===== GSON =====
-keepattributes Signature
-keepattributes *Annotation*
-dontwarn sun.misc.**
-keep class com.google.gson.** { *; }
-keep class * implements com.google.gson.TypeAdapter
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer

# Keep model classes for Gson
-keep class com.jomra.ai.agents.** { *; }
-keep class com.jomra.ai.tools.** { *; }

# ===== OKHTTP =====
-dontwarn okhttp3.**
-dontwarn okio.**
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }
-keepnames class okhttp3.internal.publicsuffix.PublicSuffixDatabase

# OkHttp platform used only on JVM and when Conscrypt dependency is available.
-dontwarn org.conscrypt.Conscrypt
-dontwarn org.conscrypt.ConscryptHostnameVerifier

# ===== RETROFIT =====
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }
-keepattributes Signature
-keepattributes Exceptions

-keepclasseswithmembers class * {
    @retrofit2.http.* <methods>;
}

# ===== COROUTINES =====
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembers class kotlinx.** {
    volatile <fields>;
}

# ===== ANDROIDX =====
-keep class androidx.** { *; }
-keep interface androidx.** { *; }
-dontwarn androidx.**

# WorkManager
-keep class * extends androidx.work.Worker
-keep class * extends androidx.work.InputMerger
-keep class androidx.work.impl.background.systemalarm.RescheduleReceiver

# Room (if used)
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-dontwarn androidx.room.paging.**

# ===== MATERIAL DESIGN =====
-keep class com.google.android.material.** { *; }
-dontwarn com.google.android.material.**

# ===== SECURITY CRYPTO =====
-keep class androidx.security.crypto.** { *; }
-keepclassmembers class androidx.security.crypto.** {
    *;
}

# ===== NATIVE METHODS =====
-keepclasseswithmembernames class * {
    native <methods>;
}

# ===== CUSTOM VIEWS =====
-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet);
}

-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet, int);
}

# ===== ENUMS =====
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# ===== PARCELABLES =====
-keep class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator *;
}

# ===== SERIALIZABLE =====
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

# ===== OPTIMIZATION =====
# Remove debug code
-assumenosideeffects class kotlin.jvm.internal.Intrinsics {
    public static void checkExpressionValueIsNotNull(...);
    public static void checkNotNullExpressionValue(...);
    public static void checkParameterIsNotNull(...);
    public static void checkNotNullParameter(...);
}

# Optimize string concatenation
-optimizations !code/simplification/string

# Allow access modification for better optimization
-allowaccessmodification

# Repackage classes for smaller APK
-repackageclasses ''

# ===== WARNINGS TO SUPPRESS =====
-dontwarn org.tensorflow.lite.gpu.**
-dontwarn javax.annotation.**
-dontwarn javax.inject.**
-dontwarn sun.misc.Unsafe
-dontwarn com.google.common.**

# ===== KEEP ANNOTATIONS =====
-keepattributes RuntimeVisibleAnnotations
-keepattributes RuntimeInvisibleAnnotations
-keepattributes AnnotationDefault

# Keep custom annotations
-keep @interface com.jomra.ai.** { *; }

# ===== REFLECTION SAFETY =====
# Keep classes accessed via reflection (if any)
-keepclassmembers class * {
    @com.jomra.ai.** *;
}

# ===== R8 COMPATIBILITY =====
-keepattributes InnerClasses
-keepattributes EnclosingMethod

# Keep generic signatures (for better stack traces)
-keepattributes Signature

# ===== CRASHLYTICS (if used) =====
-keepattributes *Annotation*
-keepattributes SourceFile,LineNumberTable
-keep public class * extends java.lang.Exception
-keep class com.google.firebase.crashlytics.** { *; }
-dontwarn com.google.firebase.crashlytics.**

# ===== SIZE OPTIMIZATION =====
# Remove unused resources (handled by shrinkResources in build.gradle)
# This is complementary

# Remove unused code
-dontwarn **
-ignorewarnings

# Optimize for size over speed
-optimizations !code/simplification/arithmetic,!code/simplification/cast,!field/*,!class/merging/*
