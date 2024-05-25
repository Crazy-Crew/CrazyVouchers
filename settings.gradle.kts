import com.ryderbelserion.feather.includeProject

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

rootProject.name = "CrazyVouchers"

pluginManagement {
    repositories {
        maven("https://repo.crazycrew.us/releases")

        gradlePluginPortal()
    }
}

plugins {
    id("com.ryderbelserion.feather-settings") version "0.0.1"
}

listOf("paper", "core").forEach(::includeProject)