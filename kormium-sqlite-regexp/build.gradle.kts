plugins {
    id("kormium-sqlite-extension")
}

// SQLite's own ext/misc/regexp.c — the `REGEXP` operator and a `regexp()` function
// Public domain, one file, no dependencies. Fetched from the SQLite tag matching the driver.
sqliteExtension {
    extensionName = "sqlite-regexp"
    entryPoint = "sqlite3_regexp_init"
    sourceUrl = "https://raw.githubusercontent.com/sqlite/sqlite/version-3.53.4/ext/misc/regexp.c"
    sourceFile = "regexp.c"
}
