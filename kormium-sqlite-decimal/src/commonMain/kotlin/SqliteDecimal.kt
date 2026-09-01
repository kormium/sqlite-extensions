package io.github.kormium.sqlite.decimal

import io.github.kormium.SqliteEngine
import io.github.kormium.SqliteExtension

/**
 * SQLite's own `ext/misc/decimal.c` — exact decimal arithmetic: `decimal_add`, `decimal_mul`, `decimal_cmp` — packaged as a Kormium extension.
 *
 * ```kotlin
 * val db = createSqliteDatabase("app.db") {
 *     sqlite { extension(SqliteDecimal) }
 * }
 * ```
 *
 * Kotlin/Native and iOS only: SQLite's contributed extensions are distributed as source, with no
 * prebuilt binary to load on the JVM or Node, so this package links the C into the binary and
 * registers it before the pool opens.
 */
// The abstract members are restated here because an `expect object` is checked on its own during
// metadata compilation; inheriting them from the interface is not enough.
public expect object SqliteDecimal : SqliteExtension {
    override val name: String
    override val supportedEngines: Set<SqliteEngine>
}
