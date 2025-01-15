package io.github.hansanto.quipoquiz.config

import dev.kord.common.entity.DiscordPartialEmoji
import dev.kord.common.entity.Snowflake
import dev.kord.core.entity.ReactionEmoji
import dev.kord.x.emoji.Emojis
import io.github.hansanto.quipoquiz.discord.framework.extension.toDiscordPartialEmoji
import io.github.hansanto.quipoquiz.util.environment.emojiEnv

/**
 * Configuration about Discord bot.
 */
object EmojiConfiguration {

    val error: DiscordPartialEmoji by emojiEnv(
        key = "BOT_EMOJI_ERROR",
        defaultValue = { Emojis.warning.toDiscordPartialEmoji() }
    )

    val validate: DiscordPartialEmoji by emojiEnv(
        key = "BOT_EMOJI_VALIDATE",
        defaultValue = { Emojis.thumbsup.toDiscordPartialEmoji() }
    )

    val voteCounter: DiscordPartialEmoji by emojiEnv(
        key = "BOT_EMOJI_VOTE_COUNTER",
        defaultValue = { Emojis.bustInSilhouette.toDiscordPartialEmoji() }
    )

    val correct: DiscordPartialEmoji by emojiEnv(
        key = "BOT_EMOJI_TRUE",
        defaultValue = {
            ReactionEmoji.Custom(
                id = Snowflake(868390746158399488),
                name = "true",
                isAnimated = false
            ).toDiscordPartialEmoji()
        }
    )

    val correctWithBackground: DiscordPartialEmoji by emojiEnv(
        key = "BOT_EMOJI_TRUE_WITH_BACKGROUND",
        defaultValue = {
            ReactionEmoji.Custom(
                id = Snowflake(868535418008776754),
                name = "true",
                isAnimated = false
            ).toDiscordPartialEmoji()
        }
    )

    val incorrect: DiscordPartialEmoji by emojiEnv(
        key = "BOT_EMOJI_FALSE",
        defaultValue = {
            ReactionEmoji.Custom(
                id = Snowflake(868382785147117578),
                name = "false",
                isAnimated = false
            ).toDiscordPartialEmoji()
        }
    )

    val incorrectWithBackground: DiscordPartialEmoji by emojiEnv(
        key = "BOT_EMOJI_FALSE_WITH_BACKGROUND",
        defaultValue = {
            ReactionEmoji.Custom(
                id = Snowflake(868537587395076116),
                name = "false",
                isAnimated = false
            ).toDiscordPartialEmoji()
        }
    )

    val mcq: DiscordPartialEmoji by emojiEnv(
        key = "BOT_EMOJI_MCQ",
        defaultValue = { Emojis.partAlternationMark.toDiscordPartialEmoji() }
    )

    val mcqWithBackground: DiscordPartialEmoji by emojiEnv(
        key = "BOT_EMOJI_MCQ_WITH_BACKGROUND",
        defaultValue = {
            Emojis.`1234`.toDiscordPartialEmoji()
        }
    )

    val previous: DiscordPartialEmoji by emojiEnv(
        key = "BOT_EMOJI_PREVIOUS",
        defaultValue = {
            val snowflake = Snowflake(868391899617181698)
            ReactionEmoji.Custom(
                id = snowflake,
                name = "previous",
                isAnimated = false
            ).toDiscordPartialEmoji()
        }
    )

    val next: DiscordPartialEmoji by emojiEnv(
        key = "BOT_EMOJI_NEXT",
        defaultValue = {
            val snowflake = Snowflake(868391899508125706)
            ReactionEmoji.Custom(
                id = snowflake,
                name = "next",
                isAnimated = false
            ).toDiscordPartialEmoji()
        }
    )

    val exit: DiscordPartialEmoji by emojiEnv(
        key = "BOT_EMOJI_EXIT",
        defaultValue = {
            val snowflake = Snowflake(867872427655954442)
            ReactionEmoji.Custom(
                id = snowflake,
                name = "end_game",
                isAnimated = false
            ).toDiscordPartialEmoji()
        }
    )
}
