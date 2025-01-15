package io.github.hansanto.quipoquiz.game

import dev.kord.common.entity.Snowflake
import dev.kord.core.Kord
import dev.kord.core.behavior.interaction.response.DeferredMessageInteractionResponseBehavior
import dev.kord.core.entity.User
import dev.kord.core.entity.interaction.Interaction
import io.github.hansanto.quipoquiz.Language
import io.github.hansanto.quipoquiz.config.GameConfiguration
import io.github.hansanto.quipoquiz.discord.component.paginator.addTimer
import io.github.hansanto.quipoquiz.discord.component.paginator.button.addChoiceButtons
import io.github.hansanto.quipoquiz.discord.component.paginator.button.addConfirmButton
import io.github.hansanto.quipoquiz.discord.component.paginator.button.addExitButton
import io.github.hansanto.quipoquiz.discord.component.paginator.button.addNextButton
import io.github.hansanto.quipoquiz.discord.component.paginator.button.addPreviousButton
import io.github.hansanto.quipoquiz.discord.component.paginator.button.addVoterCounterButton
import io.github.hansanto.quipoquiz.discord.component.question.QuestionComponent
import io.github.hansanto.quipoquiz.discord.framework.component.container.Container
import io.github.hansanto.quipoquiz.discord.framework.component.container.ContainerImpl
import io.github.hansanto.quipoquiz.discord.framework.component.container.builder.RowMessageBuilder
import io.github.hansanto.quipoquiz.discord.framework.component.container.exception.MessageDeletedException
import io.github.hansanto.quipoquiz.discord.framework.component.container.exception.handler.ContainerShutdownCreationResponseErrorHandler
import io.github.hansanto.quipoquiz.discord.framework.component.container.exception.handler.DisplayReasonCreationResponseErrorHandler
import io.github.hansanto.quipoquiz.discord.framework.component.container.exception.handler.chainWith
import io.github.hansanto.quipoquiz.discord.framework.component.paginator.AuthorizedUserCheckButtonInteraction
import io.github.hansanto.quipoquiz.discord.framework.component.paginator.OwnerUserCheckButtonInteraction
import io.github.hansanto.quipoquiz.discord.framework.component.paginator.PageContext
import io.github.hansanto.quipoquiz.discord.framework.component.paginator.PaginatorComponent
import io.github.hansanto.quipoquiz.discord.framework.component.paginator.PlayerHasLifeCheckButtonInteraction
import io.github.hansanto.quipoquiz.discord.framework.component.paginator.chainWith
import io.github.hansanto.quipoquiz.discord.framework.exception.DiscordException
import io.github.hansanto.quipoquiz.discord.framework.util.Cancellable
import io.github.hansanto.quipoquiz.extension.containsExactly
import io.github.hansanto.quipoquiz.game.player.DiscordPlayer
import io.github.hansanto.quipoquiz.game.player.Player
import io.github.hansanto.quipoquiz.game.player.PlayerManager
import io.github.hansanto.quipoquiz.game.player.SimplePlayerManager
import io.github.hansanto.quipoquiz.quipoquiz.QuipoQuizAnswerChoiceId
import io.github.hansanto.quipoquiz.quipoquiz.QuipoQuizQuestion
import io.github.hansanto.quipoquiz.quipoquiz.QuipoQuizQuestionId
import io.github.hansanto.quipoquiz.quipoquiz.QuipoQuizQuestionOverview
import io.github.hansanto.quipoquiz.util.createId
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.time.Duration

/**
 * Define a game to play.
 */
interface Game : Cancellable {

    /**
     * Start the game.
     * @param response Response to the interaction.
     */
    suspend fun start(response: DeferredMessageInteractionResponseBehavior)
}

/**
 * Implementation of [Game].
 */
class SimpleGame(
    /**
     * Kord instance to manager events.
     */
    val kord: Kord,
    /**
     * Container to display the game.
     */
    val container: Container,
    /**
     * Language to display the game.
     */
    val language: Language,
    /**
     * Hide the vote of the players.
     */
    val hideVote: Boolean,
    /**
     * Timer to vote, when the timer is over, the vote is closed.
     */
    val timerVote: Duration?,
    /**
     * List of questions to play.
     */
    val questions: List<QuipoQuizQuestionOverview>,
    /**
     * Manager of the players.
     */
    val playerManager: PlayerManager
) : Game, Cancellable by container {

    companion object {

        /**
         * Create a game from an interaction.
         * @param interaction Interaction to create the game.
         * @param questions List of questions to play.
         * @param language Language to display the game.
         * @param numberOfLife Number of life for each player.
         * @return Game instance.
         */
        suspend fun fromInteraction(
            interaction: Interaction,
            questions: List<QuipoQuizQuestionOverview>,
            language: Language,
            hideVote: Boolean,
            numberOfLife: Int?,
            timerVote: Duration?,
            owners: Collection<Snowflake>,
            authorizedUsers: Collection<Snowflake>?,
        ): SimpleGame {
            val kord = interaction.kord

            val container = ContainerImpl(
                kord = kord,
                owners = owners,
                authorizedUsers = authorizedUsers
            ).apply {
                errorHandler = DisplayReasonCreationResponseErrorHandler(language)
                    .chainWith(
                        ContainerShutdownCreationResponseErrorHandler(
                            kord = kord,
                            container = this,
                            language = language
                        )
                    )

                timeout(
                    maxAliveTimeout = GameConfiguration.timeoutAlivePerQuestion * questions.size,
                    maxIdleTimeout = GameConfiguration.timeoutIdle
                )
            }

            return SimpleGame(
                kord = kord,
                container = container,
                language = language,
                timerVote = timerVote,
                hideVote = hideVote,
                questions = questions,
                playerManager = SimplePlayerManager { user ->
                    DiscordPlayer(
                        user = user,
                        initialLife = numberOfLife
                    )
                }
            )
        }
    }

    override suspend fun start(response: DeferredMessageInteractionResponseBehavior) {
        container.onCancel {
            container.removeAndCancelComponents()
            when (it) {
                // Discord exception should be managed by the container.
                is MessageDeletedException, is DiscordException -> return@onCancel
                else -> displayEndgame(it)
            }
        }

        container.startAutoUpdate()
        container.addGroupComponent(createPaginator())
        container.render(response)
    }

    /**
     * Create paginator component.
     * @return Paginator component.
     */
    private suspend fun createPaginator(): PaginatorComponent<QuestionComponent> {
        val timerVote = timerVote
        val paginatorId = createId(container.id, "paginator")

        return PaginatorComponent(
            id = paginatorId,
            pages = buildPages(paginatorId),
            messageBuilder = container
        ).apply {
            val checkIsOwner = OwnerUserCheckButtonInteraction(messageBuilder)
            val checkIsAuthorized = AuthorizedUserCheckButtonInteraction(messageBuilder)
            val checkHasLife = PlayerHasLifeCheckButtonInteraction(playerManager)

            // Two components can trigger the reveal of the page, ConfirmButton and Timer.
            // To avoid a double reveal, we need to use this mutex.
            val revealMutex = Mutex()

            addChoiceButtons(
                kord = kord,
                preProcess = checkIsAuthorized.chainWith(checkHasLife),
                getNumberOfVotes = { choice ->
                    if (!hideVote || page.revealed) {
                        playerManager.countVote(page.questionOverview.question.id, choice)
                    } else {
                        null
                    }
                }
            ) { user, choiceId ->
                modifyVote(
                    user,
                    page.questionOverview.question,
                    choiceId
                )
            }

            addVoterCounterButton(
                row = RowMessageBuilder.FOURTH_ROW,
                getNumberOfVoters = { playerManager.countVoter(page.questionOverview.question.id) }
            )

            addPreviousButton(
                kord = kord,
                preProcess = checkIsOwner,
                row = RowMessageBuilder.FOURTH_ROW,
                disabled = { timerVote != null && !page.revealed }
            )

            addNextButton(
                kord = kord,
                preProcess = checkIsOwner,
                row = RowMessageBuilder.FOURTH_ROW
            ) { page.revealed }

            addConfirmButton(
                kord = kord,
                preProcess = checkIsOwner,
                row = RowMessageBuilder.FOURTH_ROW,
                addIf = { !page.revealed }
            ) { event ->
                revealPage(revealMutex) {
                    messageBuilder.render(event)
                }
            }

            addExitButton(
                kord = kord,
                preProcess = checkIsOwner,
                row = RowMessageBuilder.FOURTH_ROW
            )

            if (timerVote != null) {
                // Timer is automatically canceled when the container is updated.
                // If the timer is finished or the confirm button is clicked, the page is revealed and container updated.
                addTimer(
                    timer = timerVote,
                    intervalUpdate = GameConfiguration.timerVoteUpdateEvery,
                    language = language
                ) {
                    revealPage(revealMutex) {
                        messageBuilder.update()
                    }
                }
            }
        }
    }

    /**
     * If the page is already revealed, do nothing.
     * Otherwise, reveal it and apply votes to players.
     * @param mutex Mutex to avoid multiple reveals at the same time.
     * @param action Action to execute when the page is revealed.
     */
    private suspend inline fun PageContext<QuestionComponent>.revealPage(mutex: Mutex, action: () -> Unit) {
        mutex.withLock {
            val page = page
            if (page.revealed) return

            page.revealed = true
            applyVote(page.questionOverview.question)
            action()
        }
    }

    /**
     * Create a list of components to display each question.
     * @param paginatorId Paginator ID.
     * @return List of question components.
     */
    private fun buildPages(paginatorId: String): List<QuestionComponent> {
        return questions.mapIndexed { index, questionOverview ->
            questionOverview.toComponent(createId(paginatorId, "page_$index"), language)
        }.toList()
    }

    /**
     * Display the information of the end of the game.
     * @param exception Exception that caused the end of the game.
     */
    private suspend fun displayEndgame(exception: CancellationException?) {
        val rankPlayers = playerManager.rankPlayers()
        when (rankPlayers.size <= GameConfiguration.scorePlayerPerPage) {
            true -> SimpleDisplayEndGame(container, language)
            false -> PageableDisplayEndGame(kord, container, language)
        }.display(rankPlayers, exception)
    }

    /**
     * Modify the vote of a player.
     * If the player can't play, do nothing.
     * @param user Player ID.
     * @param question Question to vote.
     * @param choiceId Choice ID.
     */
    private suspend fun modifyVote(user: User, question: QuipoQuizQuestion, choiceId: QuipoQuizAnswerChoiceId) {
        val player = playerManager.getOrAddPlayer(user)
        if (question.authorizeMultipleChoices) {
            player.addOrRemoveVote(question.id, choiceId)
        } else {
            player.setOrRemoveVote(question.id, choiceId)
        }
    }

    /**
     * Close the vote and update the score of the players.
     * If the player has all correct answers, add one score.
     * If the player has at least one wrong answer, remove one life.
     */
    private suspend fun applyVote(question: QuipoQuizQuestion) {
        val questionId = question.id
        val goodChoiceIds = question.answers()

        playerManager.players()
            .asFlow()
            .filter(Player::hasLife)
            .collect { player ->
                val hasCorrectVote = this@SimpleGame.hasCorrectVote(player, questionId, goodChoiceIds)
                if (hasCorrectVote) {
                    player.addCorrectAnswer()
                } else {
                    player.addIncorrectAnswer()
                }
            }
    }

    /**
     * Check if the player has all correct answers.
     * If the player has all correct answers, return `true`
     * If the player has at least one wrong answer, return `false`
     * If the player has at least one missing answer, return `false`
     * If the player didn't vote, return `false`
     * @param player Player to check.
     * @param questionId Question ID.
     * @param goodChoiceIds List of correct answers.
     * @return `true` if the player has all correct answers, `false` otherwise.
     */
    private suspend fun hasCorrectVote(
        player: Player,
        questionId: QuipoQuizQuestionId,
        goodChoiceIds: Collection<QuipoQuizAnswerChoiceId>
    ): Boolean {
        val playerChoices = player.getVotes(questionId)
        return playerChoices.containsExactly(goodChoiceIds)
    }
}
