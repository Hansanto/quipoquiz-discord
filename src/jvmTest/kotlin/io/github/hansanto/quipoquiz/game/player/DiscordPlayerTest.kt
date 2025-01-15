package io.github.hansanto.quipoquiz.game.player

import dev.kord.core.entity.User
import io.github.hansanto.quipoquiz.quipoquiz.QuipoQuizAnswerChoiceId
import io.github.hansanto.quipoquiz.quipoquiz.QuipoQuizQuestionId
import io.github.hansanto.quipoquiz.util.randomInt
import io.github.hansanto.quipoquiz.util.randomSnowflake
import io.github.hansanto.quipoquiz.util.randomString
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import kotlin.collections.forEach

class DiscordPlayerTest : ShouldSpec({

    should("throw error when creating a player with life less than 1") {
        (-10..0).forEach { life ->
            shouldThrow<IllegalArgumentException> {
                createPlayer(life)
            }
        }
    }

    should("create a player from Kord user") {
        val id = randomSnowflake()
        val name = randomString()
        val life = randomInt(min = 1)

        val user = mockk<User> {
            every { this@mockk.id } returns id
            every { this@mockk.username } returns name
            every { kord } returns mockk {
                every { defaultSupplier } returns mockk()
            }
        }

        val player = DiscordPlayer(
            user = user,
            initialLife = life
        )

        player.id shouldBe id
        player.name shouldBe name
        player.life shouldBe life
    }

    should("getLife return the life of the player") {
        val player = createPlayer(3)
        player.life shouldBe 3
    }

    should("getLife return null if the player has infinite life") {
        val player = createPlayer(null)
        player.life shouldBe null
    }

    should("getLife return 0 if the player has no life") {
        val player = createPlayer(1)
        player.addIncorrectAnswer()
        player.life shouldBe 0
    }

    should("getLife return 0 if the player has more incorrect answers than life") {
        val player = createPlayer(2)
        repeat(3) { player.addIncorrectAnswer() }
        player.life shouldBe 0
    }

    should("hasLife return true if the player has life") {
        val player = createPlayer(1)
        player.hasLife() shouldBe true
    }

    should("hasLife return true if the player has infinite life") {
        val player = createPlayer(null)
        player.hasLife() shouldBe true
    }

    should("hasLife return false if the player has no life") {
        val player = createPlayer(1)
        player.addIncorrectAnswer()
        player.hasLife() shouldBe false
    }

    should("addCorrectAnswer return the new number of correct answers of the player") {
        val player = createPlayer(1)
        player.addCorrectAnswer() shouldBe 1
        player.addCorrectAnswer() shouldBe 2
        player.addCorrectAnswer() shouldBe 3
        player.correctAnswers shouldBe 3
    }

    should("addIncorrectAnswer return the new number of incorrect answers of the player") {
        val player = createPlayer(1)
        player.addIncorrectAnswer() shouldBe 1
        player.addIncorrectAnswer() shouldBe 2
        player.addIncorrectAnswer() shouldBe 3
        player.incorrectAnswers shouldBe 3
    }

    should("addOrRemoveVote add a vote to a choice of a question") {
        val player = createPlayer(null)
        val questionId = QuipoQuizQuestionId(randomString())
        val choiceId = QuipoQuizAnswerChoiceId(randomInt())
        player.addOrRemoveVote(questionId, choiceId)
        player.hasVote(questionId, choiceId) shouldBe true
    }

    should("addOrRemoveVote add a vote to several choices of a question") {
        val player = createPlayer(null)
        val questionId = QuipoQuizQuestionId(randomString())
        val choiceId1 = QuipoQuizAnswerChoiceId(randomInt())
        val choiceId2 = QuipoQuizAnswerChoiceId(randomInt())

        player.addOrRemoveVote(questionId, choiceId1)
        player.addOrRemoveVote(questionId, choiceId2)

        player.hasVote(questionId, choiceId1) shouldBe true
        player.hasVote(questionId, choiceId2) shouldBe true
    }

    should("addOrRemoveVote remove a vote to a choice of a question") {
        val player = createPlayer(null)
        val questionId = QuipoQuizQuestionId(randomString())
        val choiceId = QuipoQuizAnswerChoiceId(randomInt())
        player.addOrRemoveVote(questionId, choiceId)
        player.addOrRemoveVote(questionId, choiceId)
        player.hasVote(questionId, choiceId) shouldBe false
    }

    should("setOrRemoveVote add a vote to a choice of a question") {
        val player = createPlayer(null)
        val questionId = QuipoQuizQuestionId(randomString())
        val choiceId = QuipoQuizAnswerChoiceId(randomInt())
        player.setOrRemoveVote(questionId, choiceId)
        player.hasVote(questionId, choiceId) shouldBe true
    }

    should("setOrRemoveVote remove a vote to a choice of a question") {
        val player = createPlayer(null)
        val questionId = QuipoQuizQuestionId(randomString())
        val choiceId = QuipoQuizAnswerChoiceId(randomInt())
        player.setOrRemoveVote(questionId, choiceId)
        player.setOrRemoveVote(questionId, choiceId)
        player.hasVote(questionId, choiceId) shouldBe false
    }

    should("setOrRemoveVote remove other votes by setting a already voted choice") {
        val player = createPlayer(null)
        val questionId = QuipoQuizQuestionId(randomString())

        val choiceId1 = QuipoQuizAnswerChoiceId(randomInt())
        val choiceId2 = QuipoQuizAnswerChoiceId(randomInt())
        player.addOrRemoveVote(questionId, choiceId1)
        player.addOrRemoveVote(questionId, choiceId2)

        player.setOrRemoveVote(questionId, choiceId1)

        player.hasVote(questionId, choiceId1) shouldBe true
        player.hasVote(questionId, choiceId2) shouldBe false
    }

    should("setOrRemoveVote remove other votes by setting a new choice") {
        val player = createPlayer(null)
        val questionId = QuipoQuizQuestionId(randomString())

        val choiceId1 = QuipoQuizAnswerChoiceId(randomInt())
        val choiceId2 = QuipoQuizAnswerChoiceId(randomInt())
        val choiceId3 = QuipoQuizAnswerChoiceId(randomInt())
        player.addOrRemoveVote(questionId, choiceId1)
        player.addOrRemoveVote(questionId, choiceId2)

        player.setOrRemoveVote(questionId, choiceId3)

        player.hasVote(questionId, choiceId1) shouldBe false
        player.hasVote(questionId, choiceId2) shouldBe false
        player.hasVote(questionId, choiceId3) shouldBe true
    }

    should("hasVote for a question and choice return true if the player has voted for the choice") {
        val player = createPlayer(null)
        val questionId = QuipoQuizQuestionId(randomString())
        val choiceId = QuipoQuizAnswerChoiceId(randomInt())
        player.addOrRemoveVote(questionId, choiceId)
        player.hasVote(questionId, choiceId) shouldBe true
    }

    should("hasVote for a question and choice return false if the player has not voted for the choice") {
        val player = createPlayer(null)
        val questionId = QuipoQuizQuestionId(randomString())
        val choiceId = QuipoQuizAnswerChoiceId(randomInt())
        player.hasVote(questionId, choiceId) shouldBe false
    }

    should("hasVote for a question return true if the player has voted for the question") {
        val player = createPlayer(null)
        val questionId = QuipoQuizQuestionId(randomString())
        val choiceId = QuipoQuizAnswerChoiceId(randomInt())
        player.addOrRemoveVote(questionId, choiceId)
        player.hasVote(questionId) shouldBe true
    }

    should("hasVote for a question return false if the player has not voted for the question") {
        val player = createPlayer(null)
        val questionId = QuipoQuizQuestionId(randomString())
        player.hasVote(questionId) shouldBe false
    }

    should("hasVote for a question return true if the player has voted for a choice of the question") {
        val player = createPlayer(null)
        val questionId = QuipoQuizQuestionId(randomString())
        val choiceId = QuipoQuizAnswerChoiceId(randomInt())
        player.addOrRemoveVote(questionId, choiceId)
        player.hasVote(questionId) shouldBe true
    }

    should("getVote return empty list if the player has not voted for the question") {
        val player = createPlayer(null)
        val questionId = QuipoQuizQuestionId(randomString())
        player.getVotes(questionId) shouldBe emptyList()
    }

    should("getVote return the votes of the player for the question") {
        val player = createPlayer(null)
        val questionId = QuipoQuizQuestionId(randomString())
        val choiceId1 = QuipoQuizAnswerChoiceId(randomInt())
        val choiceId2 = QuipoQuizAnswerChoiceId(randomInt())
        player.addOrRemoveVote(questionId, choiceId1)
        player.addOrRemoveVote(questionId, choiceId2)
        player.getVotes(questionId) shouldBe listOf(choiceId1, choiceId2)
    }

    should("getVote return the votes of the player for the question after removing a vote") {
        val player = createPlayer(null)
        val questionId = QuipoQuizQuestionId(randomString())
        val choiceId1 = QuipoQuizAnswerChoiceId(randomInt())
        val choiceId2 = QuipoQuizAnswerChoiceId(randomInt())

        player.addOrRemoveVote(questionId, choiceId1)
        player.addOrRemoveVote(questionId, choiceId2)

        player.addOrRemoveVote(questionId, choiceId1)
        player.getVotes(questionId) shouldBe listOf(choiceId2)
    }

    should("getVote return the votes of the player for the question after setting a vote") {
        val player = createPlayer(null)
        val questionId = QuipoQuizQuestionId(randomString())
        val choiceId1 = QuipoQuizAnswerChoiceId(randomInt())
        val choiceId2 = QuipoQuizAnswerChoiceId(randomInt())
        val choiceId3 = QuipoQuizAnswerChoiceId(randomInt())

        player.addOrRemoveVote(questionId, choiceId1)
        player.addOrRemoveVote(questionId, choiceId2)

        player.setOrRemoveVote(questionId, choiceId3)
        player.getVotes(questionId) shouldBe listOf(choiceId3)
    }
})

private fun createPlayer(life: Int? = null): DiscordPlayer {
    return DiscordPlayer(
        kord = mockk {
            every { defaultSupplier } returns mockk()
        },
        id = randomSnowflake(),
        name = randomString(),
        initialLife = life
    )
}
