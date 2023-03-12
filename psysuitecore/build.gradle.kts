plugins {
    id(Plugins.androidLibrary)
    id(Plugins.kotlinAndroid)
    id("kotlin-parcelize")
}

android {

    compileSdkVersion(Configs.compileSdkVersion)
    defaultConfig {
        minSdkVersion(Configs.minSdkVersion)
        targetSdkVersion(Configs.targetSdkVersion)
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile(ProGuards.proguardTxt), ProGuards.androidDefault)        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_1_8.toString()
    }

    buildFeatures {
        viewBinding = true
    }
}

dependencies {

    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))

    implementation(Dependencies.Kotlin.stdLib)

    implementation(Dependencies.AndroidX.ktxCore)
    implementation(Dependencies.AndroidX.appCompat)
    implementation(Dependencies.AndroidX.recycleView)
    implementation(Dependencies.AndroidX.legacySupport)
    api(Dependencies.AndroidX.preference)

    implementation(Dependencies.Moshi.moshi)
    implementation(Dependencies.Moshi.moshiKt)

    testImplementation(Dependencies.junit)
    androidTestImplementation(Dependencies.AndroidX.testRunner)
    androidTestImplementation(Dependencies.AndroidX.testEspressoCore)

    implementation(project(":core"))
    implementation(project(":nativeaudio"))
    implementation(project(":psysuitepython"))
}