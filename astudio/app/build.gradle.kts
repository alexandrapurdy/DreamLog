import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    id("com.google.gms.google-services")
}

android {
    namespace = "edu.vassar.cmpu203.dreamlog"
    compileSdk = 36

    val localProps = Properties().apply {
        val file = rootProject.file("local.properties")
        if (file.exists()) {
            file.inputStream().use { load(it) }
        }
    }
    val geminiApiKey: String = localProps.getProperty("GEMINI_API_KEY") ?: ""

    defaultConfig {
        applicationId = "edu.vassar.cmpu203.dreamlog"
        minSdk = 32
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // now this will actually pick up the value from local.properties
        buildConfigField("String", "GEMINI_API_KEY", "\"$geminiApiKey\"")
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
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }


    buildFeatures {
        viewBinding = true
        buildConfig = true
    }


    testOptions {
        animationsDisabled = true
    }
}


dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.fragment)
    implementation(libs.cardview)
    implementation(libs.recyclerview)


    implementation(libs.google.generativeai)
    implementation("com.google.guava:guava:32.1.3-android")
    implementation(libs.common)
    implementation(libs.espresso.idling.resource)

    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation("androidx.test.espresso:espresso-contrib:3.7.0") {
        exclude(group = "com.google.protobuf", module = "protobuf-lite")
    }

    implementation(platform("com.google.firebase:firebase-bom:34.6.0"))
    implementation("com.google.firebase:firebase-firestore")
    implementation("com.google.firebase:firebase-auth")
}