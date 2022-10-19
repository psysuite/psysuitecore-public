buildscript {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
        maven {
            url = uri("https://dl.bintray.com/kyonifer/maven")
        }

    }
    // NOTE: Do not place your application dependencies here; they belong in the individual module build.gradle files
    dependencies {
        classpath(ClassPaths.gradlePlugin)
        classpath(ClassPaths.kotlinPlugin)
        classpath(ClassPaths.navSafeArgsGradlePlugin)
    }
}

allprojects {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
        google()
    }
}

tasks.register("clean", Delete::class){
    delete(rootProject.buildDir)
}
