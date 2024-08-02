import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
plugins {
    //alias(libs.plugins.android.application)
    //alias(libs.plugins.jetbrains.kotlin.android)
    id("com.android.application")
    id("kotlin-android")
}

val cameraxVersion by extra("1.2.3")

android {
    namespace = "com.example.YOLOIntegration"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.YOLOIntegration"
        minSdk = 31  // 28 before
        targetSdk = 34
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

    buildFeatures{
        viewBinding = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

//dependencies {
//    implementation(libs.androidx.core.ktx)
//    implementation(libs.androidx.appcompat)
//    implementation(libs.material)
//    implementation(libs.androidx.activity)
//    implementation(libs.androidx.constraintlayout)
//    testImplementation(libs.junit)
//    androidTestImplementation(libs.androidx.junit)
//    androidTestImplementation(libs.androidx.espresso.core)
//    implementation("org.tensorflow:tensorflow-lite:2.8.0")
//    implementation("org.tensorflow:tensorflow-lite-support:0.4.2")
//    implementation("androidx.camera:camera-camera2:1.1.0")
//    implementation("androidx.camera:camera-lifecycle:1.1.0")
//    implementation("androidx.camera:camera-view:1.1.0")
//}

dependencies {
    implementation("androidx.core:core-ktx:1.9.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.8.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")

    // CameraX dependencies
    implementation("androidx.camera:camera-core:${cameraxVersion}")
    implementation("androidx.camera:camera-camera2:${cameraxVersion}")
    implementation("androidx.camera:camera-lifecycle:${cameraxVersion}")
    implementation("androidx.camera:camera-view:${cameraxVersion}")

    // TensorFlow Lite dependencies
    implementation("org.tensorflow:tensorflow-lite:2.9.0")
    implementation("org.tensorflow:tensorflow-lite-support:0.4.3")

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}