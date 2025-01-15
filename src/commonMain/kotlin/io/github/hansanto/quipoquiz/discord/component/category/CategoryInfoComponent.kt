package io.github.hansanto.quipoquiz.discord.component.category

import io.github.hansanto.generated.i18n.Messages
import io.github.hansanto.quipoquiz.Language
import io.github.hansanto.quipoquiz.config.EmojiConfiguration
import io.github.hansanto.quipoquiz.discord.framework.builder.message.CustomEmbedBuilder
import io.github.hansanto.quipoquiz.discord.framework.component.EmbedComponent
import io.github.hansanto.quipoquiz.discord.framework.extension.visualizer
import io.github.hansanto.quipoquiz.extension.hexColorOrNull
import io.github.hansanto.quipoquiz.quipoquiz.QuipoQuizCategory
import io.github.hansanto.quipoquiz.quipoquiz.QuipoQuizQuiz

/**
 * Component to display the category information in an embed.
 */
class CategoryInfoComponent(
    override val id: String,
    /**
     * Category with quizzes.
     */
    private val category: QuipoQuizCategory,
    /**
     * Partition of quizzes for the category to display.
     */
    private val quizzesSubList: List<QuipoQuizQuiz>,
    /**
     * Language selected.
     */
    private val language: Language
) : EmbedComponent {

    override suspend fun render(builder: CustomEmbedBuilder) {
        val i18nLocale = language.i18nLocale

        with(builder) {
            val quizzes = category.quizzes
            val quizzesCount = quizzes.size.toString()
            val questionsTrueFalseCount = quizzes.asSequence().map { it.questionsTrueFalse.size }.sum().toString()
            val questionsMCQCount = quizzes.asSequence().map { it.questionsMCQ.size }.sum().toString()

            this.title = "Category - ${category.name}"
            category.image?.let { image = it }
            category.color.hexColorOrNull()?.let { color = it }
            this.description = "$quizzesCount ${Messages.quizzes(i18nLocale)}"

            val questionsTitle = Messages.questions(i18nLocale)
            field {
                inline = true
                name = Messages.question_truefalse_title(
                    EmojiConfiguration.correctWithBackground.visualizer(),
                    EmojiConfiguration.incorrectWithBackground.visualizer(),
                    i18nLocale
                )
                value = "$questionsTrueFalseCount $questionsTitle"
            }

            field {
                inline = true
                name = Messages.question_mcq_title(
                    EmojiConfiguration.mcq.visualizer(),
                    i18nLocale
                )
                value = "$questionsMCQCount $questionsTitle"
            }

            field {
                name = "Quiz"
                value = quizzesSubList.joinToString("\n") {
                    "**${it.id}**. ${it.title}"
                }
            }
        }
    }
}
