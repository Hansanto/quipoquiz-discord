package io.github.hansanto.quipoquiz.game.player

import dev.kord.common.entity.DiscordPartialEmoji
import dev.kord.common.entity.Snowflake
import dev.kord.core.Kord
import dev.kord.core.behavior.UserBehavior
import dev.kord.core.entity.User
import dev.kord.core.supplier.EntitySupplier
import io.github.hansanto.quipoquiz.discord.framework.extension.medalEmojiOrNull
import io.github.hansanto.quipoquiz.quipoquiz.QuipoQuizAnswerChoiceId
import io.github.hansanto.quipoquiz.quipoquiz.QuipoQuizQuestionId
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

data class RankPlayer(
    val player: Player,
    val rank: Int
) {
    val emoji: DiscordPartialEmoji? get() = DiscordPartialEmoji.medalEmojiOrNull(rank)
}

interface Player : UserBehavior {

    /**
     * The name of the player.
     */
    val name: String

    /**
     * The number of correct answers.
     */
    val correctAnswers: Int

    /**
     * The number of incorrect answers.
     */
    val incorrectAnswers: Int

    /**
     * The life of the player.
     * If the player has infinite life, set it to null.
     */
    val life: Int?

    /**
     * Determine if the player has life.
     * If the player has infinite life, return `true`.
     * @return `true` if the player has life, `false` otherwise.
     */
    suspend fun hasLife(): Boolean

    /**
     * Add one correct answer to the player.
     * @return The new number of correct answers of the player.
     */
    suspend fun addCorrectAnswer(): Int

    /**
     * Add one incorrect answer to the player.
     * @return The new number of incorrect answers of the player.
     */
    suspend fun addIncorrectAnswer(): Int

    /**
     * Add a vote to a choice of a question.
     * If the player has already voted for the choice, remove the vote.
     * If the player has already voted for another choice, then he will vote for the previous and the new choice simultaneously.
     * @param question The question to vote.
     * @param choice The choice to vote.
     */
    suspend fun addOrRemoveVote(question: QuipoQuizQuestionId, choice: QuipoQuizAnswerChoiceId)

    /**
     * Set a vote to a choice of a question.
     * If the player has already voted for the choice, remove the vote.
     * If the player has already voted for another choice, then he will vote for the new choice only.
     * @param question The question to vote.
     * @param choice The choice to vote.
     */
    suspend fun setOrRemoveVote(question: QuipoQuizQuestionId, choice: QuipoQuizAnswerChoiceId)

    /**
     * Determine if the player has voted for a choice of a question.
     * @param question The question to check.
     * @param choice The choice to check.
     * @return `true` if the player has voted for the choice, `false` otherwise.
     */
    suspend fun hasVote(question: QuipoQuizQuestionId, choice: QuipoQuizAnswerChoiceId): Boolean

    /**
     * Determine if the player has voted for a question.
     * @param question The question to check.
     * @return `true` if the player has voted for the question, `false` otherwise.
     */
    suspend fun hasVote(question: QuipoQuizQuestionId): Boolean

    /**
     * Get the votes of the player for a question.
     * @param question The question to get the votes.
     * @return The votes of the player for the question, empty if the player has not voted.
     */
    suspend fun getVotes(question: QuipoQuizQuestionId): Collection<QuipoQuizAnswerChoiceId>
}

/**
 * Implementation of [Player] for user from Discord.
 */
class DiscordPlayer(
    override val kord: Kord,
    override val id: Snowflake,
    override val name: String,
    /**
     * The initial life of the player.
     * If the player has infinite life, set it to null.
     * If defined, must be greater than 0.
     */
    val initialLife: Int? = null,
    override val supplier: EntitySupplier = kord.defaultSupplier
) : Player {

    constructor(
        user: User,
        initialLife: Int?,
        kord: Kord = user.kord,
        supplier: EntitySupplier = kord.defaultSupplier
    ) : this(
        kord = kord,
        id = user.id,
        name = user.username,
        initialLife = initialLife,
        supplier = supplier
    )

    override var correctAnswers: Int = 0

    override var incorrectAnswers: Int = 0

    override val life: Int?
        get() {
            val initialLife = initialLife ?: return null
            val life = initialLife - incorrectAnswers
            return life.coerceIn(minimumValue = 0, maximumValue = initialLife)
        }

    /**
     * The votes of the player.
     * Contains the question ID and the choices voted.
     */
    private val votes = mutableMapOf<QuipoQuizQuestionId, MutableCollection<QuipoQuizAnswerChoiceId>>()

    /**
     * Mutex to protect the access to the player.
     */
    private val mutex = Mutex()

    init {
        initialLife?.let { require(it > 0) { "Life must be greater than or equal to 0" } }
    }

    override suspend fun hasLife(): Boolean {
        mutex.withLock {
            val currentLife = life
            return currentLife == null || currentLife > 0
        }
    }

    override suspend fun addCorrectAnswer(): Int {
        return mutex.withLock { ++correctAnswers }
    }

    override suspend fun addIncorrectAnswer(): Int {
        return mutex.withLock { ++incorrectAnswers }
    }

    override suspend fun addOrRemoveVote(question: QuipoQuizQuestionId, choice: QuipoQuizAnswerChoiceId) {
        mutex.withLock {
            val currentVotes = votes[question]
            if (currentVotes == null) {
                // If no vote for this question, add it
                votes[question] = createChoiceCollection(choice)
                return
            } else {
                // If the choice is already voted, remove it
                // Otherwise, add the new choice
                if (!currentVotes.remove(choice)) {
                    currentVotes.add(choice)
                }
            }
        }
    }

    override suspend fun setOrRemoveVote(question: QuipoQuizQuestionId, choice: QuipoQuizAnswerChoiceId) {
        mutex.withLock {
            val currentVotes = votes[question]
            if (currentVotes == null) {
                // If no vote for this question, add it
                votes[question] = createChoiceCollection(choice)
                return
            }

            val isSameChoice = currentVotes.size == 1 && currentVotes.firstOrNull() == choice
            currentVotes.clear()

            // If the choice is the same, remove it
            // Otherwise, add the new choice
            if (!isSameChoice) {
                currentVotes.add(choice)
            }
        }
    }

    override suspend fun hasVote(question: QuipoQuizQuestionId, choice: QuipoQuizAnswerChoiceId): Boolean {
        return mutex.withLock { votes[question]?.contains(choice) == true }
    }

    override suspend fun hasVote(question: QuipoQuizQuestionId): Boolean {
        return mutex.withLock { votes[question]?.isNotEmpty() == true }
    }

    override suspend fun getVotes(question: QuipoQuizQuestionId): Collection<QuipoQuizAnswerChoiceId> {
        return mutex.withLock { votes[question]?.toList() ?: emptyList() }
    }

    /**
     * Create a collection with the first choice.
     * @param choice The first choice.
     * @return The collection with the first choice.
     */
    private fun createChoiceCollection(choice: QuipoQuizAnswerChoiceId): MutableCollection<QuipoQuizAnswerChoiceId> =
        mutableListOf(choice)
}
