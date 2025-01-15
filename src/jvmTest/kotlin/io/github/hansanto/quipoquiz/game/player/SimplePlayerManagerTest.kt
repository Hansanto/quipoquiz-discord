package io.github.hansanto.quipoquiz.game.player

import dev.kord.common.entity.Snowflake
import dev.kord.core.entity.User
import io.github.hansanto.quipoquiz.quipoquiz.QuipoQuizAnswerChoiceId
import io.github.hansanto.quipoquiz.quipoquiz.QuipoQuizQuestionId
import io.github.hansanto.quipoquiz.util.randomInt
import io.github.hansanto.quipoquiz.util.randomSnowflake
import io.github.hansanto.quipoquiz.util.randomString
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlin.collections.forEach

class SimplePlayerManagerTest : ShouldSpec({

    should("getOrAddPlayer should return the player if it exists") {
        val player = mockk<Player>()
        var generatorCounter = 0
        val playerManager = SimplePlayerManager {
            if (generatorCounter == 0) {
                generatorCounter++
                player
            } else {
                error("Should not be called")
            }
        }

        val user = mockk<User> {
            every { id } returns randomSnowflake()
        }
        playerManager.getOrAddPlayer(user) shouldBe player
        playerManager.getOrAddPlayer(user) shouldBe player
    }

    should("getOrAddPlayer should create a new player if it does not exist") {
        val players = createPlayerWithUser(10)

        val playerManager = SimplePlayerManager { user ->
            players[user] ?: error("Should not be called")
        }

        players.forEach { (user, player) ->
            playerManager.getOrAddPlayer(user) shouldBe player
        }
    }

    should("players should return an empty list if no player exists") {
        val playerManager = SimplePlayerManager { error("Should not be called") }
        playerManager.players() shouldBe emptyList()
    }

    should("players should return all the players") {
        val players = createPlayerWithUser(10)

        val playerManager = SimplePlayerManager { user ->
            players[user] ?: error("Should not be called")
        }

        players.forEach { (user, _) ->
            playerManager.getOrAddPlayer(user)
        }

        playerManager.players() shouldBe players.values.toList()
    }

    should("countVote should return 0 if no vote exists") {
        val playerManager = SimplePlayerManager { error("Should not be called") }
        val questionId = QuipoQuizQuestionId(randomString())
        val choiceId = QuipoQuizAnswerChoiceId(randomInt())

        playerManager.countVote(questionId, choiceId) shouldBe 0
    }

    should("countVote should return the number of votes for a choice") {
        val questionId = QuipoQuizQuestionId(randomString())
        val choiceId = QuipoQuizAnswerChoiceId(randomInt())

        val players = createPlayerWithUser(10) {
            coEvery { hasLife() } returns true
            coEvery { hasVote(questionId, choiceId) } returns true
        }

        val playerManager =
            SimplePlayerManager { snowflake -> players[snowflake] ?: error("The player $snowflake does not exist") }
        players.forEach { (user, _) ->
            playerManager.getOrAddPlayer(user)
        }

        playerManager.countVote(questionId, choiceId) shouldBe players.size
    }

    should("countVote count players that cannot play") {
        val questionId = QuipoQuizQuestionId(randomString())
        val choiceId = QuipoQuizAnswerChoiceId(randomInt())

        val players = createPlayerWithUser(2) {
            // Only the first player can play
            coEvery { hasLife() } returns (it == 0)
            coEvery { hasVote(questionId, choiceId) } returns true
        }

        val playerManager = SimplePlayerManager { user ->
            players[user] ?: error("Should not be called")
        }

        players.forEach { (user, _) ->
            playerManager.getOrAddPlayer(user)
        }

        playerManager.countVote(questionId, choiceId) shouldBe 2
    }

    should("countVoter should return 0 if no voter exists") {
        val playerManager = SimplePlayerManager { error("Should not be called") }
        val questionId = QuipoQuizQuestionId(randomString())
        playerManager.countVoter(questionId) shouldBe 0
    }

    should("countVoter should return the number of voters for a question") {
        val questionId = QuipoQuizQuestionId(randomString())

        val players = createPlayerWithUser(10) {
            coEvery { hasLife() } returns true
            coEvery { hasVote(questionId) } returns true
        }

        val playerManager = SimplePlayerManager { user ->
            players[user] ?: error("Should not be called")
        }

        players.forEach { (user, _) ->
            playerManager.getOrAddPlayer(user)
        }

        playerManager.countVoter(questionId) shouldBe players.size
    }

    should("countVoter count players that cannot play") {
        val questionId = QuipoQuizQuestionId(randomString())

        val players = createPlayerWithUser(2) {
            // Only the first player can play
            coEvery { hasLife() } returns (it == 0)
            coEvery { hasVote(questionId) } returns true
        }

        val playerManager = SimplePlayerManager {
            players[it] ?: error("Should not be called")
        }

        players.forEach { (user, _) ->
            playerManager.getOrAddPlayer(user)
        }

        playerManager.countVoter(questionId) shouldBe 2
    }

    should("sortedPlayersDescending return an empty list if no player exists") {
        val playerManager = SimplePlayerManager { error("Should not be called") }
        playerManager.sortedPlayersDescending() shouldBe emptyList()
    }

    should("sortedPlayersDescending return list sorted by correct answers descending only if no equality") {
        val (user1, player1) = createPlayer(1) {
            every { correctAnswers } returns 10
        }

        val (user2, player2) = createPlayer(2) {
            every { correctAnswers } returns 5
        }

        val (user3, player3) = createPlayer(3) {
            every { correctAnswers } returns 15
        }

        val players = buildMap {
            put(user1, player1)
            put(user2, player2)
            put(user3, player3)
        }

        val playerManager = SimplePlayerManager { user ->
            players[user] ?: error("Should not be called")
        }
        players.keys.shuffled().forEach { user ->
            playerManager.getOrAddPlayer(user)
        }

        playerManager.sortedPlayersDescending() shouldBe listOf(player3, player1, player2)
    }

    @Suppress("ktlint:standard:max-line-length")
    should(
        "sortedPlayersDescending return list sorted by correct answers descending then incorrect answers ascending if equality"
    ) {
        val (user1, player1) = createPlayer(1) {
            every { correctAnswers } returns 10
            every { incorrectAnswers } returns 5
        }

        val (user2, player2) = createPlayer(2) {
            every { correctAnswers } returns 10
            every { incorrectAnswers } returns 10
        }

        val (user3, player3) = createPlayer(3) {
            every { correctAnswers } returns 11
        }

        val players = buildMap {
            put(user1, player1)
            put(user2, player2)
            put(user3, player3)
        }

        val playerManager = SimplePlayerManager { user ->
            players[user] ?: error("Should not be called")
        }
        players.keys.shuffled().forEach { user ->
            playerManager.getOrAddPlayer(user)
        }

        playerManager.sortedPlayersDescending() shouldBe listOf(player3, player1, player2)
    }

    @Suppress("ktlint:standard:max-line-length")
    should(
        "sortedPlayersDescending return list sorted by correct answers descending then incorrect answers ascending then life descending if equality"
    ) {
        val (user1, player1) = createPlayer(1) {
            every { correctAnswers } returns 10
            every { incorrectAnswers } returns 5
            every { life } returns 100
        }

        val (user2, player2) = createPlayer(2) {
            every { correctAnswers } returns 10
            every { incorrectAnswers } returns 5
            every { life } returns 200
        }

        val (user3, player3) = createPlayer(3) {
            every { correctAnswers } returns 10
            every { incorrectAnswers } returns 5
            every { life } returns null
        }

        val (user4, player4) = createPlayer(4) {
            every { correctAnswers } returns 10
            every { incorrectAnswers } returns 10
        }

        val (user5, player5) = createPlayer(5) {
            every { correctAnswers } returns 11
        }

        val players = buildMap {
            put(user1, player1)
            put(user2, player2)
            put(user3, player3)
            put(user4, player4)
            put(user5, player5)
        }

        val playerManager = SimplePlayerManager { user ->
            players[user] ?: error("Should not be called")
        }

        players.keys.shuffled().forEach { user ->
            playerManager.getOrAddPlayer(user)
        }

        playerManager.sortedPlayersDescending() shouldBe listOf(player5, player3, player2, player1, player4)
    }

    @Suppress("ktlint:standard:max-line-length")
    should(
        "sortedPlayersDescending return list sorted by correct answers descending then incorrect answers ascending then life descending then user name ascending if equality"
    ) {
        val (user1, player1) = createPlayer(1) {
            every { correctAnswers } returns 10
            every { incorrectAnswers } returns 5
            every { life } returns 100
            every { name } returns "A"
        }

        val (user2, player2) = createPlayer(2) {
            every { correctAnswers } returns 10
            every { incorrectAnswers } returns 5
            every { life } returns 100
            every { name } returns "B"
        }

        val (user3, player3) = createPlayer(3) {
            every { correctAnswers } returns 10
            every { incorrectAnswers } returns 5
            every { life } returns 100
            every { name } returns "C"
        }

        val (user4, player4) = createPlayer(4) {
            every { correctAnswers } returns 11
        }

        val (user5, player5) = createPlayer(5) {
            every { correctAnswers } returns 10
            every { incorrectAnswers } returns 3
            every { life } returns null
        }

        val (user6, player6) = createPlayer(6) {
            every { correctAnswers } returns 10
            every { incorrectAnswers } returns 3
            every { life } returns 101
        }

        val players = buildMap {
            put(user1, player1)
            put(user2, player2)
            put(user3, player3)
            put(user4, player4)
            put(user5, player5)
            put(user6, player6)
        }

        val playerManager = SimplePlayerManager { user ->
            players[user] ?: error("Should not be called")
        }

        players.keys.shuffled().forEach { user ->
            playerManager.getOrAddPlayer(user)
        }

        playerManager.sortedPlayersDescending() shouldBe listOf(player4, player5, player6, player1, player2, player3)
    }

    should("rankPlayers return an empty list if no player exists") {
        val playerManager = SimplePlayerManager { error("Should not be called") }
        playerManager.rankPlayers() shouldBe emptyList()
    }

    should("rankPlayers return a list with different ranks if everyone has a different score") {
        val (user1, player1) = createPlayer(1) {
            every { correctAnswers } returns 10
            every { incorrectAnswers } returns 5
            every { life } returns 100
        }

        val (user2, player2) = createPlayer(2) {
            every { correctAnswers } returns 10
            every { incorrectAnswers } returns 6
            every { life } returns 100
        }

        val (user3, player3) = createPlayer(3) {
            every { correctAnswers } returns 10
            every { incorrectAnswers } returns 6
            every { life } returns 101
        }

        val (user4, player4) = createPlayer(4) {
            every { correctAnswers } returns 11
        }

        val (user5, player5) = createPlayer(5) {
            every { correctAnswers } returns 10
            every { incorrectAnswers } returns 3
            every { life } returns null
        }

        val (user6, player6) = createPlayer(6) {
            every { correctAnswers } returns 10
            every { incorrectAnswers } returns 3
            every { life } returns 101
        }

        val players = buildMap {
            put(user1, player1)
            put(user2, player2)
            put(user3, player3)
            put(user4, player4)
            put(user5, player5)
            put(user6, player6)
        }

        val playerManager = SimplePlayerManager { user ->
            players[user] ?: error("Should not be called")
        }

        players.keys.shuffled().forEach { user ->
            playerManager.getOrAddPlayer(user)
        }

        playerManager.rankPlayers() shouldBe listOf(
            RankPlayer(player4, 1),
            RankPlayer(player5, 2),
            RankPlayer(player6, 3),
            RankPlayer(player1, 4),
            RankPlayer(player3, 5),
            RankPlayer(player2, 6)
        )
    }

    should("rankPlayers return a list with the same rank if everyone has the same score") {
        val (user1, player1) = createPlayer(1) {
            every { correctAnswers } returns 10
            every { incorrectAnswers } returns 5
            every { life } returns 100
            every { name } returns "A"
        }

        val (user2, player2) = createPlayer(2) {
            every { correctAnswers } returns 10
            every { incorrectAnswers } returns 5
            every { life } returns 100
            every { name } returns "B"
        }

        val (user3, player3) = createPlayer(3) {
            every { correctAnswers } returns 10
            every { incorrectAnswers } returns 5
            every { life } returns 100
            every { name } returns "C"
        }

        val players = buildMap {
            put(user1, player1)
            put(user2, player2)
            put(user3, player3)
        }

        val playerManager = SimplePlayerManager { user ->
            players[user] ?: error("Should not be called")
        }

        players.keys.shuffled().forEach { user ->
            playerManager.getOrAddPlayer(user)
        }

        playerManager.rankPlayers() shouldBe listOf(
            RankPlayer(player1, 1),
            RankPlayer(player2, 1),
            RankPlayer(player3, 1)
        )
    }

    should("rankPlayers return a list with two players first and the rest second if two players have the same score") {
        val (user1, player1) = createPlayer(1) {
            every { correctAnswers } returns 10
            every { incorrectAnswers } returns 0
            every { life } returns 100
            every { name } returns "B"
        }

        val (user2, player2) = createPlayer(2) {
            every { correctAnswers } returns 10
            every { incorrectAnswers } returns 0
            every { life } returns 100
            every { name } returns "B"
        }

        val (user3, player3) = createPlayer(3) {
            every { correctAnswers } returns 5
            every { incorrectAnswers } returns 5
            every { life } returns 100
            every { name } returns "C"
        }

        val (user4, player4) = createPlayer(4) {
            every { correctAnswers } returns 5
            every { incorrectAnswers } returns 5
            every { life } returns 100
            every { name } returns "D"
        }

        val (user5, player5) = createPlayer(5) {
            every { correctAnswers } returns 3
            every { incorrectAnswers } returns 3
            every { life } returns null
        }

        val players = buildMap {
            put(user1, player1)
            put(user2, player2)
            put(user3, player3)
            put(user4, player4)
            put(user5, player5)
        }

        val playerManager = SimplePlayerManager { user ->
            players[user] ?: error("Should not be called")
        }

        players.keys.shuffled().forEach { user ->
            playerManager.getOrAddPlayer(user)
        }

        val rankPlayers = playerManager.rankPlayers()
        rankPlayers.size shouldBe 5

        rankPlayers.take(2) shouldContainExactlyInAnyOrder listOf(
            RankPlayer(player1, 1),
            RankPlayer(player2, 1)
        )

        rankPlayers.drop(2).take(2) shouldContainExactlyInAnyOrder listOf(
            RankPlayer(player3, 2),
            RankPlayer(player4, 2)
        )

        rankPlayers.drop(4) shouldContainExactlyInAnyOrder listOf(
            RankPlayer(player5, 3)
        )
    }
})

private inline fun createPlayerWithUser(size: Int, playerBehavior: Player.(Int) -> Unit = {}): Map<User, Player> =
    List(size) { id -> createPlayer(id, playerBehavior) }.toMap()

private inline fun createPlayer(id: Int, playerBehavior: Player.(Int) -> Unit = {}): Pair<User, Player> {
    val snowflake = Snowflake(id.toLong())

    val user = mockk<User>("$id") userMock@{
        every { this@userMock.id } returns snowflake
    }

    val player = mockk<Player>("$id") playerMock@{
        every { this@playerMock.id } returns snowflake
        playerBehavior(id)
    }
    return Pair(user, player)
}
