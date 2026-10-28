plugins {
    id("com.android.application")
    kotlin("android")
}

android {
    namespace = "com.messaoud.bonpeint"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.messaoud.bonpeint"
        minSdk = 21
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.12.0")
}
