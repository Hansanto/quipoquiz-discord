package io.github.hansanto.quipoquiz.util.environment

/**
 * Returns the value of an environmental variable, loading from a `.env` file in the current working directory if
 * possible.
 *
 * @param key Environmental variable to get the value for.
 * @return The value of the environmental variable, or `null` if it doesn't exist.
 */
actual fun envOrNull(key: String): String? {
    return js("process.env[key]") as? String
}
