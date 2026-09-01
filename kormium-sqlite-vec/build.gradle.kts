plugins {
    id("kormium-sqlite-extension")
}

// sqlite-vec: vector search in SQLite. https://github.com/asg017/sqlite-vec
//
// Everything about *how* an extension is built lives in the convention plugin; this declares only
// what is specific to sqlite-vec.
val vecVersion: String = providers.gradleProperty("sqliteVecVersion").get()

sqliteExtension {
    extensionName = "sqlite-vec"
    entryPoint = "sqlite3_vec_init"
    sourceUrl = "https://github.com/asg017/sqlite-vec/releases/download/" +
        "v$vecVersion/sqlite-vec-$vecVersion-amalgamation.zip"
    sourceFile = "sqlite-vec.c"
    extraDefines = listOf("-DSQLITE_VEC_STATIC=1")
}

kotlin {
    // sqlite-vec publishes prebuilt binaries for Node, so this package serves it too.
    wasmJs { nodejs() }

    sourceSets {
        val wasmJsMain by getting {
            dependencies {
                // The npm package resolves the right prebuilt binary per platform, and Kotlin
                // propagates it to consumers, so an application installs nothing by hand.
                implementation(npm("sqlite-vec", vecVersion))
            }
        }
    }
}
