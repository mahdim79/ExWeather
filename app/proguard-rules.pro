# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# Library Room
-dontwarn android.arch.util.paging.CountedDataSource
-dontwarn android.arch.persistence.room.paging.LimitOffsetDataSource

# Library Retrofit
# Retrofit does reflection on generic parameters. InnerClasses is required to use Signature and
# EnclosingMethod is required to use InnerClasses.
-keep class okhttp3.** { *; }
-keep class retrofit2.** { *; }
-keepattributes *Annotation*

-keep class com.squareup.retrofit2.** { *; }
-keep class retrofit2.** { *; }

# Keep Gson model classes
-keep class com.location.tracker.map.app.framework.network.dto.** { *; }

-keepclassmembers,allowobfuscation class * {
    @com.squareup.moshi.* <fields>;
}
# If you're using Gson with ProGuard, keep all fields and annotations
-keepattributes *Annotation*
-keep class com.google.gson.** { *; }

-keepattributes Signature, InnerClasses, EnclosingMethod

# Retrofit does reflection on method and parameter annotations.
-keepattributes RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations

# Keep annotation default values (e.g., retrofit2.http.Field.encoded).
-keepattributes AnnotationDefault

# Ignore annotation used for build tooling.
-dontwarn org.codehaus.mojo.animal_sniffer.IgnoreJRERequirement

# Ignore JSR 305 annotations for embedding nullability information.
-dontwarn javax.annotation.**

# Guarded by a NoClassDefFoundError try/catch and only used when on the classpath.
-dontwarn kotlin.Unit

# Top-level functions that can only be used by Kotlin.
-dontwarn retrofit2.KotlinExtensions
#-dontwarn retrofit2.KotlinExtensions$*

# With R8 full mode, it sees no subtypes of Retrofit interfaces since they are created with a Proxy
# and replaces all potential values with null. Explicitly keeping the interfaces prevents this.
-if interface * { @retrofit2.http.* <methods>; }
-keep,allowobfuscation interface <1>

# Keep inherited services.
-if interface * { @retrofit2.http.* <methods>; }
-keep,allowobfuscation interface * extends <1>

# Keep generic signature of Call, Response (R8 full mode strips signatures from non-kept items).
-keep,allowobfuscation,allowshrinking interface retrofit2.Call
-keep,allowobfuscation,allowshrinking class retrofit2.Response

# With R8 full mode generic signatures are stripped for classes that are not
# kept. Suspend functions are wrapped in continuations where the type argument
# is used.
-keep,allowobfuscation,allowshrinking class kotlin.coroutines.Continuation

# Needed to avoid crash on minified app release with Maps SDK v3.1 beta (https://issuetracker.google.com/issues/148084488)
-keep,allowoptimization class com.google.android.libraries.maps.** { *; }

-keep class com.google.ads.** # Don't proguard AdMob classes
-dontwarn com.google.ads.** # Temporary workaround for v6.2.1. It gives a warning that you can ignore

-ignorewarnings

# -keepclassmembers class * extends org.greenrobot.greendao.AbstractDao {
# public static java.lang.String TABLENAME;
# }
-keep class **$Properties

# If you do not use SQLCipher:
# -dontwarn org.greenrobot.greendao.database.**
# If you do not use RxJava:
#-dontwarn rx.**

-keepattributes *Annotation*

##---------------Begin: proguard configuration for Gson  ----------
# Gson uses generic type information stored in a class file when working with fields. Proguard
# removes such information by default, so configure it to keep all of it.
-keepattributes Signature

# For using GSON @Expose annotation
-keepattributes *Annotation*

# Gson specific classes
-dontwarn sun.misc.**
#-keep class com.google.gson.stream.** { *; }

# Application classes that will be serialized/deserialized over Gson
-keep class com.google.gson.examples.android.model.** { <fields>; }

# Prevent proguard from stripping interface information from TypeAdapterFactory,
# JsonSerializer, JsonDeserializer instances (so they can be used in @JsonAdapter)
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer

# Prevent R8 from leaving Data object members always null
-keepclassmembers,allowobfuscation class * {
  @com.google.gson.annotations.SerializedName <fields>;
}

# Retrofit does reflection on generic parameters. InnerClasses is required to use Signature and
# EnclosingMethod is required to use InnerClasses.
-keepattributes Signature, InnerClasses, EnclosingMethod

# Retrofit does reflection on method and parameter annotations.
-keepattributes RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations

# Retain service method parameters when optimizing.
-keepclassmembers,allowshrinking,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}

# Ignore annotation used for build tooling.
-dontwarn org.codehaus.mojo.animal_sniffer.IgnoreJRERequirement

# Ignore JSR 305 annotations for embedding nullability information.
-dontwarn javax.annotation.**

# Guarded by a NoClassDefFoundError try/catch and only used when on the classpath.
-dontwarn kotlin.Unit

-keep class com.squareup.okhttp.** { *; }
-keep interface com.squareup.okhttp.** { *; }

-dontwarn com.squareup.**
-dontwarn com.squareup.okhttp.**

-keepattributes Signature
-keepattributes *Annotation*
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }

-dontwarn okhttp3.**
-dontwarn okio.**

-keepattributes Signature

# Glide
-keep class com.bumptech.glide.** { *; }
-keep class com.bumptech.glide.load.model.** { *; }
-keep class com.bumptech.glide.load.resource.bitmap.** { *; }

-keep public class * implements com.bumptech.glide.module.GlideModule
-keep public class * extends com.bumptech.glide.module.AppGlideModule
-keep public enum com.bumptech.glide.load.ImageHeaderParser$** {
  **[] $VALUES;
  public *;
}
-dontwarn com.squareup.picasso.**
-dontwarn com.bumptech.glide.**

-dontwarn com.yalantis.ucrop**
-keep class com.yalantis.ucrop** { *; }
-keep interface com.yalantis.ucrop** { *; }

-dontwarn com.google.android.material.snackbar.**

# Rxjava Rules
-dontwarn sun.misc.**

-keepclassmembers class rx.internal.util.unsafe.*ArrayQueue*Field* {
   long producerIndex;
   long consumerIndex;
}

-dontnote rx.internal.util.PlatformDependent

#Android Room Rules
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.paging.**

#KotlinX
-dontwarn kotlinx.coroutines.flow.**


-keepclassmembers enum * { *; }
-keep,allowobfuscation interface <1>

# For Google Play Services
-keep public class com.google.android.gms.ads.**{
   public *;
}

# For old ads classes
-keep public class com.google.ads.**{
   public *;
}

# For mediation
-keepattributes *Annotation*

-keep public class com.google.android.gms.common.internal.safeparcel.SafeParcelable {
   public static final *** NULL;
}

-keepnames @com.google.android.gms.common.annotation.KeepName class *
-keepclassmembernames class * {
   @com.google.android.gms.common.annotation.KeepName *;
}

-keep class org.json.** { *; }
-keepclasseswithmembers class org.json.** { *; }
-keep class com.google.android.gms.** { *; }
-keep class com.google.maps.** { *; }

# Preserve generic type information for Gson serialization/deserialization
-keepattributes Signature
-keepattributes *Annotation*

# Keep Gson model classes (adjust the package as per your app)
-keep class com.dust.exweather.model.dataclasses.** { *; }

# Keep Gson TypeAdapter classes
-keep class com.google.gson.** { *; }
-keep class com.google.gson.internal.bind.** { *; }
-keep class com.google.gson.reflect.TypeToken { *; }
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}

# Keep TypeToken subclasses
-keep class com.google.gson.reflect.TypeToken { *; }
-keep class * extends com.google.gson.reflect.TypeToken { *; }