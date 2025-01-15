package io.github.hansanto.quipoquiz.util.environment

import dev.kord.common.entity.DiscordPartialEmoji
import kotlinx.serialization.json.Json

private val emojiJsonParser = Json {
    ignoreUnknownKeys = false
    explicitNulls = false
}

/**
 * Load an emoji from the environment.
 * @param key Environmental variable to get the value for.
 * @param defaultValue Default value to use if the environmental variable doesn't exist.
 * @return The value of the environmental variable.
 */
fun emojiEnv(key: String, defaultValue: () -> DiscordPartialEmoji): Lazy<DiscordPartialEmoji> {
    return env(
        key = key,
        converter = {
            emojiJsonParser.decodeFromString(DiscordPartialEmoji.serializer(), it)
        },
        defaultValue = defaultValue
    )
}
