// Top-level build file where you can add configuration options common to all sub-projects/modules.
buildscript {
    dependencies {
        // Overrides the Kotlin Gradle Plugin version that Android Gradle plugin's
        // built-in Kotlin support uses by default, so it matches libs.versions.toml.
        classpath(libs.kotlin.gradle.plugin)
    }
}

plugins {
    alias(libs.plugins.android.application) apply false
}