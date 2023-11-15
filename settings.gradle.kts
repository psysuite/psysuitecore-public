pluginManagement {
    repositories {
        google()
        jcenter()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        jcenter()
    }
}

include(":nativeaudio")

//include(":nativeaudio")
//project(":nativeaudio").projectDir = File(settingsDir, "../nativeaudio/nativeaudio")


include(":psysuitepython")
project(":psysuitepython").projectDir = File(settingsDir, "../psysuitepython/psysuitepython")

include(":core")
project(":core").projectDir = File(settingsDir, "../core/core")

include(":psysuitecore")
