package io.github.kormium.sqlite.lines

import csqlite_lines.kormium_register_sqlite_lines
import io.github.kormium.SqliteConnectionScope
import io.github.kormium.SqliteEngine
import io.github.kormium.SqliteExtension
import io.github.kormium.SqliteRegistrationScope
import kotlinx.cinterop.ExperimentalForeignApi

public actual object SqliteLines : SqliteExtension {

    actual override val name: String = "sqlite-lines"

    actual override val supportedEngines: Set<SqliteEngine> = setOf(SqliteEngine.Native)

    @OptIn(ExperimentalForeignApi::class)
    override fun beforeOpen(registration: SqliteRegistrationScope) {
        check(kormium_register_sqlite_lines() == 0) {
            "sqlite3_auto_extension(sqlite3_lines_init) failed"
        }
    }
}
