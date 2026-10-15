import java.util.Properties
import java.io.FileInputStream

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    id("com.google.gms.google-services")
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.nan.nearbystorefinder"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.nan.nearbystorefinder"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        val localProperties = Properties()
        val localPropertiesFile = rootProject.file("local.properties")
        if (localPropertiesFile.exists()) {
            localProperties.load(localPropertiesFile.inputStream())
        }
        val foursquareKey = localProperties.getProperty("FOURSQUARE_API_KEY") ?: ""
        val placesApiKey = localProperties.getProperty("PLACES_API_KEY") ?: ""

        buildConfigField("String", "FOURSQUARE_API_KEY", "\"$foursquareKey\"")
        buildConfigField("String", "PLACES_API_KEY", "\"$placesApiKey\"")
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.material3)
    implementation("com.google.android.material:material:1.12.0")
    testImplementation(libs.junit)
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
    testImplementation("app.cash.turbine:turbine:1.0.0")
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    
    // Firebase
    implementation(platform("com.google.firebase:firebase-bom:33.10.0"))
    implementation("com.google.firebase:firebase-auth")
    // Removed Firestore and Storage as requested

    // Maps & Location
    implementation("com.google.maps.android:maps-compose:2.11.4")
    implementation("com.google.android.gms:play-services-maps:18.2.0")
    implementation("com.google.android.gms:play-services-location:21.0.1")

    // Places SDK for Android (New) - secure client-side access to Places API (New);
    // key is restricted by package name + SHA-1 in Google Cloud Console (no backend needed).
    // Pinned to 4.4.1 rather than the newest 5.x/6.x release: those versions declare
    // a *strict* kotlin-stdlib:2.2.0 dependency, which forces the whole project onto
    // Kotlin 2.2.0 and breaks Room's kapt/KSP annotation processing on this project's
    // Kotlin 2.0.21 toolchain. 4.4.1 still has searchNearby/searchByText/Photos (New)
    // (added in 3.3.0) without that forced bump.
    implementation("com.google.android.libraries.places:places:4.4.1")

    // Coil
    implementation("io.coil-kt:coil-compose:2.5.0")

    // Room (migrated from kapt to KSP: adding the Places SDK pins kotlin-stdlib
    // to 2.2.0, whose metadata format kapt's bundled kotlinx-metadata-jvm reader
    // can't parse. KSP reads Kotlin metadata via the compiler directly and has
    // no such version ceiling.)
    val roomVersion = "2.6.1"
    implementation("androidx.room:room-runtime:$roomVersion")
    implementation("androidx.room:room-ktx:$roomVersion")
    ksp("androidx.room:room-compiler:$roomVersion")

    // Ktor
    val ktorVersion = "2.3.7"
    implementation("io.ktor:ktor-client-core:$ktorVersion")
    implementation("io.ktor:ktor-client-android:$ktorVersion")
    implementation("io.ktor:ktor-client-content-negotiation:$ktorVersion")
    implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")
    implementation("io.ktor:ktor-client-logging:$ktorVersion")

    // Koin
    implementation("io.insert-koin:koin-android:3.5.3")
    implementation("io.insert-koin:koin-androidx-compose:3.5.3")

    // Navigation
    implementation("androidx.navigation:navigation-compose:2.7.7")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("com.google.android.gms:play-services-auth:20.7.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.7.3")

    // Splash
    implementation("androidx.core:core-splashscreen:1.0.1")
}
