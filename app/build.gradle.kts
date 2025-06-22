plugins {
    alias(libs.plugins.android.application)
    id("com.google.relay") version "0.3.12"
    id("org.jetbrains.kotlinx.kover") version "0.7.4"
}

android {
    namespace = "com.pim.planta"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.pim.planta"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        getByName("debug") {
            // isTestCoverageEnabled = true // Removed JaCoCo-specific setting
        }
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

    testOptions {
        unitTests {
            isIncludeAndroidResources = true
        }
    }
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.room.common)
    implementation(libs.room.runtime)
    implementation(libs.espresso.core)
    testImplementation(libs.ext.junit)
    testImplementation(libs.ext.junit)
    annotationProcessor(libs.room.compiler)
    implementation(libs.databinding.adapters)
    implementation(libs.places)
    implementation(libs.espresso.contrib)
    implementation(libs.work.testing)
    implementation(libs.rules)
    implementation(libs.espresso.intents)
    implementation("com.google.guava:guava:33.0.0-jre")

    // Mockito
    testImplementation(libs.mockito.core)
    testImplementation(libs.mockito.inline)
    androidTestImplementation(libs.mockito.android)

    // Robolectric
    implementation(libs.robolectric)
    testImplementation(libs.robolectric)
    androidTestImplementation(libs.robolectric)

    testImplementation(libs.junit)
    testImplementation(libs.espresso.core)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    implementation(libs.mpandroidchart)
    implementation(libs.work.runtime)
    testImplementation(libs.work.testing)

    implementation(libs.lottie)
}

kover {
}

koverReport {
    filters {
        excludes {
            // Exclude generated classes
            classes("**/R\$*", "**/R", "**/BuildConfig", "**/Manifest*")
        }
    }
}