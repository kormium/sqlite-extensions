plugins {
    `kotlin-dsl`
}

repositories {
    mavenCentral()
    gradlePluginPortal()
}

// The convention plugin below applies these, so they have to be on the build classpath here.
dependencies {
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:2.4.10")
    implementation("com.vanniktech:gradle-maven-publish-plugin:0.37.0")
}
