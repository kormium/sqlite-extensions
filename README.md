# sqlite-extensions

SQLite extensions packaged for [Kormium](https://github.com/kormium/kormium). Add a dependency,
name it in the `sqlite { }` block, done:

```kotlin
dependencies {
    implementation("io.github.kormium:kormium-sqlite-vec:<version>")
}
```

```kotlin
val db = createSqliteDatabase("app.db", poolSize = 4) {
    sqlite { extension(SqliteVec) }
}

db.autocommit {
    executeUpdate("create virtual table items using vec0(embedding float[4])", emptyMap(), emptyList())
}
```

## Packages

| Artifact | Extension | Platforms |
| --- | --- | --- |
| `kormium-sqlite-vec` | [sqlite-vec](https://github.com/asg017/sqlite-vec) — vector search | Kotlin/Native, iOS, Node |
| `kormium-sqlite-uuid` | SQLite's `ext/misc/uuid.c` — RFC-4122 UUIDs | Kotlin/Native, iOS |
| `kormium-sqlite-regexp` | SQLite's `ext/misc/regexp.c` — the `REGEXP` operator | Kotlin/Native, iOS |
| `kormium-sqlite-series` | SQLite's `ext/misc/series.c` — `generate_series()` | Kotlin/Native, iOS |
| `kormium-sqlite-sha1` | SQLite's `ext/misc/sha1.c` — `sha1()`, `sha1_query()` | Kotlin/Native, iOS |
| `kormium-sqlite-decimal` | SQLite's `ext/misc/decimal.c` — exact decimal arithmetic | Kotlin/Native, iOS |
| `kormium-sqlite-lines` | [sqlite-lines](https://github.com/asg017/sqlite-lines) — read a file or blob line by line | Kotlin/Native, iOS |
| `kormium-sqlite-path` | [sqlite-path](https://github.com/asg017/sqlite-path) — parse and build filesystem paths | Kotlin/Native, iOS |

The `ext/misc` ones are Kotlin/Native only because SQLite distributes them as source: there is no
prebuilt binary to load on the JVM or Node, so they are linked into the binary instead.

## Adding a package

A package is a declaration, not a copy of the build logic — that lives once in
`buildSrc`, in the `kormium-sqlite-extension` convention plugin:

```kotlin
plugins { id("kormium-sqlite-extension") }

sqliteExtension {
    extensionName = "sqlite-uuid"
    entryPoint = "sqlite3_uuid_init"
    sourceUrl = "https://raw.githubusercontent.com/sqlite/sqlite/version-3.53.4/ext/misc/uuid.c"
    sourceFile = "uuid.c"
}
```

The plugin fetches the C, compiles it with the Kotlin/Native toolchain, archives it, generates the
cinterop that registers the entry point, and wires publishing. The package supplies the
`SqliteExtension` object and, if the extension ships a prebuilt binary for Node, a `wasmJs` target.

## Sample

`sample/` puts four of them — vec, uuid, regexp and series — in one database, generating rows with
`generate_series`, keying them with `uuid()`, indexing them in a `vec0` table and filtering with
`REGEXP`. It is also the integration test for the property the whole design rests on: four packages,
one SQLite, no combination artifact.

## How a package is built

Kormium ships no extensions and curates no list of them
([ADR 0013](https://github.com/kormium/kormium/blob/main/docs/adr/0013-sqlite-extensions.md)); this
repository is one publisher among possible others, using the same public SPI anyone can use.

The shape that matters is that **a package carries only itself, never a SQLite of its own**:

- **Kotlin/Native and iOS** — the C is compiled with `-DSQLITE_CORE` into this package's own static
  library, so its `sqlite3_*` references stay unresolved and the final link satisfies them from the
  `libsqlite3.a` already inside `kormium-sqlite`'s cinterop klib. One SQLite in the process, and
  extensions from different packages compose. Registration happens in `beforeOpen`, via
  `sqlite3_auto_extension`, before the driver opens its pool.
- **Node** — the extension's own npm package resolves a prebuilt binary per platform, and it is
  loaded into the connection in `install`. Kotlin propagates the npm dependency to consumers, so
  nothing is installed by hand.

The SQLite headers a package compiles against come from the Kormium release it targets, published
there as a `sqlite-headers` artifact, so the two cannot drift apart.

`supportedEngines` says where a package works. A driver checks it while opening the database, so
using a package on a platform it was never built for fails at `createSqliteDatabase` with its name
in the message — not as `no such module` on some later query.

## What cannot be packaged this way

Static linking into a Kotlin/Native binary needs C with no unbundled native dependencies. Two limits
found by trying:

- **`sqlite-url` needs libcurl.** Statically linking curl and its TLS stack for seven targets is a
  project of its own, not another extension. Left out.
- **Rust and Go extensions** (`sqlite-regex`, `sqlite-ulid`, `sqlite-xsv`, `sqlite-jsonschema`,
  `sqlite-fastrand`, `sqlite-http`, `sqlite-html`) cannot be linked into a K/N binary at all. They
  are not lost, though: on the JVM and Node nothing is linked — a prebuilt binary is loaded — so
  those platforms can have them. That needs a different shape of package, and is not built yet.

## Not yet covered

`kormium-sqlite-vec` is built for Kotlin/Native, iOS and Node. Still to come:

- **JVM** — needs sqlite-vec's per-platform binaries vendored into the jar and extracted at load.
- **Android** — the `.so` per ABI; Kormium's JNI shim (`kormium-sqlite-android-ext`) does the
  registration, so the package contributes no C.
- **Browser** — an extension there is a *different* engine build; it needs
  [sqlite-wasm-engines](https://github.com/kormium/sqlite-wasm-engines)' loadable build plus a
  matching Emscripten `SIDE_MODULE`.

## Developing

With a Kormium checkout next to this one the build uses it directly (composite build), so a change
to the SPI shows up here immediately. Without it — as in CI — the published artifacts are resolved.
Force the standalone path with `-Pkormium.ignoreSibling=true`.

Note that `./gradlew build` only works on macOS: the shared native source set needs the cinterop of
every declared target, and the Apple ones cannot be compiled elsewhere. Build task by task on Linux,
as CI does.

```sh
./gradlew build          # needs the Kotlin/Native toolchain; downloads sqlite-vec at build time
./gradlew :kormium-sqlite-vec:linuxX64Test
```

## Releasing

Manual, like Kormium's own: publishing to Maven Central cannot be undone, so nothing fires from a
push. Bump `version` in `gradle.properties`, tag, then dispatch the `publish` workflow with that
tag. It runs on macOS — the only host where every declared target builds, so the packages go out as
one complete set rather than a Linux-shaped subset with the Apple klibs missing — and it runs
`apiCheck` and the tests, including the four-extension sample, before publishing anything.

It needs the same secrets as Kormium: `MAVEN_CENTRAL_USERNAME`, `MAVEN_CENTRAL_PASSWORD`,
`SIGNING_IN_MEMORY_KEY`, `SIGNING_IN_MEMORY_KEY_PASSWORD`. Like there, the deployment lands in the
Central Portal as `USER_MANAGED` and waits for a **Publish** click.

## Licence

Apache 2.0. sqlite-vec is MIT/Apache-2.0; SQLite itself is public domain.
