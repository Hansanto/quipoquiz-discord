package io.github.hansanto.quipoquiz.game

import dev.kord.core.Kord
import io.github.hansanto.quipoquiz.Language
import io.github.hansanto.quipoquiz.config.GameConfiguration
import io.github.hansanto.quipoquiz.discord.component.endgame.EndGameInfoComponent
import io.github.hansanto.quipoquiz.discord.component.endgame.EndGameScoreComponent
import io.github.hansanto.quipoquiz.discord.component.paginator.button.addNextButton
import io.github.hansanto.quipoquiz.discord.component.paginator.button.addPreviousButton
import io.github.hansanto.quipoquiz.discord.framework.component.container.Container
import io.github.hansanto.quipoquiz.discord.framework.component.paginator.OwnerUserCheckButtonInteraction
import io.github.hansanto.quipoquiz.discord.framework.component.paginator.PaginatorComponent
import io.github.hansanto.quipoquiz.discord.framework.component.paginator.addEmbed
import io.github.hansanto.quipoquiz.game.player.RankPlayer
import io.github.hansanto.quipoquiz.util.createId
import kotlinx.coroutines.CancellationException
import kotlin.time.Duration.Companion.minutes

/**
 * Determine how to display the end of the game.
 */
fun interface DisplayEndGame {

    /**
     * Display the end of the game.
     * @param rankPlayers List of players with their scores.
     * @param exception Exception that caused the end of the game.
     */
    suspend fun display(rankPlayers: List<RankPlayer>, exception: CancellationException?)
}

/**
 * Display the end of the game by injecting the text components directly into the container without wrapping them in another component.
 * @property container Container to display the end of the game.
 * @property language Language of the game.
 */
class SimpleDisplayEndGame(
    val container: Container,
    val language: Language
) : DisplayEndGame {

    override suspend fun display(rankPlayers: List<RankPlayer>, exception: CancellationException?) {
        container.addEmbedComponent(
            EndGameInfoComponent(
                id = createId(container.id, "end_game_info"),
                language = language,
                cause = exception
            )
        )
        if (rankPlayers.isNotEmpty()) {
            container.addEmbedComponent(
                EndGameScoreComponent(
                    id = createId(container.id, "end_game_rank"),
                    ranks = rankPlayers
                )
            )
        }
        container.update()
    }
}

/**
 * Display the end of the game by injecting the text components into a paginator.
 * @property kord Kord instance to manager events.
 * @property container Container to display the end of the game.
 */
class PageableDisplayEndGame(
    val kord: Kord,
    val container: Container,
    val language: Language
) : DisplayEndGame {

    override suspend fun display(rankPlayers: List<RankPlayer>, exception: CancellationException?) {
        val endgameContainer = container.create()
        endgameContainer.timeout(maxAliveTimeout = 5.minutes, maxIdleTimeout = 5.minutes)

        val paginatorId = createId(endgameContainer.id, "paginator")
        endgameContainer.addGroupComponent(
            PaginatorComponent(
                id = paginatorId,
                pages = buildPages(paginatorId, rankPlayers),
                messageBuilder = endgameContainer
            ).apply {
                addEmbed {
                    EndGameInfoComponent(
                        id = createId(paginatorId, "end_game_info"),
                        language = language,
                        cause = exception
                    )
                }

                val checkIsOwner = OwnerUserCheckButtonInteraction(messageBuilder)
                addPreviousButton(
                    kord = kord,
                    preProcess = checkIsOwner
                )
                addNextButton(
                    kord = kord,
                    preProcess = checkIsOwner
                )
            }
        )

        endgameContainer.update()
    }

    /**
     * Create a list of components to display the end of the game.
     * @param paginatorId Paginator ID.
     * @return List of end game components.
     */
    private fun buildPages(paginatorId: String, rankPlayers: List<RankPlayer>): List<EndGameScoreComponent> {
        val size = GameConfiguration.scorePlayerPerPage
        var index = 0
        return rankPlayers.windowed(size, size, true).map {
            EndGameScoreComponent(
                id = createId(paginatorId, "end_game_score_${index++}"),
                ranks = it
            )
        }
    }
}
