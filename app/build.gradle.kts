plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.kapt)
    alias(libs.plugins.hilt.android)
    alias(libs.plugins.navigation.safeargs)
    id("kotlin-parcelize")
}

android {
    namespace = "com.example.palmscanner"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.palmscanner"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
    }

    buildFeatures {
        viewBinding = true
    }

    packaging {
        jniLibs {
            useLegacyPackaging = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {

    // ✅ Core Android
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.constraintlayout)

    // ✅ Navigation Component (Safe Args)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)

    // ✅ Lifecycle + ViewModel + StateFlow
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.livedata.ktx)

    // ✅ CameraX
    implementation(libs.androidx.camera.core)
    implementation(libs.androidx.camera.camera2)
    implementation(libs.androidx.camera.lifecycle)
    implementation(libs.androidx.camera.view)

    // ✅ MediaPipe Hands (Tasks Vision)
    implementation(libs.mediapipe.tasks.vision)

    // ✅ Hilt - Dependency Injection
    implementation(libs.hilt.android)
    kapt(libs.hilt.compiler)

    // ✅ Hilt Navigation
    implementation(libs.androidx.hilt.navigation.fragment)

    // ✅ Coroutines
    implementation(libs.kotlinx.coroutines.android)

    // ✅ Coil - Image Loading
    implementation(libs.coil)

    // ✅ ExifInterface
    implementation(libs.androidx.exifinterface)

    // ✅ Activity KTX (for registerForActivityResult)
    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.fragment.ktx)

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
}