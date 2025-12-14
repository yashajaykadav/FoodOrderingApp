import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.google.services)
    id ("kotlin-parcelize")
}

android {
    namespace = "com.foodordering.krishnafoods"
    compileSdk = 36

    buildFeatures {
        buildConfig = true
            viewBinding = true
    }

    defaultConfig {
        applicationId = "com.foodordering.krishnafoods"
        minSdk = 24
        targetSdk = 36
        versionCode = 18
        versionName = "2.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField("String", "CLOUDINARY_CLOUD_NAME", "\"${project.property("CLOUDINARY_CLOUD_NAME")}\"")
        buildConfigField("String", "CLOUDINARY_API_KEY", "\"${project.property("CLOUDINARY_API_KEY")}\"")
        buildConfigField("String", "CLOUDINARY_UPLOAD_PRESET", "\"${project.property("CLOUDINARY_UPLOAD_PRESET")}\"")

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

            ndk {
                debugSymbolLevel = "FULL"
            }
        }
        debug {

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

    implementation(platform(libs.firebase.bom))

    implementation(libs.firebase.auth)
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.database)
    implementation(libs.firebase.messaging)

    implementation(libs.cloudinary.android)
    implementation("com.google.android.material:material:1.11.0")

    implementation(libs.google.auth.library.oauth2.http)

    implementation(libs.okhttp)

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
    annotationProcessor(libs.glide.compiler)
    implementation(libs.lottie)
    implementation(libs.dotsindicator)
    implementation(libs.gson)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}

