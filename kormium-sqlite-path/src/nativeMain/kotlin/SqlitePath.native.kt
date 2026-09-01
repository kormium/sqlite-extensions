package io.github.kormium.sqlite.path

import csqlite_path.kormium_register_sqlite_path
import io.github.kormium.SqliteConnectionScope
import io.github.kormium.SqliteEngine
import io.github.kormium.SqliteExtension
import io.github.kormium.SqliteRegistrationScope
import kotlinx.cinterop.ExperimentalForeignApi

public actual object SqlitePath : SqliteExtension {

    actual override val name: String = "sqlite-path"

    actual override val supportedEngines: Set<SqliteEngine> = setOf(SqliteEngine.Native)

    @OptIn(ExperimentalForeignApi::class)
    override fun beforeOpen(registration: SqliteRegistrationScope) {
        check(kormium_register_sqlite_path() == 0) {
            "sqlite3_auto_extension(sqlite3_path_init) failed"
        }
    }
}
