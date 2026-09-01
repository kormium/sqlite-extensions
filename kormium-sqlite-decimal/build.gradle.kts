plugins {
    id("kormium-sqlite-extension")
}

// SQLite's own ext/misc/decimal.c — exact decimal arithmetic: `decimal_add`, `decimal_mul`, `decimal_cmp`
// Public domain, one file, no dependencies. Fetched from the SQLite tag matching the driver.
sqliteExtension {
    extensionName = "sqlite-decimal"
    entryPoint = "sqlite3_decimal_init"
    sourceUrl = "https://raw.githubusercontent.com/sqlite/sqlite/version-3.53.4/ext/misc/decimal.c"
    sourceFile = "decimal.c"
}
