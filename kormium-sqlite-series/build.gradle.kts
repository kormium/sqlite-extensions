plugins {
    id("kormium-sqlite-extension")
}

// SQLite's own ext/misc/series.c — `generate_series()`, a table-valued function over a range
// Public domain, one file, no dependencies. Fetched from the SQLite tag matching the driver.
sqliteExtension {
    extensionName = "sqlite-series"
    entryPoint = "sqlite3_series_init"
    sourceUrl = "https://raw.githubusercontent.com/sqlite/sqlite/version-3.53.4/ext/misc/series.c"
    sourceFile = "series.c"
}
