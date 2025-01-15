package io.github.hansanto.quipoquiz.quipoquiz

import io.github.hansanto.quipoquiz.util.randomString
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe

class QuipoQuizCategoryTest : ShouldSpec({

    should("toQuestionOverviews return an empty sequence when there are no categories") {
        val categories = emptyList<QuipoQuizCategory>()
        val questionOverviews = categories.toQuestionOverviews().toList()
        questionOverviews shouldBe emptyList()
    }

    should("toQuestionOverviews return an empty sequence when there are no quiz") {
        val categories = listOf(
            QuipoQuizCategory(
                id = QuipoQuizCategoryId(randomString()),
                name = randomString(),
                image = randomString(),
                icon = randomString(),
                color = randomString(),
                quizzes = emptyList()
            )
        )
        val questionOverviews = categories.toQuestionOverviews().toList()
        questionOverviews shouldBe emptyList()
    }

    should("toQuestionOverviews return an empty sequence when there are no questions") {
        val categoryId = QuipoQuizCategoryId(randomString())
        val categories = listOf(
            createCategory(categoryId)
        )
        val questionOverviews = categories.toQuestionOverviews().toList()
        questionOverviews shouldBe emptyList()
    }

    should("toQuestionOverviews return list of all questions overview") {
        val categoryId = QuipoQuizCategoryId(randomString())
        val categories = listOf(
            createCategory(
                categoryId,
                questionsTrueFalse = listOf(
                    { quizId ->
                        List(10) { createQuestionTrueFalse(quizId) }
                    },
                    { quizId ->
                        List(5) { createQuestionTrueFalse(quizId) }
                    }
                ),
                questionsMCQ = listOf(
                    { quizId ->
                        List(20) { createQuestionMCQ(quizId) }
                    },
                    { quizId ->
                        List(3) { createQuestionMCQ(quizId) }
                    }
                )
            ),
            createCategory(
                QuipoQuizCategoryId(randomString()),
                questionsTrueFalse = listOf(
                    { quizId ->
                        List(2) { createQuestionTrueFalse(quizId) }
                    }
                ),
                questionsMCQ = listOf(
                    { quizId ->
                        List(4) { createQuestionMCQ(quizId) }
                    }
                )
            )
        )

        val questionOverviews = categories.toQuestionOverviews().toList()
        questionOverviews.size shouldBe 44
    }
})

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

private fun createCategory(
    categoryId: QuipoQuizCategoryId,
    questionsTrueFalse: List<(QuipoQuizQuizId) -> List<QuipoQuizQuestionTrueFalse>> = emptyList(),
    questionsMCQ: List<(QuipoQuizQuizId) -> List<QuipoQuizQuestionMCQ>> = emptyList()
): QuipoQuizCategory {
    val quizzes = mutableListOf<QuipoQuizQuiz>()

    val numberOfQuizzes = maxOf(questionsTrueFalse.size, questionsMCQ.size)
    repeat(numberOfQuizzes) {
        val quizId = QuipoQuizQuizId(randomString())
        val questionsTrueFalseList = questionsTrueFalse.getOrNull(it)?.invoke(quizId) ?: emptyList()
        val questionsMCQList = questionsMCQ.getOrNull(it)?.invoke(quizId) ?: emptyList()
        quizzes.add(
            QuipoQuizQuiz(
                id = QuipoQuizQuizId(randomString()),
                categoryId = categoryId,
                title = randomString(),
                questionsTrueFalse = questionsTrueFalseList,
                questionsMCQ = questionsMCQList
            )
        )
    }

    return QuipoQuizCategory(
        id = categoryId,
        name = randomString(),
        image = randomString(),
        icon = randomString(),
        color = randomString(),
        quizzes = quizzes
    )
}
