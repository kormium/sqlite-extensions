plugins {
    id("kormium-sqlite-extension")
}

// SQLite's own ext/misc/sha1.c — `sha1()` and `sha1_query()` hashes
// Public domain, one file, no dependencies. Fetched from the SQLite tag matching the driver.
sqliteExtension {
    extensionName = "sqlite-sha1"
    entryPoint = "sqlite3_sha_init"
    sourceUrl = "https://raw.githubusercontent.com/sqlite/sqlite/version-3.53.4/ext/misc/sha1.c"
    sourceFile = "sha1.c"
}
