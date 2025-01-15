package io.github.hansanto.quipoquiz.discord.component.question

import io.github.hansanto.generated.i18n.Messages
import io.github.hansanto.quipoquiz.Language
import io.github.hansanto.quipoquiz.config.EmojiConfiguration
import io.github.hansanto.quipoquiz.discord.framework.builder.message.CustomEmbedBuilder
import io.github.hansanto.quipoquiz.discord.framework.extension.visualizer
import io.github.hansanto.quipoquiz.quipoquiz.QuipoQuizQuestionMCQOverview

class QuestionMCQComponent(
    override val id: String,
    override val questionOverview: QuipoQuizQuestionMCQOverview,
    /**
     * The language to display the information.
     */
    val language: Language,
    override var revealed: Boolean = false
) : AbstractQuestionComponent(
    id,
    questionOverview,
    revealed
) {

    override suspend fun render(builder: CustomEmbedBuilder) {
        super.render(builder)
        val question = questionOverview.question
        val languageLocale = language.i18nLocale

        with(builder) {
            field {
                name = Messages.question_mcq_title(
                    EmojiConfiguration.mcqWithBackground.visualizer(),
                    languageLocale
                )
                value = question.title
            }

            if (revealed) {
                field {
                    val correct = question.choices.filter { it.goodAnswer }.map { it.symbol.visualizer() }
                    name = Messages.question_mcq_answer(correct.joinToString(", "), languageLocale)
                    question.explanation?.let { value = it }
                }
            }

            field {
                name = Messages.question_mcq_title_choice(languageLocale)
                value = question.choices.joinToString("\n") { choice ->
                    "${choice.symbol.visualizer()} ${choice.choice}"
                }
            }
        }
    }
}
