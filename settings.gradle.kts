pluginManagement {
    repositories {
        google()             // Necesario para plugins de Android y Firebase
        mavenCentral()       // Repositorio general de librerías
        gradlePluginPortal() // Para plugins de Gradle
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()       // Muy importante: Firebase y Hilt están aquí
        mavenCentral()
    }
}

rootProject.name = "AppMusica"
include(":app")
