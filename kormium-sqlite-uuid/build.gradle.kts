plugins {
    id("kormium-sqlite-extension")
}

// SQLite's own ext/misc/uuid.c — RFC-4122 UUIDs: `uuid()`, `uuid_str()`, `uuid_blob()`
// Public domain, one file, no dependencies. Fetched from the SQLite tag matching the driver.
sqliteExtension {
    extensionName = "sqlite-uuid"
    entryPoint = "sqlite3_uuid_init"
    sourceUrl = "https://raw.githubusercontent.com/sqlite/sqlite/version-3.53.4/ext/misc/uuid.c"
    sourceFile = "uuid.c"
}
