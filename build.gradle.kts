// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    // Use the alias syntax for all plugins consistently
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.google.gms.google.services) apply false // <-- CHANGED
}
