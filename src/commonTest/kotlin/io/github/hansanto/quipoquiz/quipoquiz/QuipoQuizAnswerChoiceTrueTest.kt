package io.github.hansanto.quipoquiz.quipoquiz

import io.github.hansanto.quipoquiz.config.EmojiConfiguration
import io.github.hansanto.quipoquiz.util.randomBoolean
import io.kotest.assertions.json.shouldEqualJson
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class QuipoQuizAnswerChoiceTrueTest : ShouldSpec({

    should("id is 0") {
        val answerChoiceTrue = QuipoQuizAnswerChoiceTrue(randomBoolean())
        answerChoiceTrue.id shouldBe QuipoQuizAnswerChoiceId(0)
    }

    should("choice is true") {
        val answerChoiceTrue = QuipoQuizAnswerChoiceTrue(randomBoolean())
        answerChoiceTrue.choice shouldBe "true"
    }

    should("symbol is correct emoji defined from configuration") {
        val answerChoiceTrue = QuipoQuizAnswerChoiceTrue(randomBoolean())
        answerChoiceTrue.symbol shouldBe EmojiConfiguration.correct
    }

    should("style is green") {
        val answerChoiceTrue = QuipoQuizAnswerChoiceTrue(randomBoolean())
        answerChoiceTrue.style shouldBe ChoiceStyle.GREEN
    }

    should("serialization only use goodAnswer value") {
        val value = randomBoolean()
        val answerChoiceTrue = QuipoQuizAnswerChoiceTrue(value)
        val serialized = Json.encodeToString(answerChoiceTrue)
        serialized shouldEqualJson """
            {"goodAnswer": $value}
        """.trimIndent()
    }
})
