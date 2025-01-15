package io.github.hansanto.quipoquiz.game.player

import dev.kord.common.entity.Snowflake
import dev.kord.core.entity.User
import io.github.hansanto.quipoquiz.quipoquiz.QuipoQuizAnswerChoiceId
import io.github.hansanto.quipoquiz.quipoquiz.QuipoQuizQuestionId
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.collections.mapIndexed
import kotlin.collections.sortedWith

/**
 * Manager the instances of [Player].
 */
interface PlayerManager {

    /**
     * Get the player by its unique identifier.
     * @param user User to get the player.
     * @return The player if it exists, `null` otherwise.
     */
    suspend fun getPlayer(user: User): Player?

    /**
     * Get the player by its unique identifier.
     */
    suspend fun getOrAddPlayer(user: User): Player

    /**
     * Get all the players.
     * @return The list of all the players.
     */
    suspend fun players(): List<Player>

    /**
     * Count the number of votes for a choice of a question.
     * @param question ID of the question.
     * @param choice ID of the choice.
     * @return The number of votes for the choice.
     */
    suspend fun countVote(question: QuipoQuizQuestionId, choice: QuipoQuizAnswerChoiceId): Int

    /**
     * Count the number of voters for a question.
     * @param question ID of the question.
     * @return The number of voters for the question.
     */
    suspend fun countVoter(question: QuipoQuizQuestionId): Int

    /**
     * Get the players sorted by the best player to the worst player in game.
     * @return The list of players.
     */
    suspend fun sortedPlayersDescending(): List<Player>

    /**
     * Get the rank of the players sorted by the best player to the worst player in game (descending).
     * @return The list of players with their rank.
     */
    suspend fun rankPlayers(): List<RankPlayer>
}

/**
 * Implementation of [PlayerManager].
 */
class SimplePlayerManager(
//    val kord: Kord,
    /**
     * Factory to create a new instance of [Player].
     */
    private val playerFactory: suspend (User) -> Player
) : PlayerManager {

    companion object {
        /**
         * Comparator to sort the players by their score.
         * The best player is the player with the most correct answers, then the least incorrect answers, then the most life.
         * The worst player is the player with the least correct answers, then the most incorrect answers, then the least life.
         */
        private val comparatorScore = compareBy<Player>(
            { -it.correctAnswers },
            { it.incorrectAnswers },
            { -(it.life ?: Int.MAX_VALUE) }
        )
    }

    /**
     * Unique identifier of the player to the player.
     * Allowing to perform a quick search.
     */
    private val players: MutableMap<Snowflake, Player> = mutableMapOf<Snowflake, Player>()
//        .apply {
//            repeat(22) {
//                val life = (100..200).random()
//                put(Snowflake(it.toULong()), DiscordPlayer(kord, Snowflake((Int.MIN_VALUE..Int.MAX_VALUE).random().toULong()), "Player $it", life).apply {
//                    val demi = life / 2
//                    correctAnswers = (0..demi).random()
//                    incorrectAnswers = (0..demi).random()
//                })
//            }
//        }

    /**
     * Mutex to protect the access to the players.
     */
    private val mutex = Mutex()

    override suspend fun getPlayer(user: User): Player? {
        return mutex.withLock {
            players[user.id]
        }
    }

    override suspend fun getOrAddPlayer(user: User): Player {
        return mutex.withLock {
            players.getOrPut(user.id) { playerFactory(user) }
        }
    }

    override suspend fun players(): List<Player> {
        return mutex.withLock {
            players.values.toList()
        }
    }

    override suspend fun countVote(question: QuipoQuizQuestionId, choice: QuipoQuizAnswerChoiceId): Int {
        return mutex.withLock {
            players.values.count { it.hasVote(question, choice) }
        }
    }

    override suspend fun countVoter(question: QuipoQuizQuestionId): Int {
        return mutex.withLock {
            players.values.count { it.hasVote(question) }
        }
    }

    override suspend fun sortedPlayersDescending(): List<Player> {
        return mutex.withLock {
            players.values.sortedWith(comparatorScore.thenBy { it.name })
        }
    }

    override suspend fun rankPlayers(): List<RankPlayer> {
        val sortedPlayers = sortedPlayersDescending()
        if (sortedPlayers.isEmpty()) {
            return emptyList()
        }

        var rank = 1
        var previousPlayer: Player? = null
        return sortedPlayers.mapIndexed { index, player ->
            when {
                previousPlayer == null -> RankPlayer(player, rank)

                // Are similar, keep the same rank
                comparatorScore.compare(previousPlayer, player) == 0 -> RankPlayer(player, rank)

                // Are different, increment the rank
                else -> RankPlayer(player, ++rank)
            }.also { previousPlayer = player }
        }
    }
}
