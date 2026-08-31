@file:OptIn(io.github.kormium.DelicateKormiumApi::class)

package io.github.kormium.sqlite.vec

import io.github.kormium.QueryException
import io.github.kormium.autocommit
import io.github.kormium.createSqliteDatabase
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

/**
 * One test method, not several: registration is process-global, so the negative control is only
 * meaningful before the first one and the steps need a known order.
 */
class SqliteVecTest {

    @Test
    fun vecReachesEveryPooledConnection() {
        // Nothing registered yet.
        createSqliteDatabase().use { db ->
            assertFailsWith<QueryException> {
                db.autocommit { execute("select vec_version()", emptyMap(), emptyList()) { it.getString(0) } }
            }
        }

        createSqliteDatabase(poolSize = 3) { sqlite { extension(SqliteVec) } }.use { db ->
            val version = db.autocommit {
                execute("select vec_version()", emptyMap(), emptyList()) { it.getString(0) }
            }.single()
            assertTrue(version!!.startsWith("v0."), "got $version")

            db.autocommit {
                executeUpdate("create virtual table items using vec0(embedding float[4])", emptyMap(), emptyList())
                executeUpdate(
                    "insert into items(rowid, embedding) values (1, '[0.1,0.2,0.3,0.4]'), " +
                        "(2, '[0.9,0.9,0.9,0.9]')",
                    emptyMap(),
                    emptyList(),
                )
            }
            val nearest = db.autocommit {
                execute(
                    "select rowid, distance from items where embedding match '[0.1,0.2,0.3,0.5]' " +
                        "and k = 1",
                    emptyMap(),
                    emptyList(),
                ) { it.getLong(0) to it.getDouble(1) }
            }.single()
            assertEquals(1L, nearest.first)
            assertTrue(nearest.second!! > 0.0)
        }
    }
}
