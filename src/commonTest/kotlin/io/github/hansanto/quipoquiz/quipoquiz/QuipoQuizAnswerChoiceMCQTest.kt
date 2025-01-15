package io.github.hansanto.quipoquiz.quipoquiz

import dev.kord.common.entity.DiscordPartialEmoji
import io.github.hansanto.quipoquiz.discord.framework.extension.numberEmojiOrNull
import io.github.hansanto.quipoquiz.util.randomBoolean
import io.github.hansanto.quipoquiz.util.randomString
import io.kotest.assertions.json.shouldEqualJson
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class QuipoQuizAnswerChoiceMCQTest : ShouldSpec({

    should("symbol match with the id") {
        fun check(id: Int) {
            val choiceMCQ = QuipoQuizAnswerChoiceMCQ(
                id = QuipoQuizAnswerChoiceId(id),
                choice = randomString(),
                goodAnswer = randomBoolean()
            )

            choiceMCQ.symbol shouldBe DiscordPartialEmoji.numberEmojiOrNull(id)!!
        }

        (0..10).forEach { check(it) }
    }

    should("throw exception if id is negative") {
        shouldThrow<IllegalArgumentException> {
            QuipoQuizAnswerChoiceMCQ(
                id = QuipoQuizAnswerChoiceId(-1),
                choice = randomString(),
                goodAnswer = randomBoolean()
            )
        }
    }

    should("throw exception if id is greater than 10") {
        shouldThrow<IllegalArgumentException> {
            QuipoQuizAnswerChoiceMCQ(
                id = QuipoQuizAnswerChoiceId(11),
                choice = randomString(),
                goodAnswer = randomBoolean()
            )
        }
    }

    should("style is blue") {
        val choiceMCQ = QuipoQuizAnswerChoiceMCQ(
            id = QuipoQuizAnswerChoiceId(1),
            choice = randomString(),
            goodAnswer = randomBoolean()
        )

        choiceMCQ.style shouldBe ChoiceStyle.BLUE
    }

    should("serialization only use id, choice and goodAnswer") {
        val id = QuipoQuizAnswerChoiceId(1)
        val choice = randomString()
        val goodAnswer = randomBoolean()

        val choiceMCQ = QuipoQuizAnswerChoiceMCQ(
            id = id,
            choice = choice,
            goodAnswer = goodAnswer
        )

        val serialized = Json.encodeToString(choiceMCQ)
        serialized shouldEqualJson """
            {
                "id": ${id.value},
                "choice": "$choice",
                "goodAnswer": $goodAnswer
            }
        """.trimIndent()
    }
})
