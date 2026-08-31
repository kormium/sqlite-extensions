pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
}

rootProject.name = "sqlite-extensions"

include("kormium-sqlite-vec")

// Develop against a Kormium checkout sitting next to this one, when there is one: the SPI moves
// with the driver, and an extension is the first thing to notice when it changes. Without the
// sibling the build simply resolves the published artifacts instead.
val kormium = file("../kormium")
if (kormium.resolve("settings.gradle.kts").exists() &&
    providers.gradleProperty("kormium.ignoreSibling").orNull != "true"
) {
    includeBuild(kormium)
}
