plugins {
    id(libs.plugins.android.library.get().pluginId)
    id(libs.plugins.kotlin.android.get().pluginId)
    id(libs.plugins.kotlin.parcelize.get().pluginId)
}

android {
    compileSdk = Configs.compileSdkVersion
    namespace = Configs.psysuitecorenamespace

    defaultConfig {
        minSdk = Configs.minSdkVersion
        targetSdk = Configs.targetSdkVersion
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile(ProGuards.proguardTxt), ProGuards.androidDefault)
        }
    }

    compileOptions {
        val javaVer = JavaVersion.toVersion(rootProject.ext["javaVersion"] as String)
        sourceCompatibility = javaVer
        targetCompatibility = javaVer
    }

    buildFeatures {
        viewBinding = true
    }

    kotlinOptions {
        jvmTarget = rootProject.ext["javaVersion"] as String
    }
}

dependencies{

    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))

    implementation(project(":core"))
    implementation(project(":nativeaudio"))
    implementation(project(":psysuitepython"))

    implementation(libs.kotlin.stdlib)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)

    implementation(libs.permissions)
    implementation(libs.androidx.legacy.support)
    implementation(libs.androidx.fragment)
    implementation(libs.androidx.lifecycle.viewmodel)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.recyclerview)
    api(libs.androidx.preference)

    implementation(libs.moshi)
    implementation(libs.moshi.kotlin)

    implementation(libs.okhttp)
    implementation(libs.gson)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.test.junit.ktx)
}
