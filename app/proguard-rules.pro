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

-keepclassmembernames class kotlinx.** {
    volatile <fields>;
}

-keep class kotlin.reflect.jvm.internal.ReflectionFactoryImpl { *; }
# gson
-keepattributes *Annotation*

-keepclasseswithmembernames class * {
    native <methods>;
}
-keepclassmembers,allowobfuscation class * {
  @com.google.gson.annotations.SerializedName <fields>;
}

# Нужно чтобы библиотечные методы не были обфусцированы
# Только удаление не используемого кода
# так же поправлены большинство note сообщения
-keep,allowshrinking,includedescriptorclasses class android.** { *; }
-keep,allowshrinking,includedescriptorclasses class androidx.** { *; }
-keep,allowshrinking,includedescriptorclasses class com.google.** { *; }
-keep,allowshrinking,includedescriptorclasses class kotlinx.coroutines.** { *; }
-keep,allowshrinking,includedescriptorclasses class okhttp3.** { *; }
-keep,allowshrinking,includedescriptorclasses class kotlin.** { *; }
-keep,allowshrinking,includedescriptorclasses class com.nostra13.universalimageloader.** { *; }
-keep,allowshrinking,includedescriptorclasses class com.davemorrissey.labs.** { *; }
-keep,allowshrinking,includedescriptorclasses class org.jsoup.** { *; }
-keep,allowshrinking,includedescriptorclasses class com.soundcloud.android.** { *; }
-keep,allowshrinking,includedescriptorclasses class com.getkeepsafe.taptargetview.** { *; }
-keep,allowshrinking,includedescriptorclasses class info.guardianproject.** { *; }

# проект
-dontobfuscate
-keep,includedescriptorclasses class org.nv95.openmanga.** { *; }

-keep @JvmOverloads class * {
  <init>(...);
  *;
}

