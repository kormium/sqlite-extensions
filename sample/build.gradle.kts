plugins {
    kotlin("multiplatform")
}

// Four extension packages in one database — the whole point of the design, exercised rather than
// asserted. Each package brings only its own static library; there is exactly one SQLite in the
// process and no combination artifact anywhere.
//
// Not published: this is a demonstration and an integration test, not a library.
kotlin {
    jvmToolchain(21)

    linuxX64()
    macosX64()
    macosArm64()

    applyDefaultHierarchyTemplate()

    sourceSets {
        val nativeMain by getting {
            dependencies {
                implementation("io.github.kormium:kormium-sqlite:${providers.gradleProperty("kormiumVersion").get()}")
                implementation(project(":kormium-sqlite-vec"))
                implementation(project(":kormium-sqlite-uuid"))
                implementation(project(":kormium-sqlite-regexp"))
                implementation(project(":kormium-sqlite-series"))
            }
        }
        val nativeTest by getting {
            dependencies { implementation(kotlin("test")) }
        }
    }
}
