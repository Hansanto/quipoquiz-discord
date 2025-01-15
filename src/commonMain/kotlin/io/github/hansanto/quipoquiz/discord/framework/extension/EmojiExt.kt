package io.github.hansanto.quipoquiz.discord.framework.extension

import dev.kord.common.entity.DiscordPartialEmoji
import dev.kord.common.entity.optional.optional
import dev.kord.core.entity.ReactionEmoji
import dev.kord.x.emoji.DiscordEmoji
import dev.kord.x.emoji.Emojis

/**
 * List of medal emojis.
 */
private val medalEmojis: List<DiscordPartialEmoji> = listOf(
    Emojis.`1stPlaceMedal`.toDiscordPartialEmoji(),
    Emojis.`2ndPlaceMedal`.toDiscordPartialEmoji(),
    Emojis.`3rdPlaceMedal`.toDiscordPartialEmoji()
)

/**
 * List of medal emojis.
 */
val DiscordPartialEmoji.Companion.medals: List<DiscordPartialEmoji>
    get() = medalEmojis

/**
 * Get the emoji associated with the rank.
 * @param rank The rank.
 * @return The emoji associated with the rank or `null` if the rank is not between 1 and 3.
 */
fun DiscordPartialEmoji.Companion.medalEmojiOrNull(rank: Int): DiscordPartialEmoji? {
    return medals.getOrNull(rank - 1)
}

/**
 * List of number emojis.
 */
private val emojiNumbers: List<DiscordPartialEmoji> = listOf(
    Emojis.zero.toDiscordPartialEmoji(),
    Emojis.one.toDiscordPartialEmoji(),
    Emojis.two.toDiscordPartialEmoji(),
    Emojis.three.toDiscordPartialEmoji(),
    Emojis.four.toDiscordPartialEmoji(),
    Emojis.five.toDiscordPartialEmoji(),
    Emojis.six.toDiscordPartialEmoji(),
    Emojis.seven.toDiscordPartialEmoji(),
    Emojis.eight.toDiscordPartialEmoji(),
    Emojis.nine.toDiscordPartialEmoji(),
    Emojis.keycapTen.toDiscordPartialEmoji()
)

/**
 * List of number emojis.
 */
val DiscordPartialEmoji.Companion.numbers: List<DiscordPartialEmoji>
    get() = emojiNumbers

/**
 * Get the emoji associated with the number.
 * @param number The number.
 * @return The emoji associated with the number or `null` if the number is not between 0 and 10.
 */
fun DiscordPartialEmoji.Companion.numberEmojiOrNull(number: Int): DiscordPartialEmoji? {
    return numbers.getOrNull(number)
}

/**
 * Convert a [ReactionEmoji.Custom] to a [DiscordPartialEmoji].
 * @return The [DiscordPartialEmoji] instance.
 */
fun ReactionEmoji.Custom.toDiscordPartialEmoji(): DiscordPartialEmoji {
    return DiscordPartialEmoji(id = id, name = name, animated = isAnimated.optional())
}

/**
 * Convert a [DiscordEmoji] to a [DiscordPartialEmoji].
 * @return The [DiscordPartialEmoji] instance.
 */
fun DiscordEmoji.toDiscordPartialEmoji(): DiscordPartialEmoji {
    return DiscordPartialEmoji(id = null, name = this.unicode)
}

/**
 * Create a string format to display the emoji in Discord.
 * @return the string format to display the emoji in Discord.
 */
fun DiscordPartialEmoji.visualizer(): String {
    val name = name ?: error("An emoji must have a name")
    // If there is no id, this means it is a Unicode emoji and not a custom emoji.
    val id = id ?: return name

    // Custom emojis format: <a:name:id>
    return buildString {
        append('<')
        if (animated.asNullable == true) {
            append('a')
        }
        append(':')
        append(name)
        append(':')
        append(id)
        append('>')
    }
}
