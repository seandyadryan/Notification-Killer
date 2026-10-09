pluginManagement {
    resolutionStrategy {
        eachPlugin {
            val version = requested.version
            when (requested.id.id) {
                "com.android.application" -> useModule("com.android.tools.build:gradle:$version")
                "org.jetbrains.kotlin.android" -> useModule("org.jetbrains.kotlin:kotlin-gradle-plugin:$version")
                "org.jetbrains.kotlin.plugin.compose" -> useModule("org.jetbrains.kotlin:compose-compiler-gradle-plugin:$version")
                "com.google.devtools.ksp" -> useModule("com.google.devtools.ksp:symbol-processing-gradle-plugin:$version")
            }
        }
    }
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
}
rootProject.name = "Notification Killer"
include(":app")
