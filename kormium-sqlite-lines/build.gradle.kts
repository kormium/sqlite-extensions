plugins {
    id("kormium-sqlite-extension")
}

// sqlite-lines — read a file or blob line by line: `lines()`, `lines_read()`
// https://github.com/asg017/sqlite-lines
val upstream: String = providers.gradleProperty("sqliteLinesVersion").get()

sqliteExtension {
    extensionName = "sqlite-lines"
    entryPoint = "sqlite3_lines_init"
    sourceUrl = "https://raw.githubusercontent.com/asg017/sqlite-lines/v$upstream/sqlite-lines.c"
    sourceFile = "sqlite-lines.c"
    // Upstream's own build injects these; the source uses them for its `lines_version()`
    // and debug functions and will not compile without them. Escaped so the quotes survive
    // the clang response file and reach the compiler as string literals.
    extraDefines = listOf(
        "-DSQLITE_LINES_VERSION=\\\"v$upstream\\\"",
        "-DSQLITE_LINES_DATE=\\\"\\\"",
        "-DSQLITE_LINES_SOURCE=\\\"asg017/sqlite-lines\\\"",
    )
}
