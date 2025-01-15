package io.github.hansanto.quipoquiz.discord.component.endgame

import dev.kord.x.emoji.Emojis
import io.github.hansanto.quipoquiz.config.EmojiConfiguration
import io.github.hansanto.quipoquiz.discord.framework.builder.message.CustomEmbedBuilder
import io.github.hansanto.quipoquiz.discord.framework.component.EmbedComponent
import io.github.hansanto.quipoquiz.discord.framework.extension.visualizer
import io.github.hansanto.quipoquiz.game.player.RankPlayer

class EndGameScoreComponent(
    override val id: String,
    private val ranks: List<RankPlayer>
) : EmbedComponent {

    init {
        require(ranks.isNotEmpty()) { "Players must not be empty" }
    }

    override suspend fun render(builder: CustomEmbedBuilder) {
        builder.field {
            name = "Score"

            val maxLengthRank = ranks.maxOf { it.rank.toString().length }
            value = buildString {
                ranks.forEach {
                    val player = it.player
                    val rank = it.rank
                    val emoji = it.emoji

                    val correctAnswers = player.correctAnswers
                    val incorrectAnswers = player.incorrectAnswers
                    val life = player.life
                    val playerName = player.mention

                    if (emoji != null) {
                        append(emoji.visualizer())
                    } else {
                        append("**").append(rank.toString().padStart(maxLengthRank, '0')).append("**.")
                    }

                    append(" [")
                    append(correctAnswers)
                    append(' ')
                    append(EmojiConfiguration.correctWithBackground.visualizer())
                    append(" / ")
                    append(incorrectAnswers)
                    append(' ')
                    append(EmojiConfiguration.incorrectWithBackground.visualizer())
                    append(" / ")
                    append(life ?: '∞')
                    append(' ')
                    append(Emojis.hearts)
                    append(']')
                    append(' ')
                    append(playerName)
                    append('\n')
                }
            }
        }
    }
}
