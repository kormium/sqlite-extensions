package io.github.kormium.sqlite.vec

import io.github.kormium.SqliteExtension

/**
 * [sqlite-vec](https://github.com/asg017/sqlite-vec) — vector search in SQLite — as a Kormium
 * extension.
 *
 * ```kotlin
 * val db = createSqliteDatabase("app.db") {
 *     sqlite { extension(SqliteVec) }
 * }
 * ```
 *
 * How it reaches SQLite differs per platform, which is the whole reason
 * [io.github.kormium.SqliteExtension] has two phases: on Kotlin/Native and iOS the extension is
 * linked into the binary and registered before the pool opens, on Node it is a prebuilt library
 * loaded into the connection. Callers see neither — they name the object and the package picks.
 */
public expect object SqliteVec : SqliteExtension
