package io.github.hansanto.quipoquiz.config

import dev.kord.common.entity.Snowflake
import io.github.hansanto.quipoquiz.Language
import io.github.hansanto.quipoquiz.from
import io.github.hansanto.quipoquiz.util.environment.env
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

/**
 * Configuration about Discord bot.
 */
object BotConfiguration {

    /**
     * Default language for the user if not specified.
     * If the user starts a command without specifying a language, this language will be used.
     */
    val defaultLanguage: Language by env(
        key = "BOT_LANGUAGE_DEFAULT",
        converter = { arg ->
            Language.from(arg) ?: error("The language $arg is not supported.")
        },
        defaultValue = { Language.ENGLISH }
    )

    /**
     * The interval to refresh an interaction.
     * Each interval, the bot will verify if a new change is applicable and send it to Discord.
     */
    val refreshInterval: Duration by env(
        key = "BOT_REFRESH_INTERVAL",
        converter = { it.toLong().milliseconds },
        defaultValue = { 1.seconds }
    ) {
        require(it > 0.seconds) { "The value must be greater than 0 seconds" }
    }

    /**
     * Discord bot token.
     */
    val token: String by env(
        key = "BOT_TOKEN",
        converter = { it }
    )

    /**
     * ID of the Discord server to test new features.
     */
    val devGuildId: Snowflake? by env(
        key = "BOT_DEV_GUILD",
        converter = { Snowflake(it.toLong()) },
        defaultValue = { null }
    )
}
