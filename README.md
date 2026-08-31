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

## Not yet covered

`kormium-sqlite-vec` is built for Kotlin/Native, iOS and Node. Still to come:

- **JVM** — needs sqlite-vec's per-platform binaries vendored into the jar and extracted at load.
- **Android** — the `.so` per ABI; Kormium's JNI shim (`kormium-sqlite-android-ext`) does the
  registration, so the package contributes no C.
- **Browser** — an extension there is a *different* engine build; it needs
  [sqlite-wasm-engines](https://github.com/kormium/sqlite-wasm-engines)' loadable build plus a
  matching Emscripten `SIDE_MODULE`.

## Developing

> **Not yet buildable standalone.** `kormium-sqlite-spi` and the `sqlite-headers` artifact are not
> on Maven Central yet — they land with the next Kormium release. Until then the build needs a
> Kormium checkout beside this one, which is what CI does too.

With a Kormium checkout next to this one the build uses it directly (composite build), so a change
to the SPI shows up here immediately. Without it, published artifacts are resolved instead — which
will start working once the SPI ships. Force the standalone path with `-Pkormium.ignoreSibling=true`.

```sh
./gradlew build          # needs the Kotlin/Native toolchain; downloads sqlite-vec at build time
./gradlew :kormium-sqlite-vec:linuxX64Test
```

## Licence

Apache 2.0. sqlite-vec is MIT/Apache-2.0; SQLite itself is public domain.
