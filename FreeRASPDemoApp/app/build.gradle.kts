plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.talsec.plugin)
}

android {
    namespace = "com.aheaditec.talsec.demoapp"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.aheaditec.talsec.demoapp"
        minSdk = 23
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    talsec {
        sdkVersion = "19.2.3"
    }
}

dependencies {
    implementation(libs.kotlin.stdlib)
    implementation(libs.bundles.androidx)
}