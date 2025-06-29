plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("com.google.gms.google-services")
}

android {
    namespace = "com.ot.lidarsstudio"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.ot.lidarsstudio"
        minSdk = 24
        targetSdk = 35
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    implementation("androidx.gridlayout:gridlayout:1.1.0")
    // Glide for image loading
    implementation("com.github.bumptech.glide:glide:4.16.0")
    // Firebase BOM to manage versions
    implementation(platform("com.google.firebase:firebase-bom:33.16.0"))

    // Firebase dependencies
    implementation("com.google.firebase:firebase-storage")
    implementation("com.google.firebase:firebase-firestore")
}
