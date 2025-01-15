package io.github.hansanto.quipoquiz.quipoquiz

import io.github.hansanto.quipoquiz.util.randomString
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe

class QuipoQuizQuizTest : ShouldSpec({

    should("allQuestions return an empty sequence when there are no questions") {
        val quiz = createQuiz()
        val questions = quiz.allQuestions().toList()
        questions shouldBe emptyList()
    }

    should("allQuestions only return sequence of true false questions if MCQ questions are empty") {
        val id = QuipoQuizQuizId(randomString())
        val expectedQuestions = List(10) { createQuestionTrueFalse(id) }

        val quiz = createQuiz(
            questionsTrueFalse = { quizId -> expectedQuestions }
        )

        val questions = quiz.allQuestions().toList()
        questions shouldBe expectedQuestions
    }

    should("allQuestions only return sequence of MCQ questions if true false questions are empty") {
        val id = QuipoQuizQuizId(randomString())
        val expectedQuestions = List(10) { createQuestionMCQ(id) }

        val quiz = createQuiz(
            questionsMCQ = { quizId -> expectedQuestions }
        )

        val questions = quiz.allQuestions().toList()
        questions shouldBe expectedQuestions
    }

    should("allQuestions return sequence of all questions") {
        val id = QuipoQuizQuizId(randomString())
        val expectedTrueFalseQuestions = List(10) { createQuestionTrueFalse(id) }
        val expectedMCQQuestions = List(10) { createQuestionMCQ(id) }
        val expectedQuestions = expectedTrueFalseQuestions + expectedMCQQuestions

        val quiz = createQuiz(
            questionsTrueFalse = { quizId -> expectedTrueFalseQuestions },
            questionsMCQ = { quizId -> expectedMCQQuestions }
        )

        val questions = quiz.allQuestions().toList()
        questions shouldBe expectedQuestions
    }
})

private inline fun createQuiz(
    id: QuipoQuizQuizId = QuipoQuizQuizId(randomString()),
    questionsTrueFalse: (QuipoQuizQuizId) -> List<QuipoQuizQuestionTrueFalse> = { emptyList() },
    questionsMCQ: (QuipoQuizQuizId) -> List<QuipoQuizQuestionMCQ> = { emptyList() }
): QuipoQuizQuiz {
    return QuipoQuizQuiz(
        id = id,
        categoryId = QuipoQuizCategoryId(randomString()),
        title = randomString(),
        questionsTrueFalse = questionsTrueFalse(id),
        questionsMCQ = questionsMCQ(id)
    )
}

private fun createQuestionTrueFalse(quizId: QuipoQuizQuizId): QuipoQuizQuestionTrueFalse = QuipoQuizQuestionTrueFalse(
    id = QuipoQuizQuestionId(randomString()),
    quizId = quizId,
    image = randomString(),
    title = randomString(),
    explanation = randomString(),
    trueChoice = QuipoQuizAnswerChoiceTrue(true),
    falseChoice = QuipoQuizAnswerChoiceFalse(false)
)

private fun createQuestionMCQ(quizId: QuipoQuizQuizId): QuipoQuizQuestionMCQ = QuipoQuizQuestionMCQ(
    id = QuipoQuizQuestionId(randomString()),
    quizId = quizId,
    image = randomString(),
    title = randomString(),
    explanation = randomString(),
    choices = listOf(
        QuipoQuizAnswerChoiceMCQ(id = QuipoQuizAnswerChoiceId(1), choice = randomString(), goodAnswer = false),
        QuipoQuizAnswerChoiceMCQ(id = QuipoQuizAnswerChoiceId(2), choice = randomString(), goodAnswer = false),
        QuipoQuizAnswerChoiceMCQ(id = QuipoQuizAnswerChoiceId(3), choice = randomString(), goodAnswer = false),
        QuipoQuizAnswerChoiceMCQ(id = QuipoQuizAnswerChoiceId(4), choice = randomString(), goodAnswer = true)
    )
)
