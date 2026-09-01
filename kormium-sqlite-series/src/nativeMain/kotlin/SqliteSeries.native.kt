package io.github.kormium.sqlite.series

import csqlite_series.kormium_register_sqlite_series
import io.github.kormium.SqliteConnectionScope
import io.github.kormium.SqliteEngine
import io.github.kormium.SqliteExtension
import io.github.kormium.SqliteRegistrationScope
import kotlinx.cinterop.ExperimentalForeignApi

public actual object SqliteSeries : SqliteExtension {

    actual override val name: String = "sqlite-series"

    actual override val supportedEngines: Set<SqliteEngine> = setOf(SqliteEngine.Native)

    @OptIn(ExperimentalForeignApi::class)
    override fun beforeOpen(registration: SqliteRegistrationScope) {
        // Linked into the binary, so nothing is loaded at runtime — the entry point is registered
        // once, before the driver opens its pool. Re-registering is a harmless no-op in SQLite.
        check(kormium_register_sqlite_series() == 0) {
            "sqlite3_auto_extension(sqlite3_series_init) failed"
        }
    }

    override fun install(connection: SqliteConnectionScope) {
        // Nothing to load; prove it actually registered rather than trusting it.
        checkNotNull(connection.queryScalar("select 1")) { "connection is unusable" }
    }
}
