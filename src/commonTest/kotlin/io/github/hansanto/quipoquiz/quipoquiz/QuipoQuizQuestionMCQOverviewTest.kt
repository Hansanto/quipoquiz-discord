package io.github.hansanto.quipoquiz.quipoquiz

import io.github.hansanto.quipoquiz.Language
import io.github.hansanto.quipoquiz.discord.component.question.QuestionMCQComponent
import io.github.hansanto.quipoquiz.util.randomString
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe

class QuipoQuizQuestionMCQOverviewTest : ShouldSpec({

    should("toComponent create true false component with data from overview") {
        val question = QuipoQuizQuestionMCQ(
            id = QuipoQuizQuestionId(""),
            quizId = QuipoQuizQuizId(""),
            image = null,
            title = "",
            explanation = "",
            choices = emptyList()
        )

        val quiz = QuipoQuizQuiz(
            id = QuipoQuizQuizId(""),
            categoryId = QuipoQuizCategoryId(""),
            title = "Quiz",
            questionsTrueFalse = emptyList(),
            questionsMCQ = emptyList()
        )

        val category = QuipoQuizCategory(
            id = QuipoQuizCategoryId(""),
            name = "Category",
            image = null,
            icon = null,
            color = "",
            quizzes = emptyList()
        )

        val overview = QuipoQuizQuestionMCQOverview(
            question = question,
            quiz = quiz,
            category = category
        )

        val id = randomString()
        val language = Language.entries.random()
        val component = overview.toComponent(
            id = id,
            language = language
        )

        component as QuestionMCQComponent
        component.id shouldBe id
        component.questionOverview shouldBe overview
        component.revealed shouldBe false
        component.language shouldBe language
    }
})
