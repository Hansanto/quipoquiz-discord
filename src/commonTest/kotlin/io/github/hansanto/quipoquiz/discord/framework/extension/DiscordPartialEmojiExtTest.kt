package io.github.hansanto.quipoquiz.discord.framework.extension

import dev.kord.common.entity.DiscordPartialEmoji
import dev.kord.common.entity.Snowflake
import dev.kord.common.entity.optional.OptionalBoolean
import dev.kord.common.entity.optional.optional
import dev.kord.core.entity.ReactionEmoji
import dev.kord.x.emoji.Emojis
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe

class DiscordPartialEmojiExtTest : ShouldSpec({

    should("medal emojis should be correct") {
        DiscordPartialEmoji.medals shouldBe listOf(
            Emojis.`1stPlaceMedal`.toDiscordPartialEmoji(),
            Emojis.`2ndPlaceMedal`.toDiscordPartialEmoji(),
            Emojis.`3rdPlaceMedal`.toDiscordPartialEmoji()
        )
    }

    should("medalEmojiOrNull returns the correct emoji for rank") {
        DiscordPartialEmoji.medalEmojiOrNull(1) shouldBe Emojis.`1stPlaceMedal`.toDiscordPartialEmoji()
        DiscordPartialEmoji.medalEmojiOrNull(2) shouldBe Emojis.`2ndPlaceMedal`.toDiscordPartialEmoji()
        DiscordPartialEmoji.medalEmojiOrNull(3) shouldBe Emojis.`3rdPlaceMedal`.toDiscordPartialEmoji()
    }

    should("medalEmojiOrNull returns null for negative rank") {
        (-100..0).forEach {
            DiscordPartialEmoji.medalEmojiOrNull(it) shouldBe null
        }
    }

    should("medalEmojiOrNull returns null for rank greater than 3") {
        (4..100).forEach {
            DiscordPartialEmoji.medalEmojiOrNull(it) shouldBe null
        }
    }

    should("numbers should be correct") {
        DiscordPartialEmoji.numbers shouldBe listOf(
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
    }

    should("numberEmojiOrNull returns the correct emoji for number") {
        DiscordPartialEmoji.numberEmojiOrNull(0) shouldBe Emojis.zero.toDiscordPartialEmoji()
        DiscordPartialEmoji.numberEmojiOrNull(1) shouldBe Emojis.one.toDiscordPartialEmoji()
        DiscordPartialEmoji.numberEmojiOrNull(2) shouldBe Emojis.two.toDiscordPartialEmoji()
        DiscordPartialEmoji.numberEmojiOrNull(3) shouldBe Emojis.three.toDiscordPartialEmoji()
        DiscordPartialEmoji.numberEmojiOrNull(4) shouldBe Emojis.four.toDiscordPartialEmoji()
        DiscordPartialEmoji.numberEmojiOrNull(5) shouldBe Emojis.five.toDiscordPartialEmoji()
        DiscordPartialEmoji.numberEmojiOrNull(6) shouldBe Emojis.six.toDiscordPartialEmoji()
        DiscordPartialEmoji.numberEmojiOrNull(7) shouldBe Emojis.seven.toDiscordPartialEmoji()
        DiscordPartialEmoji.numberEmojiOrNull(8) shouldBe Emojis.eight.toDiscordPartialEmoji()
        DiscordPartialEmoji.numberEmojiOrNull(9) shouldBe Emojis.nine.toDiscordPartialEmoji()
        DiscordPartialEmoji.numberEmojiOrNull(10) shouldBe Emojis.keycapTen.toDiscordPartialEmoji()
    }

    should("numberEmojiOrNull returns null for negative number") {
        (-100..-1).forEach {
            DiscordPartialEmoji.numberEmojiOrNull(it) shouldBe null
        }
    }

    should("numberEmojiOrNull returns null for number greater than 10") {
        (11..100).forEach {
            DiscordPartialEmoji.numberEmojiOrNull(it) shouldBe null
        }
    }

    should("reactionEmoji toDiscordPartialEmoji returns the correct DiscordPartialEmoji") {
        val reactionEmoji = ReactionEmoji.Custom(
            id = Snowflake(1234),
            name = "name",
            isAnimated = false
        )
        val emoji = reactionEmoji.toDiscordPartialEmoji()
        emoji.id shouldBe Snowflake(1234)
        emoji.name shouldBe "name"
        emoji.animated shouldBe false.optional()
    }

    should("discordEmoji toDiscordPartialEmoji returns the correct DiscordPartialEmoji") {
        val emoji = Emojis.a.toDiscordPartialEmoji()
        emoji.id shouldBe null
        emoji.name shouldBe "\uD83C\uDD70\uFE0F"
        emoji.animated shouldBe OptionalBoolean.Missing
    }

    should("visualizer throws exception when emoji has no name") {
        val emoji = DiscordPartialEmoji(name = null, id = Snowflake(1234), animated = false.optional())
        val exception = shouldThrow<IllegalStateException> {
            emoji.visualizer()
        }
        exception.message shouldBe "An emoji must have a name"
    }

    should("visualizer returns the correct string format for non-animated custom emoji") {
        val emoji = DiscordPartialEmoji(name = "name", id = Snowflake(1234), animated = false.optional())
        emoji.visualizer() shouldBe "<:name:1234>"
    }

    should("visualizer returns the correct string format for animated custom emoji") {
        val emoji = DiscordPartialEmoji(name = "name", id = Snowflake(1234), animated = true.optional())
        emoji.visualizer() shouldBe "<a:name:1234>"
    }

    should("visualizer returns the correct string format for Unicode emoji") {
        val emoji = Emojis.a.toDiscordPartialEmoji()
        emoji.visualizer() shouldBe "\uD83C\uDD70\uFE0F"
    }
})
