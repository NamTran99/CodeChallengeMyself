plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("kotlin-kapt")
    id("com.google.dagger.hilt.android")
    id("kotlin-parcelize")
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.myapplication"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.myapplication"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        getByName("debug") {
            buildConfigField("String", "API_AI_KEY", "\"AIzaSyAe_bGma3nCm6q0RBdE8Td04k1l0ZtJvZs\"")
        }
        getByName("release") {
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

    kotlinOptions {
        jvmTarget = "17"
    }

    packaging {
        resources {
            excludes.add("/META-INF/{AL2.0,LGPL2.1}") // loại bỏ các giấy phép trong thư viện bên thuws 3 ko thì build lỗi
        }
    }

//    defaultConfig{
//        ndk{
//            abiFilters  += listOf("armeabi-v7a", "arm64-v8a")
//        }
//    }

    buildFeatures {
        buildConfig = true
        viewBinding = true
        dataBinding = true
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.15.0")
    implementation("com.airbnb.android:lottie:6.4.0")
    implementation(platform("org.jetbrains.kotlin:kotlin-bom:1.9.0"))
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.navigation:navigation-fragment-ktx:2.8.5")
    implementation("com.airbnb.android:lottie:6.4.0")
    implementation("com.google.ai.client.generativeai:generativeai:0.9.0")
    implementation("com.google.dagger:hilt-android:2.49")
    implementation(project(":mylibrary"))
    implementation("com.google.firebase:firebase-messaging-ktx:24.1.0")
    implementation(project(":myBase"))
    kapt("com.google.dagger:hilt-android-compiler:2.48")
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.10.0")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
    implementation("com.guolindev.permissionx:permissionx:1.8.1")
//    releaseImplementation(files("libs/ads-sdk-6.1.0.6.aar"))
//    debugImplementation(files("libs/ads-sdk-6.1.0.6.aar"))
//    implementation(files("libs/ads-sdk-6.1.0.6.aar"))
}
