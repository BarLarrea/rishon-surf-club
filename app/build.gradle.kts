import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.androidx.navigation.safeargs)
    id("kotlin-kapt")
    alias(libs.plugins.google.services)
}

val localProperties = Properties().apply {
    val localPropertiesFile = rootProject.file("local.properties")
    if (localPropertiesFile.exists()) {
        localPropertiesFile.inputStream().use { load(it) }
    }
}



// Retrieve the Cloudinary properties using the correct keys from local.properties
val cloudinaryCloudName: String = localProperties.getProperty("cloudinaryCloudName") ?: ""
val cloudinaryApiKey: String = localProperties.getProperty("cloudinaryApiKey") ?: ""
val cloudinaryApiSecret: String = localProperties.getProperty("cloudinaryApiSecret") ?: ""
val cocktailApiKey: String = localProperties.getProperty("cocktailApiKey") ?: ""

// Retrieve Gemini API Key or Service Account credentials
val geminiProjectId: String = localProperties.getProperty("GEMINI_PROJECT_ID") ?: ""
val geminiClientEmail: String = localProperties.getProperty("GEMINI_CLIENT_EMAIL") ?: ""
val geminiPrivateKey: String = localProperties.getProperty("GEMINI_PRIVATE_KEY") ?: ""
val geminiPrivateKeyId: String = localProperties.getProperty("GEMINI_PRIVATE_KEY_ID") ?: ""
val geminiClientId: String = localProperties.getProperty("GEMINI_CLIENT_ID") ?: ""
val geminiAPIKey: String = localProperties.getProperty("GEMINI_API_KEY") ?: ""



android {
    buildFeatures {
        buildConfig = true
    }
    packaging {
        resources {
            pickFirst("META-INF/DEPENDENCIES")
        }
    }

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
        viewBinding = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.circleimageview)
    implementation(libs.glide)
    annotationProcessor(libs.compiler)

    implementation(libs.picasso)
    implementation(libs.cloudinary.android)

    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.auth)
    implementation(libs.firebase.storage)
    implementation(libs.ktor.serialization.gson)

    implementation(libs.androidx.room.runtime)
    kapt(libs.androidx.room.compiler)
    implementation(libs.androidx.room.ktx)

    // Retrofit and Gson converter
    implementation(libs.retrofit)
    implementation(libs.converter.gson)

    // OkHttp (and optional logging interceptor)
    implementation(libs.okhttp)
    implementation(libs.logging.interceptor)

    // Kotlin Coroutines
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)

    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // Navigation Graph
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)

//Chat

    implementation(libs.generativeai) // Google AI Gemini SDK
    implementation(libs.retrofit) // Retrofit for HTTP requests
    implementation(libs.converter.gson)// Gson for JSON parsing
    implementation(libs.okhttp)// OkHttp for networking (Retrofit internally uses it)
    implementation(libs.logging.interceptor) // Optional for logging requests
    implementation(libs.kotlinx.coroutines.android)// Coroutines for asynchronous execution
    implementation(libs.google.auth.library.credentials)
    implementation(libs.google.auth.library.oauth2.http.v1180)

}
