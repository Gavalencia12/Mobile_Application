plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.google.gms.google.services)
    alias(libs.plugins.crashlytics)
    id ("kotlin-kapt") // No se puede convertir en alias porque es parte del sistema de plugins de Kotlin y se debe aplicar de manera directa
    alias(libs.plugins.dagger)
}

android {
    namespace = "com.example.carhive"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.carhive"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    viewBinding{
        enable = false
    }
}

dependencies {

//    Corrutinas dependencies
    implementation(libs.kotlinx.coroutines.android)

//    Dagger dependencies
    implementation(libs.dagger.hilt.android)
    kapt(libs.dagger.hilt.compile)

//    Kotlin dependencies
    implementation(libs.androidx.core.ktx)

//    Compose dependencies
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.hilt.naivgation)
    implementation(libs.androidx.lifecycle.runtime.ktx) // Lifecycle.
    implementation(libs.coil.compose) // Coil.
    implementation(libs.androidx.navigation.compose) // Navigation.
    implementation(libs.androidx.lifecycle.viewmodel.compose) // ViewModel.

//    Firebase dependencies
    implementation(platform(libs.firebase.bom)) // Firebase Bom.
    implementation(libs.firebase.database) // Real time Database.
    implementation(libs.firebase.storage) // Storage.
    implementation(libs.firebase.crashlytics) // Crashlytics.
    implementation(libs.firebase.auth) // Authentication.

//    Testing dependencies
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)

//    Debugging dependencies
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

}

kapt {
    correctErrorTypes = true
}