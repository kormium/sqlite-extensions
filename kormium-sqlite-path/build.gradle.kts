plugins {
    id("kormium-sqlite-extension")
}

// sqlite-path — parse and build filesystem paths: `path_dirname()`, `path_basename()`, `path_join()`
// https://github.com/asg017/sqlite-path
val upstream: String = providers.gradleProperty("sqlitePathVersion").get()

sqliteExtension {
    extensionName = "sqlite-path"
    entryPoint = "sqlite3_path_init"
    sourceUrl = "https://raw.githubusercontent.com/asg017/sqlite-path/v$upstream/sqlite-path.c"
    sourceFile = "sqlite-path.c"
    // sqlite-path parses paths with cwalk, which it vendors rather than amalgamates.
    extraSourceUrls = listOf(
        "https://raw.githubusercontent.com/likle/cwalk/master/src/cwalk.c",
        "https://raw.githubusercontent.com/likle/cwalk/master/include/cwalk.h",
    )
    extraSourceFiles = listOf("cwalk.c")
    // Upstream's own build injects these; the source uses them for its `path_version()`
    // and debug functions and will not compile without them. Escaped so the quotes survive
    // the clang response file and reach the compiler as string literals.
    extraDefines = listOf(
        "-DSQLITE_PATH_VERSION=\\\"v$upstream\\\"",
        "-DSQLITE_PATH_DATE=\\\"\\\"",
        "-DSQLITE_PATH_SOURCE=\\\"asg017/sqlite-path\\\"",
        "-DSQLITE_PATH_CWALK_VERSION=\\\"likle/cwalk\\\"",
    )
}
