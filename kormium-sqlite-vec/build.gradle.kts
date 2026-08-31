plugins {
    kotlin("multiplatform")
    id("com.vanniktech.maven.publish")
}

// sqlite-vec packaged as a Kormium SqliteExtension.
//
// The package carries ONLY sqlite-vec, never a SQLite of its own: the C is compiled with
// -DSQLITE_CORE into a static library whose sqlite3_* symbols stay unresolved, and the final link
// satisfies them from the libsqlite3.a already embedded in kormium-sqlite's cinterop klib. That is
// what lets several extensions coexist — each brings itself, and there is exactly one SQLite in the
// process. See Kormium ADR 0013.
val kormiumVersion: String = providers.gradleProperty("kormiumVersion").get()
val vecVersion: String = providers.gradleProperty("sqliteVecVersion").get()

// The SQLite headers this extension compiles against come from the Kormium release it will be
// linked into — published there as a `sqlite-headers` artifact — so the two cannot drift apart.
// Declared outside `kotlin { }`: inside it, `dependencies` is the source-set DSL, not the project's.
//
// A composite build does not share artifacts with a classifier, so when developing against a
// sibling checkout the headers are read from its source tree instead; the published zip is what
// CI and standalone builds use.
val siblingHeaders: File = rootProject.file("../kormium/kormium-sqlite/src/nativeInterop/cinterop")
val useSiblingHeaders: Boolean =
    siblingHeaders.resolve("sqlite3.h").isFile && siblingHeaders.resolve("sqlite3ext.h").isFile

val sqliteHeaders: Configuration by configurations.creating {
    isCanBeConsumed = false
    isCanBeResolved = true
}

dependencies {
    if (!useSiblingHeaders) {
        sqliteHeaders("io.github.kormium:kormium-sqlite:$kormiumVersion:sqlite-headers@zip")
    }
}

val headersDir: Provider<Directory> = layout.buildDirectory.dir("sqlite-headers")

val unpackHeaders = tasks.register<Sync>("unpackSqliteHeaders") {
    description = "Collects sqlite3.h / sqlite3ext.h from the Kormium release being targeted"
    if (useSiblingHeaders) {
        from(siblingHeaders) { include("sqlite3.h", "sqlite3ext.h") }
    } else {
        from(provider { sqliteHeaders.map { zipTree(it) } })
    }
    into(headersDir)
}

kotlin {
    explicitApi()
    jvmToolchain(21)

    val cinteropDir = layout.buildDirectory.dir("vec-src").get().asFile
    val amalgamation = File(cinteropDir, "sqlite-vec.c")

    // sqlite-vec publishes a single-file amalgamation per release; fetching it keeps ~350 KB of
    // third-party C out of this repository and makes the version a property rather than a commit.
    val fetchVec = tasks.register("fetchSqliteVec") {
        description = "Downloads the sqlite-vec $vecVersion amalgamation"
        outputs.dir(cinteropDir)
        doLast {
            cinteropDir.mkdirs()
            val base = "https://github.com/asg017/sqlite-vec/releases/download/v$vecVersion"
            val zip = File(cinteropDir, "amalgamation.zip")
            uri("$base/sqlite-vec-$vecVersion-amalgamation.zip").toURL().openStream().use { input ->
                zip.outputStream().use { input.copyTo(it) }
            }
            copy {
                from(zipTree(zip))
                into(cinteropDir)
            }
            check(amalgamation.exists()) { "sqlite-vec amalgamation did not contain sqlite-vec.c" }
        }
    }

    val konanDataDir = System.getenv("KONAN_DATA_DIR")?.let(::File)
        ?: File(System.getProperty("user.home"), ".konan")
    val konanVersion = "2.4.10"
    fun runKonan(): List<String> {
        val dist = (konanDataDir.listFiles { f ->
            f.isDirectory && f.name.startsWith("kotlin-native-prebuilt-") && f.name.endsWith(konanVersion)
        } ?: emptyArray<File>()).firstOrNull()
            ?: error("Kotlin/Native $konanVersion toolchain not found under $konanDataDir")
        return if (System.getProperty("os.name").startsWith("Windows"))
            listOf("cmd", "/c", dist.resolve("bin/run_konan.bat").absolutePath)
        else
            listOf(dist.resolve("bin/run_konan").absolutePath)
    }

    listOf(
        linuxX64(), macosX64(), macosArm64(), mingwX64(),
        iosX64(), iosArm64(), iosSimulatorArm64(),
    ).forEach { target ->
        val konanName = target.konanTarget.name
        val capName = target.targetName.replaceFirstChar { it.uppercase() }
        val outDir = layout.buildDirectory.dir("vec/$konanName")
        val objFile = outDir.map { it.file("sqlite-vec.o") }
        val staticLib = outDir.map { it.file("libsqlitevec.a") }

        val compileVec = tasks.register<Exec>("compileSqliteVec$capName") {
            dependsOn(fetchVec, unpackHeaders)
            inputs.dir(cinteropDir)
            inputs.dir(headersDir)
            outputs.file(objFile)
            doFirst {
                objFile.get().asFile.parentFile.mkdirs()
                // Arguments go through a clang response file: run_konan swallows `-DFOO=1` passed
                // directly (its JVM launcher takes them for system properties), which would
                // silently drop -DSQLITE_CORE and build a loadable extension instead of a static
                // one. This happens on every host, not only Windows.
                fun q(f: File) = "\"" + f.absolutePath.replace('\\', '/') + "\""
                val rsp = objFile.get().asFile.resolveSibling("clang-args.rsp")
                rsp.writeText(
                    listOf(
                        "-O2", "-DSQLITE_CORE=1", "-DSQLITE_VEC_STATIC=1",
                        "-I" + headersDir.get().asFile.absolutePath,
                        "-I" + cinteropDir.absolutePath,
                        "-c", q(amalgamation), "-o", q(objFile.get().asFile),
                    ).joinToString("\n"),
                )
                commandLine(runKonan() + listOf("clang", "clang", konanName, "@" + rsp.absolutePath))
            }
        }
        val archiveVec = tasks.register<Exec>("archiveSqliteVec$capName") {
            dependsOn(compileVec)
            inputs.file(objFile)
            outputs.file(staticLib)
            doFirst {
                commandLine(
                    runKonan() + listOf(
                        "llvm", "llvm-ar", "rcs",
                        staticLib.get().asFile.absolutePath, objFile.get().asFile.absolutePath,
                    ),
                )
            }
        }

        target.compilations.getByName("main").cinterops {
            register("sqlitevec") {
                defFile(project.file("src/nativeInterop/cinterop/sqlitevec.def"))
                compilerOpts("-I${headersDir.get().asFile.absolutePath}")
                extraOpts("-staticLibrary", "libsqlitevec.a", "-libraryPath", outDir.get().asFile.absolutePath)
            }
        }
        tasks.named("cinteropSqlitevec$capName").configure {
            dependsOn(archiveVec, unpackHeaders)
            // The header and the archive reach cinterop through compilerOpts/extraOpts, which
            // Gradle cannot see; without declaring them the klib stays up to date across a version
            // bump and the old sqlite-vec silently stays linked in.
            inputs.dir(headersDir)
            inputs.file(staticLib)
        }
    }

    // Node: sqlite-vec ships an npm package that resolves the right prebuilt binary per platform,
    // and Kotlin propagates the npm dependency to consumers, so nothing is installed by hand.
    wasmJs { nodejs() }

    applyDefaultHierarchyTemplate()

    sourceSets {
        val commonMain by getting {
            dependencies {
                api("io.github.kormium:kormium-sqlite-spi:$kormiumVersion")
            }
        }
        val wasmJsMain by getting {
            dependencies {
                implementation(npm("sqlite-vec", vecVersion))
            }
        }
        val nativeTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation("io.github.kormium:kormium-sqlite:$kormiumVersion")
            }
        }
    }
}
