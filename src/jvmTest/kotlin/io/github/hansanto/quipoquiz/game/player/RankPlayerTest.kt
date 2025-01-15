package io.github.hansanto.quipoquiz.game.player

import dev.kord.common.entity.DiscordPartialEmoji
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import io.mockk.mockk

class RankPlayerTest : ShouldSpec({

    should("emoji with rank 1 returns 🥇") {
        val rankPlayer = RankPlayer(mockk(), 1)
        rankPlayer.emoji shouldBe DiscordPartialEmoji(
            id = null,
            name = "🥇"
        )
    }

    should("emoji with rank 2 returns 🥈") {
        val rankPlayer = RankPlayer(mockk(), 2)
        rankPlayer.emoji shouldBe DiscordPartialEmoji(
            id = null,
            name = "🥈"
        )
    }

    should("emoji with rank 3 returns 🥉") {
        val rankPlayer = RankPlayer(mockk(), 3)
        rankPlayer.emoji shouldBe DiscordPartialEmoji(
            id = null,
            name = "🥉"
        )
    }

    should("emoji with rank greater than 3 returns null") {
        var rank = 4
        repeat(1000) {
            val rankPlayer = RankPlayer(mockk(), rank++)
            rankPlayer.emoji shouldBe null
        }
    }

    should("emoji with rank less than 1 returns null") {
        var rank = 0
        repeat(1000) {
            val rankPlayer = RankPlayer(mockk(), rank--)
            rankPlayer.emoji shouldBe null
        }
    }
})
