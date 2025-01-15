package io.github.hansanto.quipoquiz.extension

import kotlinx.io.buffered
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.io.readString

/**
 * `true` if the path is a directory, `false` otherwise.
 */
val Path.isDirectory: Boolean
    get() = SystemFileSystem.metadataOrNull(this)?.isDirectory == true

/**
 * `true` if the path is a regular file, `false` otherwise.
 */
val Path.isRegularFile: Boolean
    get() = SystemFileSystem.metadataOrNull(this)?.isRegularFile == true

/**
 * Check if a file or directory exists at the path.
 * @receiver Path
 * @return `true` if the file or directory exists, `false` otherwise.
 */
fun Path.exists(): Boolean {
    return SystemFileSystem.exists(this)
}

/**
 * Resolve the given parts against this path.
 * @receiver Base path.
 * @param parts List of sub-paths to resolve.
 * @return The resolved path.
 */
fun Path.resolve(vararg parts: String): Path {
    return Path(this, *parts)
}

/**
 * Create directory at the path.
 * Will create parent directories if they do not exist.
 * @receiver Path of the future directory.
 */
fun Path.createDirectories() {
    SystemFileSystem.createDirectories(this, false)
}

/**
 * Delete the file or directory at the path.
 * If the path is a directory, it will delete the directory and all its contents.
 * If the path does not exist, it will do nothing.
 * @receiver Path to delete.
 */
fun Path.delete() {
    if (isDirectory) {
        deleteDirectoryFiles()
    }
    SystemFileSystem.delete(this, false)
}

/**
 * Delete all files in the directory.
 * If a subdirectory is found, its files will be deleted recursively.
 * @receiver Path to the directory.
 */
fun Path.deleteDirectoryFiles() {
    SystemFileSystem.list(this).forEach { it.delete() }
}

/**
 * Read the content of the file as a string.
 * @receiver Path to the file.
 * @return The content of the file as a string.
 */
fun Path.readText(): String {
    return SystemFileSystem.source(this).buffered().use { it.readString() }
}

/**
 * Write the text to the file.
 * @receiver Path to the file.
 * @param text Content to write.
 * @param append `true` to append the text to the file, `false` to overwrite the file.
 */
fun Path.writeText(text: String, append: Boolean = false) {
    SystemFileSystem.sink(this, append).buffered().use { it.write(text.encodeToByteArray()) }
}
