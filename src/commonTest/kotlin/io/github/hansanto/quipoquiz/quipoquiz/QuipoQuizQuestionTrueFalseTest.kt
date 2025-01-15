package io.github.hansanto.quipoquiz.quipoquiz

import io.github.hansanto.quipoquiz.util.randomString
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe

class QuipoQuizQuestionTrueFalseTest : ShouldSpec({

    should("authorizeMultipleChoices return false") {
        val question = createQuestionTrueFalse()
        question.authorizeMultipleChoices shouldBe false
    }

    should("choices return list of choices") {
        val question = createQuestionTrueFalse()
        question.choices shouldBe listOf(
            question.trueChoice,
            question.falseChoice
        )
    }

    should("answer return true if true choice is correct") {
        val question = createQuestionTrueFalse(
            trueChoice = QuipoQuizAnswerChoiceTrue(true),
            falseChoice = QuipoQuizAnswerChoiceFalse(false)
        )
        question.answer shouldBe true
    }

    should("answer return false if false choice is correct") {
        val question = createQuestionTrueFalse(
            trueChoice = QuipoQuizAnswerChoiceTrue(false),
            falseChoice = QuipoQuizAnswerChoiceFalse(true)
        )
        question.answer shouldBe false
    }

    should("answer return true if both choices are correct") {
        val question = createQuestionTrueFalse(
            trueChoice = QuipoQuizAnswerChoiceTrue(true),
            falseChoice = QuipoQuizAnswerChoiceFalse(true)
        )
        question.answer shouldBe true
    }

    should("answer return false if both choices are incorrect") {
        val question = createQuestionTrueFalse(
            trueChoice = QuipoQuizAnswerChoiceTrue(false),
            falseChoice = QuipoQuizAnswerChoiceFalse(false)
        )
        question.answer shouldBe false
    }

    should("answers return empty list if both choices are incorrect") {
        val question = createQuestionTrueFalse(
            trueChoice = QuipoQuizAnswerChoiceTrue(false),
            falseChoice = QuipoQuizAnswerChoiceFalse(false)
        )
        question.answers() shouldBe emptyList()
    }

    should("answers return list of true if if true choice is correct") {
        val question = createQuestionTrueFalse(
            trueChoice = QuipoQuizAnswerChoiceTrue(true),
            falseChoice = QuipoQuizAnswerChoiceFalse(false)
        )
        question.answers() shouldBe listOf(QuipoQuizAnswerChoiceId(0))
    }

    should("answers return list of false if if false choice is correct") {
        val question = createQuestionTrueFalse(
            trueChoice = QuipoQuizAnswerChoiceTrue(false),
            falseChoice = QuipoQuizAnswerChoiceFalse(true)
        )
        question.answers() shouldBe listOf(QuipoQuizAnswerChoiceId(1))
    }

    should("answers return list of true and false if both choices are correct") {
        val question = createQuestionTrueFalse(
            trueChoice = QuipoQuizAnswerChoiceTrue(true),
            falseChoice = QuipoQuizAnswerChoiceFalse(true)
        )
        question.answers() shouldContainExactlyInAnyOrder listOf(QuipoQuizAnswerChoiceId(0), QuipoQuizAnswerChoiceId(1))
    }

    should("toOverview return overview") {
        val question = createQuestionTrueFalse()
        val quiz = createQuiz()
        val category = createCategory()
        val overview = question.toOverview(
            quiz = quiz,
            category = category
        )
        (overview is QuipoQuizQuestionTrueFalseOverview) shouldBe true
    }
})

private fun createQuestionTrueFalse(
    trueChoice: QuipoQuizAnswerChoiceTrue = QuipoQuizAnswerChoiceTrue(true),
    falseChoice: QuipoQuizAnswerChoiceFalse = QuipoQuizAnswerChoiceFalse(false)
): QuipoQuizQuestionTrueFalse = QuipoQuizQuestionTrueFalse(
    id = QuipoQuizQuestionId(randomString()),
    quizId = QuipoQuizQuizId(randomString()),
    image = randomString(),
    title = randomString(),
    explanation = randomString(),
    trueChoice = trueChoice,
    falseChoice = falseChoice
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
