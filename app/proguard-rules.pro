# --------------------------------------------------------
# Author: Yash Kadav
# Email: yashkadav52@gmail.com
# Purpose: Safe ProGuard rules for Firebase + Gson + Glide + ViewModels
# --------------------------------------------------------

# --- General Android & Library Rules ---
-keepattributes Signature,SourceFile,LineNumberTable
-keepattributes *Annotation*

# Keep Parcelable implementations
-keep class * implements android.os.Parcelable { *; }

# Keep enums
-keepclassmembers enum * { *; }

# Google Mobile Ads SDK
-keep class com.google.android.gms.ads.** { *; }

# --- Your App's Model & ViewModel Classes ---
# Keeps all data models (Firebase/Gson serialization safe)
-keep class com.foodordering.krishnafoods.models.** { *; }
-keep class com.foodordering.krishnafoods.admin.model.** { *; }

# Keeps ViewModels (Lifecycle safe)
-keep class com.foodordering.krishnafoods.user.viewmodel.** { *; }

# --- Firebase ---
# Firestore: keep fields annotated with @PropertyName
-keepclassmembers class * {
    @com.google.firebase.firestore.PropertyName <fields>;
}

# Auth: keep Google/Firebase auth providers
-keep class com.google.firebase.auth.** { *; }

# --- Gson ---
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}

# --- Glide ---
-keep public class * extends com.bumptech.glide.module.AppGlideModule
-keep public class * extends com.bumptech.glide.module.LibraryGlideModule
-keep public enum com.bumptech.glide.load.ImageHeaderParser$ImageType
-keep public @interface com.bumptech.glide.annotation.GlideModule

# --- Lottie ---
-keep class com.airbnb.lottie.** { *; }

# --- FIX FOR CLOUDINARY & PICASSO ---
-dontwarn com.squareup.picasso.**

# --- Optional: Keep Retrofit/Moshi (if used) ---
# -keep class com.squareup.moshi.** { *; }
# -keep class retrofit2.** { *; }

# --------------------------------------------------------
# End of Rules
# --------------------------------------------------------
