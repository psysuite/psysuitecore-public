object Configs {

    const val compileSdkVersion = 31
    const val minSdkVersion     = 26
    const val targetSdkVersion  = 26
}

object Versions {

    const val kotlin = "1.6.10"
    const val ktx = "1.8.0"
    const val gradlePlugin = "4.1.1"
    const val appCompat = "1.5.1"

    const val navVersion = "2.3.5"
    const val navSafeArgsGradlePlugin = "1.0.0"
    const val moshi = "1.12.0"

    const val fragment = "1.4.0"

}

object Dependencies {

    object AndroidX {
        const val navFragment   = "androidx.navigation:navigation-fragment-ktx:${Versions.navVersion}"
        const val navUi         = "androidx.navigation:navigation-ui-ktx:${Versions.navVersion}"
        const val ktxCore       = "androidx.core:core-ktx:${Versions.ktx}"
        const val appCompat     = "androidx.appcompat:appcompat:${Versions.appCompat}"
    }

    object Kotlin {
        const val stdLib = "org.jetbrains.kotlin:kotlin-stdlib:${Versions.kotlin}"
        const val reflect   = "org.jetbrains.kotlin:kotlin-reflect:${Versions.kotlin}"
    }

    object Moshi {
        const val moshi = "com.squareup.moshi:moshi:${Versions.moshi}"
        const val moshiKt = "com.squareup.moshi:moshi-kotlin:${Versions.moshi}"

    }
}

object ClassPaths {

    const val gradlePlugin = "com.android.tools.build:gradle:${Versions.gradlePlugin}"
    const val kotlinPlugin = "org.jetbrains.kotlin:kotlin-gradle-plugin:${Versions.kotlin}"
    const val navSafeArgsGradlePlugin = "android.arch.navigation:navigation-safe-args-gradle-plugin:${Versions.navSafeArgsGradlePlugin}"
}

object Plugins {

    const val androidLibrary        = "com.android.library"
    const val kotlinAndroid         = "android"
    const val kotlinExtensions      = "android.extensions"
}

object ProGuards {

    val androidDefault = "proguard-rules.pro"
    val proguardTxt = "proguard-android.txt"
}