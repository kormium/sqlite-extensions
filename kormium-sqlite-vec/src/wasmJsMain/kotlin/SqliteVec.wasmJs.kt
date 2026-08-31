package io.github.kormium.sqlite.vec

import io.github.kormium.SqliteConnectionScope
import io.github.kormium.SqliteEngine
import io.github.kormium.SqliteExtension

/**
 * The `sqlite-vec` npm package is a resolver: it depends on one prebuilt binary per platform and
 * `getLoadablePath()` returns the right one. Kotlin propagates the npm dependency to consumers, so
 * an application only adds this Gradle dependency.
 */
private fun loadablePath(): String = js("require('sqlite-vec').getLoadablePath()")

/**
 * Node: sqlite-vec ships as a prebuilt loadable library, so it is loaded into the connection rather
 * than linked in. better-sqlite3 holds a single connection, so this runs once.
 */
public actual object SqliteVec : SqliteExtension {

    override val name: String = "sqlite-vec"

    override val supportedEngines: Set<SqliteEngine> = setOf(SqliteEngine.BetterSqlite3)

    override fun install(connection: SqliteConnectionScope) {
        connection.loadLibrary(loadablePath())
    }
}
