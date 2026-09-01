package io.github.kormium.sqlite.vec

import io.github.kormium.SqliteEngine
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
// The abstract members are restated here because an `expect object` is checked on its own during
// metadata compilation: inheriting them from the interface is not enough, the expectation has to
// carry them. `beforeOpen` and `install` are not listed — they have defaults in the interface, and
// each platform overrides only the one it needs.
public expect object SqliteVec : SqliteExtension {
    override val name: String
    override val supportedEngines: Set<SqliteEngine>
}
