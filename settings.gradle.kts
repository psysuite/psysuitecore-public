pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
    versionCatalogs {
        create("libs") {
            from(files("../../psysuite/gradle/libs.versions.toml"))
        }
    }
}

include(":nativeaudio")

include(":psysuitepython")
project(":psysuitepython").projectDir = File(settingsDir, "../psysuitepython/psysuitepython")

include(":core")
project(":core").projectDir = File(settingsDir, "../core/core")

include(":psysuitecore")
