
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.androidx.navigation.safeargs)
    alias(libs.plugins.google.services)
    id("kotlin-kapt")
}

val localProperties = Properties().apply {
    val localPropertiesFile = rootProject.file("local.properties")
    if (localPropertiesFile.exists()) {
        localPropertiesFile.inputStream().use { load(it) }
    }
}

val cloudinaryCloudName: String = localProperties.getProperty("cloudinaryCloudName") ?: ""
val cloudinaryApiKey: String = localProperties.getProperty("cloudinaryApiKey") ?: ""
val cloudinaryApiSecret: String = localProperties.getProperty("cloudinaryApiSecret") ?: ""
val cocktailApiKey: String = localProperties.getProperty("cocktailApiKey") ?: ""

val geminiProjectId: String = localProperties.getProperty("GEMINI_PROJECT_ID") ?: ""
val geminiClientEmail: String = localProperties.getProperty("GEMINI_CLIENT_EMAIL") ?: ""
val geminiPrivateKey: String = localProperties.getProperty("GEMINI_PRIVATE_KEY") ?: ""
val geminiPrivateKeyId: String = localProperties.getProperty("GEMINI_PRIVATE_KEY_ID") ?: ""
val geminiClientId: String = localProperties.getProperty("GEMINI_CLIENT_ID") ?: ""
val geminiAPIKey: String = localProperties.getProperty("GEMINI_API_KEY") ?: ""

android {
    namespace = "com.example.surf_club_android"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.surf_club_android"
        minSdk = 29
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        buildConfigField("String", "CLOUDINARY_CLOUD_NAME", "\"$cloudinaryCloudName\"")
        buildConfigField("String", "CLOUDINARY_API_KEY", "\"$cloudinaryApiKey\"")
        buildConfigField("String", "CLOUDINARY_API_SECRET", "\"$cloudinaryApiSecret\"")
        buildConfigField("String", "COCKTAIL_API_KEY", "\"$cocktailApiKey\"")

        buildConfigField("String", "GEMINI_PROJECT_ID", "\"${localProperties.getProperty("GEMINI_PROJECT_ID")}\"")
        buildConfigField("String", "GEMINI_PRIVATE_KEY", "\"${localProperties.getProperty("GEMINI_PRIVATE_KEY")?.replace("\n", "\\n")}\"")
        buildConfigField("String", "GEMINI_CLIENT_EMAIL", "\"${localProperties.getProperty("GEMINI_CLIENT_EMAIL")}\"")
        buildConfigField("String", "GEMINI_CLIENT_ID", "\"${localProperties.getProperty("GEMINI_CLIENT_ID")}\"")
        buildConfigField("String", "GEMINI_PRIVATE_KEY_ID", "\"${localProperties.getProperty("GEMINI_PRIVATE_KEY_ID")}\"")
        buildConfigField("String", "GEMINI_API_KEY", "\"$geminiAPIKey\"")

        }

        buildFeatures {
            viewBinding = true
            buildConfig = true
        }

        buildTypes {
            release {
                isMinifyEnabled = false
                proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            }
        }

        compileOptions {
            sourceCompatibility = JavaVersion.VERSION_11
            targetCompatibility = JavaVersion.VERSION_11
        }
        kotlinOptions {
            jvmTarget = "11"
        }

        packaging {
            resources {
                pickFirst("META-INF/DEPENDENCIES")
            }
        }
    }

    dependencies {
        // Core AndroidX
        implementation(libs.androidx.core.ktx)
        implementation(libs.androidx.appcompat)
        implementation(libs.material)
        implementation(libs.androidx.activity)
        implementation(libs.androidx.constraintlayout)

        // UI Enhancements
        implementation(libs.circleimageview)
        implementation(libs.glide)
        annotationProcessor(libs.compiler)
        implementation(libs.picasso)

        // Navigation
        implementation(libs.androidx.navigation.fragment.ktx)
        implementation(libs.androidx.navigation.ui.ktx)

        // Firebase
        implementation(platform(libs.firebase.bom))
        implementation(libs.firebase.firestore)
        implementation(libs.firebase.auth)
        implementation(libs.firebase.storage)

        // Room
        implementation(libs.androidx.room.runtime)
        kapt(libs.androidx.room.compiler)
        implementation(libs.androidx.room.ktx)

        // Networking
        implementation(libs.retrofit)
        implementation(libs.converter.gson)
        implementation(libs.okhttp)
        implementation(libs.logging.interceptor)
        implementation(libs.ktor.serialization.gson)

        // Coroutines
        implementation(libs.kotlinx.coroutines.core)
        implementation(libs.kotlinx.coroutines.android)

        // Cloudinary
        implementation(libs.cloudinary.android)

        // Gemini / AI
        implementation(libs.generativeai)
        implementation(libs.google.auth.library.credentials)
        implementation(libs.google.auth.library.oauth2.http.v1180)
        implementation(libs.markwon.core)

        // Testing
        testImplementation(libs.junit)
        androidTestImplementation(libs.androidx.junit)
        androidTestImplementation(libs.androidx.espresso.core)
    }
