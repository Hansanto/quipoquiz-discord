package io.github.hansanto.quipoquiz.discord.component.question

import io.github.hansanto.quipoquiz.config.GameConfiguration
import io.github.hansanto.quipoquiz.discord.framework.builder.message.CustomEmbedBuilder
import io.github.hansanto.quipoquiz.extension.hexColorOrNull
import io.github.hansanto.quipoquiz.quipoquiz.QuipoQuizQuestionOverview

/**
 * Common display for any question type.
 */
abstract class AbstractQuestionComponent(
    override val id: String,
    /**
     * The question overview.
     */
    override val questionOverview: QuipoQuizQuestionOverview,
    /**
     * `true` if the answer is revealed and should be displayed.
     */
    override var revealed: Boolean = false
) : QuestionComponent {

    override suspend fun render(builder: CustomEmbedBuilder) {
        val question = questionOverview.question
        val category = questionOverview.category
        val quiz = questionOverview.quiz

        with(builder) {
            title = "${quiz.id}. ${quiz.title}"
            question.image?.let {
                thumbnail {
                    url = it
                }
            }

            (category.color.hexColorOrNull() ?: GameConfiguration.defaultColor).let {
                color = it
            }

            footer {
                addText(category.name)
            }
        }
    }
}
