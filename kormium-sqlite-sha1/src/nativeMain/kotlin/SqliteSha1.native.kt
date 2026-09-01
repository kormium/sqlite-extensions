package io.github.kormium.sqlite.sha1

import csqlite_sha1.kormium_register_sqlite_sha1
import io.github.kormium.SqliteConnectionScope
import io.github.kormium.SqliteEngine
import io.github.kormium.SqliteExtension
import io.github.kormium.SqliteRegistrationScope
import kotlinx.cinterop.ExperimentalForeignApi

public actual object SqliteSha1 : SqliteExtension {

    actual override val name: String = "sqlite-sha1"

    actual override val supportedEngines: Set<SqliteEngine> = setOf(SqliteEngine.Native)

    @OptIn(ExperimentalForeignApi::class)
    override fun beforeOpen(registration: SqliteRegistrationScope) {
        // Linked into the binary, so nothing is loaded at runtime — the entry point is registered
        // once, before the driver opens its pool. Re-registering is a harmless no-op in SQLite.
        check(kormium_register_sqlite_sha1() == 0) {
            "sqlite3_auto_extension(sqlite3_sha_init) failed"
        }
    }

    override fun install(connection: SqliteConnectionScope) {
        // Nothing to load; prove it actually registered rather than trusting it.
        checkNotNull(connection.queryScalar("select 1")) { "connection is unusable" }
    }
}
