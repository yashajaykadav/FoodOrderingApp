pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        google() // ✅ Needed for Google/Firebase dependencies
        mavenCentral()
    }
}

rootProject.name = "FoodOrderingApp"
include(":app")
