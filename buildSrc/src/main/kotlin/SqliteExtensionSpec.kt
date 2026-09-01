import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property

/**
 * Describes one SQLite extension package. Everything that differs between packages lives here;
 * the rest — fetching the C, compiling it with the Kotlin/Native toolchain, archiving it, wiring
 * cinterop, publishing — is identical and lives in the `kormium-sqlite-extension` convention
 * plugin.
 *
 * The point is that adding an extension should be a declaration, not another copy of 180 lines of
 * build logic that then drifts.
 */
abstract class SqliteExtensionSpec {

    /** The extension's own name, e.g. `sqlite-vec`. Used in messages and to derive symbol names. */
    abstract val extensionName: Property<String>

    /** SQLite's entry point, e.g. `sqlite3_vec_init`. SQLite derives it from the file name. */
    abstract val entryPoint: Property<String>

    /**
     * Where the C comes from — a `.c` file or a `.zip`/`.tar.gz` containing one. Fetched at build
     * time rather than vendored, so the version is a property instead of a commit.
     */
    abstract val sourceUrl: Property<String>

    /** The C file to compile, as named inside [sourceUrl] (or the file name if it is a plain `.c`). */
    abstract val sourceFile: Property<String>

    /** Extra `-D` flags this extension needs, beyond `-DSQLITE_CORE`. */
    abstract val extraDefines: ListProperty<String>

    /** The cinterop package name, derived from [extensionName] unless set. Must be unique. */
    abstract val cinteropPackage: Property<String>

    /** The generated C registration function, derived from [extensionName] unless set. */
    abstract val registerFunction: Property<String>
}
