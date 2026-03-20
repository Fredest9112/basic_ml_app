plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
}

kotlin {
    jvmToolchain(17)
}

android {
    namespace = "com.example.basic_ml_app"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.basic_ml_app"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    // ── Core Android ─────────────────────────────────────────────────────────
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)

    // ── Compose ───────────────────────────────────────────────────────────────
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)

    // ── Navigation ────────────────────────────────────────────────────────────
    implementation(libs.navigation.compose)
    implementation(libs.hilt.navigation.compose)

    // ── Dependency Injection ──────────────────────────────────────────────────
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    // ── Serialization ─────────────────────────────────────────────────────────
    implementation(libs.serialization.json)

    // ── Machine Learning ──────────────────────────────────────────────────────
    implementation(libs.tensorflow.lite)

    // ── Unit tests ────────────────────────────────────────────────────────────
    testImplementation(libs.bundles.unitTest)

    // ── Instrumented tests ────────────────────────────────────────────────────
    // The BOM must be imported separately for the androidTest classpath so that
    // version-less Compose test artifacts (ui-test-junit4 etc.) resolve correctly.
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.bundles.androidTest)
    kspAndroidTest(libs.hilt.compiler.androidtest)

    // ── Debug only ────────────────────────────────────────────────────────────
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}