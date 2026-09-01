package io.github.kormium.sqlite.uuid

import io.github.kormium.SqliteEngine
import io.github.kormium.SqliteExtension

/**
 * SQLite's own `ext/misc/uuid.c` — RFC-4122 UUIDs: `uuid()`, `uuid_str()`, `uuid_blob()` — packaged as a Kormium extension.
 *
 * ```kotlin
 * val db = createSqliteDatabase("app.db") {
 *     sqlite { extension(SqliteUuid) }
 * }
 * ```
 *
 * Kotlin/Native and iOS only: SQLite's contributed extensions are distributed as source, with no
 * prebuilt binary to load on the JVM or Node, so this package links the C into the binary and
 * registers it before the pool opens.
 */
// The abstract members are restated here because an `expect object` is checked on its own during
// metadata compilation; inheriting them from the interface is not enough.
public expect object SqliteUuid : SqliteExtension {
    override val name: String
    override val supportedEngines: Set<SqliteEngine>
}
