package io.github.kormium.sqlite.vec

import csqlitevec.kormium_register_sqlite_vec
import io.github.kormium.SqliteConnectionScope
import io.github.kormium.SqliteEngine
import io.github.kormium.SqliteExtension
import io.github.kormium.SqliteRegistrationScope
import kotlinx.cinterop.ExperimentalForeignApi

/**
 * Kotlin/Native and iOS: sqlite-vec is compiled into this package's static library and linked into
 * the application, so there is nothing to load at runtime. Registration happens once, before the
 * driver opens its pool.
 *
 * Note this is process-global — every SQLite connection opened after the first database that
 * declares this extension will have `vec0` available, including databases that never asked for it.
 */
public actual object SqliteVec : SqliteExtension {

    override val name: String = "sqlite-vec"

    override val supportedEngines: Set<SqliteEngine> = setOf(SqliteEngine.Native)

    @OptIn(ExperimentalForeignApi::class)
    override fun beforeOpen(registration: SqliteRegistrationScope) {
        // Repeat registration is a harmless no-op in SQLite, so this needs no guard of its own.
        check(kormium_register_sqlite_vec() == 0) {
            "sqlite3_auto_extension(sqlite3_vec_init) failed"
        }
    }

    override fun install(connection: SqliteConnectionScope) {
        checkNotNull(connection.queryScalar("select vec_version()")) {
            "sqlite-vec is linked into this binary but did not register on the connection"
        }
    }
}
