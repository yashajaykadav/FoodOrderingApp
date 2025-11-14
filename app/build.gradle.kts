import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.google.services)
}

android {
    namespace = "com.foodordering.krishnafoods"
    compileSdk = 36

    buildFeatures {
        buildConfig = true
    }

    defaultConfig {
        applicationId = "com.foodordering.krishnafoods"
        minSdk = 24
        targetSdk = 36
        versionCode = 16
        versionName = "1.1.3"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Cloudinary credentials
        buildConfigField("String", "CLOUDINARY_CLOUD_NAME", "\"${project.property("CLOUDINARY_CLOUD_NAME")}\"")
        buildConfigField("String", "CLOUDINARY_API_KEY", "\"${project.property("CLOUDINARY_API_KEY")}\"")
        buildConfigField("String", "CLOUDINARY_UPLOAD_PRESET", "\"${project.property("CLOUDINARY_UPLOAD_PRESET")}\"")

        // Firebase (dynamic endpoint based on project ID)
        buildConfigField("String", "FIREBASE_PROJECT_ID", "\"${project.property("FIREBASE_PROJECT_ID")}\"")
        buildConfigField(
            "String",
            "FCM_ENDPOINT",
            "\"https://fcm.googleapis.com/v1/projects/${project.property("FIREBASE_PROJECT_ID")}/messages:send\""
        )
        // Google Sign-In
        buildConfigField( "String", "DEFAULT_WEB_CLIENT_ID", "\"${project.property("DEFAULT_WEB_CLIENT_ID")}\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            isDebuggable = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            // EDIT: This block generates the native debug symbols required by the Play Console.
            ndk {
                debugSymbolLevel = "FULL"
            }
        }
        debug {
            // Inherits settings from defaultConfig.
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlin {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }
    packaging {
        resources {
            excludes += "/META-INF/DEPENDENCIES"
        }
        jniLibs {
            useLegacyPackaging = false
            keepDebugSymbols.add("**/*.so")
        }
    }
}

dependencies {

    // Firebase BoM manages Firebase dependency versions
    implementation(platform(libs.firebase.bom))

    // Firebase dependencies
    implementation(libs.firebase.auth.ktx)
    implementation(libs.firebase.firestore.ktx)
    implementation(libs.firebase.database.ktx)
    implementation(libs.firebase.messaging.ktx)

    // Cloudinary for image uploads
    implementation(libs.cloudinary.android)

    // SUGGESTION: This is a server-side library. Verify if it's truly needed on Android.
    // The standard library 'play.services.auth' is usually sufficient.
    implementation(libs.google.auth.library.oauth2.http)

    // OkHttp for HTTP requests
    implementation(libs.okhttp)

    // AndroidX and Material dependencies
    implementation(libs.play.services.auth)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.fragment.ktx)
    implementation(libs.glide)
    implementation(libs.androidx.swiperefreshlayout)
    implementation(libs.androidx.gridlayout)
    implementation(libs.androidx.activity)
    annotationProcessor(libs.compiler) // For Glide
    implementation(libs.lottie)
    implementation(libs.dotsindicator)
    implementation(libs.gson)
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.9.4")


    // Testing dependencies
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}

