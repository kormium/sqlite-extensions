package io.github.kormium.sqlite.path

import io.github.kormium.SqliteEngine
import io.github.kormium.SqliteExtension

/**
 * [sqlite-path](https://github.com/asg017/sqlite-path) — parse and build filesystem paths: `path_dirname()`, `path_basename()`, `path_join()` — packaged as a Kormium extension.
 *
 * ```kotlin
 * val db = createSqliteDatabase("app.db") {
 *     sqlite { extension(SqlitePath) }
 * }
 * ```
 *
 * Kotlin/Native and iOS: the C is linked into the binary and registered before the pool opens.
 * Upstream also publishes prebuilt binaries for other runtimes; wiring those up is a separate
 * matter for the JVM and Node, and this package does not claim them.
 */
// The abstract members are restated here because an `expect object` is checked on its own during
// metadata compilation; inheriting them from the interface is not enough.
public expect object SqlitePath : SqliteExtension {
    override val name: String
    override val supportedEngines: Set<SqliteEngine>
}
