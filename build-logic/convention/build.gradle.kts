repositories {
    mavenCentral()
    gradlePluginPortal()
    google()
}

plugins {
    `kotlin-dsl`
    `groovy-gradle-plugin`
}

group = "com.antyzero.pantheon.buildlogic"

dependencies {
    //implementation(libs.android.tools.gradle.api)
    implementation("com.android.tools.build:gradle-api:8.2.2")
}
