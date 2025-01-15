package io.github.hansanto.quipoquiz.util.environment

import dev.kord.x.emoji.Emojis.it
import kotlin.time.Duration

/**
 * Requires the value to be greater than the minimum.
 * @param min Minimum value.
 */
internal fun Int.requireGreaterThan(min: Int) {
    require(this > min) { "The value must be greater than $min" }
}

/**
 * Requires the value to be greater than the minimum.
 * @param min Minimum value.
 */
internal fun Duration.requireGreaterThan(min: Duration) {
    require(this > min) { "The value must be greater than $min" }
}

/**
 * Returns the value of an environmental variable.
 * @param key Environmental variable to get the value for.
 * @return The value of the environmental variable, or `null` if it doesn't exist.
 */
expect fun envOrNull(key: String): String?

/**
 * Returns the value of an environmental variable using delegation.
 * @param key Environmental variable to get the value for.
 * @param converter Function to convert the value of the environmental variable to the required type.
 * @param defaultValue Default value to use if the environmental variable doesn't exist.
 * @param validator Function to validate the converted value.
 * @return The value of the environmental variable.
 */
inline fun <reified T> env(
    key: String,
    crossinline converter: (String) -> T,
    crossinline defaultValue: () -> T = { error("The environment variable [$key] must be set") },
    crossinline validator: (T) -> Unit = {}
): Lazy<T> = lazy {
    val envValue = envOrNull(key) ?: return@lazy defaultValue()
    val convertedValue = runCatching { converter(envValue) }.getOrElse { error ->
        throw IllegalArgumentException(
            "The environment variable [$key] could not be converted to the required type [${T::class.simpleName}].",
            error
        )
    }
    convertedValue.also {
        runCatching {
            validator(it)
        }.onFailure { error ->
            throw IllegalArgumentException(
                "The environment variable [$key] has an invalid value [$envValue].",
                error
            )
        }
    }
}
