
plugins {
    id("com.android.application")
    id("kotlin-android")
}
android {
    namespace = "com.matthaug.taskmanager"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.matthaug.taskmanager"
        minSdk = 27
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

    buildFeatures {
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
dependencies { // App Level build.gradle.kts
    implementation("com.google.android.material:material:1.9.0")
    //New dependencies
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    //Gson Depenency below
    implementation("com.google.code.gson:gson:2.10.1")
    implementation(libs.firebase.crashlytics.buildtools)
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.11.0")
    implementation("androidx.fragment:fragment-ktx:1.6.2")
    implementation(libs.androidx.gridlayout)
    //Work Manager
    implementation("androidx.work:work-runtime-ktx:2.9.0")
    //Page3
    implementation(libs.androidx.paging.runtime.ktx)




}
