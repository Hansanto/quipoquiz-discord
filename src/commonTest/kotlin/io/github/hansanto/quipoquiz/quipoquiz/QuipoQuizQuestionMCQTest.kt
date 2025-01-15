package io.github.hansanto.quipoquiz.quipoquiz

import io.github.hansanto.quipoquiz.util.randomBoolean
import io.github.hansanto.quipoquiz.util.randomString
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe

class QuipoQuizQuestionMCQTest : ShouldSpec({

    should("authorizeMultipleChoices return true") {
        val question = createQuestionMCQ()
        question.authorizeMultipleChoices shouldBe true
    }

    should("answers return empty list if no choice is available") {
        val question = createQuestionMCQ(choices = emptyList())
        question.answers() shouldBe emptyList()
    }

    should("answers return empty list if all choices are incorrect") {
        val question = createQuestionMCQ(
            choices = listOf(
                QuipoQuizAnswerChoiceMCQ(id = QuipoQuizAnswerChoiceId(1), choice = randomString(), goodAnswer = false),
                QuipoQuizAnswerChoiceMCQ(id = QuipoQuizAnswerChoiceId(2), choice = randomString(), goodAnswer = false),
                QuipoQuizAnswerChoiceMCQ(id = QuipoQuizAnswerChoiceId(3), choice = randomString(), goodAnswer = false),
                QuipoQuizAnswerChoiceMCQ(id = QuipoQuizAnswerChoiceId(4), choice = randomString(), goodAnswer = false)
            )
        )
        question.answers() shouldBe emptyList()
    }

    should("answers return list of good answers") {
        val goodIds = mutableListOf<QuipoQuizAnswerChoiceId>()
        val choices = List(10) {
            val goodAnswer = randomBoolean()
            val id = QuipoQuizAnswerChoiceId(it)
            if (goodAnswer) goodIds.add(id)
            QuipoQuizAnswerChoiceMCQ(id = id, choice = randomString(), goodAnswer = goodAnswer)
        }

        val question = createQuestionMCQ(
            choices = choices
        )
        question.answers() shouldBe goodIds
    }

    should("toOverview return overview") {
        val question = createQuestionMCQ()
        val quiz = createQuiz()
        val category = createCategory()
        val overview = question.toOverview(
            quiz = quiz,
            category = category
        )
        (overview is QuipoQuizQuestionMCQOverview) shouldBe true
    }
})

private fun createQuestionMCQ(
    choices: Collection<QuipoQuizAnswerChoiceMCQ> = listOf(
        QuipoQuizAnswerChoiceMCQ(id = QuipoQuizAnswerChoiceId(1), choice = randomString(), goodAnswer = false),
        QuipoQuizAnswerChoiceMCQ(id = QuipoQuizAnswerChoiceId(2), choice = randomString(), goodAnswer = false),
        QuipoQuizAnswerChoiceMCQ(id = QuipoQuizAnswerChoiceId(3), choice = randomString(), goodAnswer = false),
        QuipoQuizAnswerChoiceMCQ(id = QuipoQuizAnswerChoiceId(4), choice = randomString(), goodAnswer = true)
    )
): QuipoQuizQuestionMCQ = QuipoQuizQuestionMCQ(
    id = QuipoQuizQuestionId(randomString()),
    quizId = QuipoQuizQuizId(randomString()),
    image = randomString(),
    title = randomString(),
    explanation = randomString(),
    choices = choices
)

private fun createCategory(): QuipoQuizCategory {
    return QuipoQuizCategory(
        id = QuipoQuizCategoryId(randomString()),
        name = randomString(),
        image = randomString(),
        icon = randomString(),
        color = randomString(),
        quizzes = emptyList()
    )
}

private fun createQuiz(): QuipoQuizQuiz {
    return QuipoQuizQuiz(
        id = QuipoQuizQuizId(randomString()),
        categoryId = QuipoQuizCategoryId(randomString()),
        title = randomString(),
        questionsTrueFalse = emptyList(),
        questionsMCQ = emptyList()
    )
}
