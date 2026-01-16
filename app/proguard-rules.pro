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

# Keep tracking annotations and methods with annotations
-keepattributes RuntimeVisibleAnnotations
-keepattributes AnnotationDefault

# Keep tracking annotation classes
-keep @com.apptracker.demo.annotations.TrackButtonClick class *
-keep @com.apptracker.demo.annotations.TrackScreenView class *
-keep @com.apptracker.demo.annotations.TrackViewItem class *
-keep @com.apptracker.demo.annotations.TrackAddToCart class *
-keep @com.apptracker.demo.annotations.TrackRemoveFromCart class *
-keep @com.apptracker.demo.annotations.TrackCheckoutStarted class *
-keep @com.apptracker.demo.annotations.TrackPurchaseInitiated class *
-keep @com.apptracker.demo.annotations.TrackViewCart class *

# Keep annotation classes themselves
-keep class com.apptracker.demo.annotations.** { *; }

# Keep methods with tracking annotations
-keepclassmembers class * {
    @com.apptracker.demo.annotations.** <methods>;
}

# Keep TrackingInterceptor
-keep class com.apptracker.demo.tracking.TrackingInterceptor { *; }

