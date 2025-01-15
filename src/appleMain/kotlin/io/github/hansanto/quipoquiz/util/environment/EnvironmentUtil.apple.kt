package io.github.hansanto.quipoquiz.util.environment

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.toKString
import platform.posix.getenv

/**
 * Returns the value of an environmental variable.
 * @param key Environmental variable to get the value for.
 * @return The value of the environmental variable, or `null` if it doesn't exist.
 */
@OptIn(ExperimentalForeignApi::class)
actual fun envOrNull(key: String): String? {
    return getenv(key)?.toKString()
}
