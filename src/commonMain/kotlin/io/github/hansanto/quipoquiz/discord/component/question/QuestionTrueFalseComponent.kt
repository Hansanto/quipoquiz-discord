package io.github.hansanto.quipoquiz.discord.component.question

import io.github.hansanto.generated.i18n.Messages
import io.github.hansanto.quipoquiz.Language
import io.github.hansanto.quipoquiz.config.EmojiConfiguration
import io.github.hansanto.quipoquiz.discord.framework.builder.message.CustomEmbedBuilder
import io.github.hansanto.quipoquiz.discord.framework.extension.visualizer
import io.github.hansanto.quipoquiz.quipoquiz.QuipoQuizQuestionTrueFalse
import io.github.hansanto.quipoquiz.quipoquiz.QuipoQuizQuestionTrueFalseOverview

/**
 * Display the information for a [QuipoQuizQuestionTrueFalse].
 * If [revealed] is true, the answer will be displayed.
 */
class QuestionTrueFalseComponent(
    override val id: String,
    override val questionOverview: QuipoQuizQuestionTrueFalseOverview,
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
                name = Messages.question_truefalse_title(
                    EmojiConfiguration.correctWithBackground.visualizer(),
                    EmojiConfiguration.incorrectWithBackground.visualizer(),
                    languageLocale
                )
                value = question.title
            }

            if (revealed) {
                field {
                    name = if (question.answer) {
                        Messages.question_true_answer(
                            EmojiConfiguration.correctWithBackground.visualizer(),
                            languageLocale
                        )
                    } else {
                        Messages.question_false_answer(
                            EmojiConfiguration.incorrectWithBackground.visualizer(),
                            languageLocale
                        )
                    }
                    question.explanation?.let { value = it }
                }
            }
        }
    }
}
