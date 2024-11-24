    plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.google.gms.google.services)
    alias(libs.plugins.crashlytics)
    id ("kotlin-kapt") // No se puede convertir en alias porque es parte del sistema de plugins de Kotlin y se debe aplicar de manera directa
    alias(libs.plugins.dagger)
    id("kotlin-parcelize")
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
        enable = true
    }
    dataBinding{
        enable = true
    }

    kapt {
        correctErrorTypes = true
    }
}

dependencies {

//    XML dependencies
    implementation(libs.constrain.layout)
    implementation(libs.fragment.navigation)
    implementation(libs.fragment)
    implementation(libs.navigation)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.gifdrawable)
    implementation("com.google.android.material:material:1.8.0")
    implementation("com.github.bumptech.glide:glide:4.15.1")
    kapt("com.github.bumptech.glide:compiler:4.15.1")

    implementation(libs.bumptech.glide)
    implementation(libs.firebase.firestore.ktx)
    implementation(libs.material)
    annotationProcessor(libs.bumptech.glide.compiler)
    implementation(libs.gifdrawable)


//    Corrutinas dependencies
    implementation(libs.kotlinx.coroutines.android)

//    Dagger dependencies
    implementation(libs.dagger.hilt.android)
    implementation(libs.androidx.runtime.livedata)
    implementation(libs.androidx.room.ktx)
    implementation(libs.androidx.room.runtime)
    kapt(libs.androidx.room.compiler)
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
    implementation("com.github.bumptech.glide:glide:4.16.0")


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

    implementation("com.github.bumptech.glide:glide:4.15.1")
    kapt("com.github.bumptech.glide:compiler:4.15.1")
    implementation ("com.github.PhilJay:MPAndroidChart:v3.1.0")
    implementation ("androidx.cardview:cardview:1.0.0")
    implementation ("com.github.chrisbanes:PhotoView:2.3.0")
    implementation (libs.androidx.recyclerview)

    implementation ("androidx.work:work-runtime-ktx:2.8.1")



}

kapt {
    correctErrorTypes = true
}