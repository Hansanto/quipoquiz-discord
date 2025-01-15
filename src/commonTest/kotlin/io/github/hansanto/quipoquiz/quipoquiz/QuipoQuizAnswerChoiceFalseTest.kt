package io.github.hansanto.quipoquiz.quipoquiz

import io.github.hansanto.quipoquiz.config.EmojiConfiguration
import io.github.hansanto.quipoquiz.util.randomBoolean
import io.kotest.assertions.json.shouldEqualJson
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class QuipoQuizAnswerChoiceFalseTest : ShouldSpec({

    should("id is 0") {
        val answerChoiceFalse = QuipoQuizAnswerChoiceFalse(randomBoolean())
        answerChoiceFalse.id shouldBe QuipoQuizAnswerChoiceId(1)
    }

    should("choice is true") {
        val answerChoiceFalse = QuipoQuizAnswerChoiceFalse(randomBoolean())
        answerChoiceFalse.choice shouldBe "false"
    }

    should("symbol is correct emoji defined from configuration") {
        val answerChoiceFalse = QuipoQuizAnswerChoiceFalse(randomBoolean())
        answerChoiceFalse.symbol shouldBe EmojiConfiguration.incorrect
    }

    should("style is green") {
        val answerChoiceFalse = QuipoQuizAnswerChoiceFalse(randomBoolean())
        answerChoiceFalse.style shouldBe ChoiceStyle.RED
    }

    should("serialization only use goodAnswer value") {
        val value = randomBoolean()
        val answerChoiceFalse = QuipoQuizAnswerChoiceFalse(value)
        val serialized = Json.encodeToString(answerChoiceFalse)
        serialized shouldEqualJson """
            {"goodAnswer": $value}
        """.trimIndent()
    }
})
