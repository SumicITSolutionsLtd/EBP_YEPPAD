plugins {
    id("com.android.application")
    id("kotlin-android")
    // The Flutter Gradle Plugin must be applied after the Android and Kotlin Gradle plugins.
    id("dev.flutter.flutter-gradle-plugin")
}

android {
    namespace = "com.yonah.ebp_platform"
    compileSdk = 36 // ✅ Explicitly set to latest installed SDK
    ndkVersion = flutter.ndkVersion

    defaultConfig {
        minSdk = flutter.minSdkVersion
        targetSdk = 36 // ✅ Required for Play Protect compliance
        versionCode = 1
        versionName = "1.0"
        multiDexEnabled = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_11.toString()
    }

    signingConfigs {
        create("release") {
            keyAlias = "key"
            keyPassword = "EBP@25*#**@8"
            storeFile = file("C:/Users/yonah/key.jks")
            storePassword = "EBP@25*#**@8"
        }
    }

    buildTypes {
        getByName("release") {
            signingConfig = signingConfigs.getByName("release")
            isMinifyEnabled = false
            isShrinkResources = false
        }
    }

    // ✅ ABI splits to support all architectures
    splits {
        abi {
            isEnable = true
            reset()
            include("armeabi-v7a", "arm64-v8a", "x86", "x86_64")
            isUniversalApk = true // ✅ Ensures one APK works on all devices
        }
    }
}

flutter {
    source = "../.."
}