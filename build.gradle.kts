// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false

    // new plugins
//    id("com.google.dagger.hilt.android") version "2.48" apply false
//    id("com.google.devtools.ksp") version "1.9.10-1.0.13" apply false

    alias(libs.plugins.hilt.android) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.compose.compiler) apply false


}