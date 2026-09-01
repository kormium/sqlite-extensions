import com.vanniktech.maven.publish.MavenPublishBaseExtension

plugins {
    id("org.jetbrains.kotlinx.binary-compatibility-validator") version "0.18.1"
}

// The maven-publish plugin is not declared here: buildSrc puts it on the build classpath for the
// kormium-sqlite-extension convention plugin, and declaring a version again would conflict with
// that. Subprojects apply it by id below, which resolves from the classpath.

apiValidation {
    // The sample is a demonstration and an integration test, not a published surface.
    ignoredProjects.add("sample")

    // An extension package is a contract for application code, so track the klib ABI too.
    @OptIn(kotlinx.validation.ExperimentalBCVApi::class)
    klib { enabled = true }
}

allprojects {
    group = providers.gradleProperty("group").get()
    version = providers.gradleProperty("version").get()
    repositories { mavenCentral() }
}

subprojects {
    if (name == "sample") return@subprojects
    apply(plugin = "com.vanniktech.maven.publish")

    configure<MavenPublishBaseExtension> {
        publishToMavenCentral()
        signAllPublications()
        coordinates(group.toString(), name, version.toString())

        pom {
            name.set(this@subprojects.name)
            description.set(
                "A SQLite extension packaged for Kormium — installed with " +
                    "createSqliteDatabase { sqlite { extension(...) } } on every platform the " +
                    "extension supports.",
            )
            inceptionYear.set("2026")
            url.set("https://github.com/kormium/sqlite-extensions")
            licenses {
                license {
                    name.set("The Apache License, Version 2.0")
                    url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
                    distribution.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
                }
            }
            developers {
                developer {
                    id.set("kormium")
                    name.set("Kormium")
                    url.set("https://github.com/kormium")
                }
            }
            scm {
                url.set("https://github.com/kormium/sqlite-extensions")
                connection.set("scm:git:git://github.com/kormium/sqlite-extensions.git")
                developerConnection.set("scm:git:ssh://git@github.com/kormium/sqlite-extensions.git")
            }
        }
    }
}
