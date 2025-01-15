package io.github.hansanto.quipoquiz.quipoquiz

import dev.kord.common.entity.ButtonStyle
import dev.kord.common.entity.DiscordPartialEmoji
import io.github.hansanto.quipoquiz.Language
import io.github.hansanto.quipoquiz.config.EmojiConfiguration
import io.github.hansanto.quipoquiz.discord.component.question.QuestionComponent
import io.github.hansanto.quipoquiz.discord.component.question.QuestionMCQComponent
import io.github.hansanto.quipoquiz.discord.component.question.QuestionTrueFalseComponent
import io.github.hansanto.quipoquiz.discord.framework.extension.numberEmojiOrNull
import io.github.hansanto.quipoquiz.discord.framework.extension.numbers
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline

/**
 * Type representing the entire data of QuipoQuiz.
 * Each language is associated with a list of categories with quizzes.
 */
typealias QuipoQuizData = Map<Language, List<QuipoQuizCategory>>

/**
 * Create a sequence where all questions with two possible answers (true or false) are associated with their quiz
 * and category.
 * @receiver Collection of categories.
 * @return New sequence.
 */
fun Iterable<QuipoQuizCategory>.toQuestionOverviews(): Sequence<QuipoQuizQuestionOverview> {
    return sequence {
        for (category in this@toQuestionOverviews) {
            for (quiz in category.quizzes) {
                for (question in quiz.allQuestions()) {
                    yield(question.toOverview(quiz, category))
                }
            }
        }
    }
}

/**
 * Create a sequence where all questions with two possible answers (true or false) are associated with their quiz
 * and category.
 * @receiver Collection of categories.
 * @return New sequence.
 */
fun QuipoQuizCategory.toQuestionOverviews(): Sequence<QuipoQuizQuestionOverview> {
    return sequence {
        for (quiz in quizzes) {
            for (question in quiz.allQuestions()) {
                yield(question.toOverview(quiz, this@toQuestionOverviews))
            }
        }
    }
}

/**
 * Create a sequence where all questions with two possible answers (true or false) are associated with their quiz
 * and category.
 * @receiver Pair of category and quiz.
 * @return New sequence.
 */
fun Pair<QuipoQuizCategory, QuipoQuizQuiz>.toQuestionOverviews(): Sequence<QuipoQuizQuestionOverview> {
    return sequence {
        for (question in second.allQuestions()) {
            yield(question.toOverview(second, first))
        }
    }
}

@Serializable
@JvmInline
value class QuipoQuizCategoryId(val value: String) {

    companion object {
        /**
         * Identifier for the category "none".
         */
        val none = QuipoQuizCategoryId("none")
    }

    override fun toString(): String = value
}

/**
 * All information about a category for a language.
 */
@Serializable
data class QuipoQuizCategory(
    /**
     * The unique identifier.
     */
    val id: QuipoQuizCategoryId,
    /**
     * The name for a language.
     */
    val name: String,
    /**
     * Image of the category.
     */
    val image: String?,
    /**
     * Icon of the category.
     */
    val icon: String?,
    /**
     * Color associated with the category.
     */
    val color: String,
    /**
     * All quizzes in the category.
     */
    val quizzes: List<QuipoQuizQuiz>
)

@Serializable
@JvmInline
value class QuipoQuizQuizId(val value: String) {

    override fun toString(): String = value
}

/**
 * All information about a quiz for a language.
 */
@Serializable
data class QuipoQuizQuiz(
    /**
     * The unique identifier.
     */
    val id: QuipoQuizQuizId,
    /**
     * The category identifier.
     */
    val categoryId: QuipoQuizCategoryId?,
    /**
     * The title for a language.
     */
    val title: String,
    /**
     * All questions with two possible answers (true or false).
     */
    val questionsTrueFalse: List<QuipoQuizQuestionTrueFalse>,
    /**
     * All questions with multiple choices.
     */
    val questionsMCQ: List<QuipoQuizQuestionMCQ>
) {

    /**
     * Get all questions.
     * @return Sequence of questions.
     */
    fun allQuestions(): Sequence<QuipoQuizQuestion> {
        return sequence {
            yieldAll(questionsTrueFalse)
            yieldAll(questionsMCQ)
        }
    }
}

/**
 * All information about a question with two possible answers (true or false).
 */
@Serializable
data class QuipoQuizQuestionTrueFalse(
    override val id: QuipoQuizQuestionId,
    override val quizId: QuipoQuizQuizId,
    override val image: String?,
    override var title: String,
    override var explanation: String?,
    /**
     * The "true" choice, can be the correct or incorrect answer.
     */
    val trueChoice: QuipoQuizAnswerChoiceTrue,
    /**
     * The "false" choice, can be the correct or incorrect answer.
     */
    val falseChoice: QuipoQuizAnswerChoiceFalse
) : QuipoQuizQuestion {

    @Contextual
    override val authorizeMultipleChoices: Boolean
        get() = false

    // To change the order of the choices in Discord interface, change the order of the list.
    @Contextual
    override val choices: Collection<QuipoQuizAnswerChoice> = listOf(trueChoice, falseChoice)

    @Contextual
    val answer: Boolean
        get() = trueChoice.goodAnswer

    override fun toOverview(quiz: QuipoQuizQuiz, category: QuipoQuizCategory): QuipoQuizQuestionOverview {
        return QuipoQuizQuestionTrueFalseOverview(this, quiz, category)
    }
}

@Serializable
data class QuipoQuizQuestionMCQ(
    override val id: QuipoQuizQuestionId,
    override val quizId: QuipoQuizQuizId,
    override val image: String?,
    override var title: String,
    override var explanation: String?,
    override val choices: Collection<QuipoQuizAnswerChoiceMCQ>
) : QuipoQuizQuestion {

    @Contextual
    override val authorizeMultipleChoices: Boolean
        get() = true

    override fun toOverview(quiz: QuipoQuizQuiz, category: QuipoQuizCategory): QuipoQuizQuestionOverview {
        return QuipoQuizQuestionMCQOverview(this, quiz, category)
    }
}

@Serializable
@JvmInline
value class QuipoQuizQuestionId(val value: String) {

    override fun toString(): String = value
}

interface QuipoQuizQuestion {
    /**
     * The unique identifier.
     */
    val id: QuipoQuizQuestionId

    /**
     * The quiz identifier.
     */
    val quizId: QuipoQuizQuizId

    /**
     * The image associated with the question.
     */
    val image: String?

    /**
     * The title for a language.
     */
    var title: String

    /**
     * The explanation of the answer to explain why it is true or false.
     */
    var explanation: String?

    /**
     * `true` if the question can have multiple choices, `false` if only one choice is possible.
     */
    val authorizeMultipleChoices: Boolean

    /**
     * List of possible choices.
     * @return List of possible choice.
     */
    val choices: Collection<QuipoQuizAnswerChoice>

    /**
     * Get the IDs of the correct answers.
     * @return List of IDs.
     */
    fun answers(): Collection<QuipoQuizAnswerChoiceId> {
        return choices.filter { it.goodAnswer }.map { it.id }
    }

    /**
     * Convert the question to [QuipoQuizQuestionOverview].
     * @param quiz Quiz.
     * @param category Category.
     * @return Question overview.
     */
    fun toOverview(quiz: QuipoQuizQuiz, category: QuipoQuizCategory): QuipoQuizQuestionOverview
}

@Serializable
@JvmInline
value class QuipoQuizAnswerChoiceId(val value: Int) {

    override fun toString(): String = value.toString()
}

sealed interface QuipoQuizAnswerChoice {
    /**
     * The unique identifier.
     */
    val id: QuipoQuizAnswerChoiceId

    /**
     * Label of the choice.
     */
    val choice: String

    /**
     * `true` if it is the correct answer, `false` otherwise.
     */
    val goodAnswer: Boolean

    /**
     * The emoji associated with the choice.
     */
    val symbol: DiscordPartialEmoji

    /**
     * The style of the button.
     */
    val style: ChoiceStyle
}

/**
 * Style associated with a choice.
 * @property button Button style to interact with the choice.
 */
enum class ChoiceStyle(val button: ButtonStyle) {
    BLUE(ButtonStyle.Primary),
    GREEN(ButtonStyle.Success),
    RED(ButtonStyle.Danger)
}

@Serializable
data class QuipoQuizAnswerChoiceTrue(
    override val goodAnswer: Boolean
) : QuipoQuizAnswerChoice {

    @Contextual
    override val id: QuipoQuizAnswerChoiceId
        get() = QuipoQuizAnswerChoiceId(0)

    @Contextual
    override val choice: String
        get() = "true"

    @Contextual
    override val symbol: DiscordPartialEmoji
        get() = EmojiConfiguration.correct

    @Contextual
    override val style: ChoiceStyle
        get() = ChoiceStyle.GREEN
}

@Serializable
data class QuipoQuizAnswerChoiceFalse(
    override val goodAnswer: Boolean
) : QuipoQuizAnswerChoice {

    @Contextual
    override val id: QuipoQuizAnswerChoiceId
        get() = QuipoQuizAnswerChoiceId(1)

    @Contextual
    override val choice: String
        get() = "false"

    @Contextual
    override val symbol: DiscordPartialEmoji
        get() = EmojiConfiguration.incorrect

    @Contextual
    override val style: ChoiceStyle
        get() = ChoiceStyle.RED
}

@Serializable
data class QuipoQuizAnswerChoiceMCQ(
    override val id: QuipoQuizAnswerChoiceId,
    override val choice: String,
    override val goodAnswer: Boolean
) : QuipoQuizAnswerChoice {

    @Contextual
    override val symbol: DiscordPartialEmoji =
        DiscordPartialEmoji.numberEmojiOrNull(id.value)
            ?: throw IllegalArgumentException("The ID must be between 0 and ${DiscordPartialEmoji.numbers.lastIndex}.")

    @Contextual
    override val style: ChoiceStyle
        get() = ChoiceStyle.BLUE
}

/**
 * All information about a question associated with its quiz and category.
 * @property question Question.
 * @property quiz Quiz.
 * @property category Category.
 */
@Serializable
data class QuipoQuizQuestionTrueFalseOverview(
    override val question: QuipoQuizQuestionTrueFalse,
    override val quiz: QuipoQuizQuiz,
    override val category: QuipoQuizCategory
) : QuipoQuizQuestionOverview {

    override fun toComponent(id: String, language: Language): QuestionComponent {
        return QuestionTrueFalseComponent(
            id = id,
            questionOverview = this,
            language = language
        )
    }
}

/**
 * All information about a question associated with its quiz and category.
 * @property question Question.
 * @property quiz Quiz.
 * @property category Category.
 */
@Serializable
data class QuipoQuizQuestionMCQOverview(
    override val question: QuipoQuizQuestionMCQ,
    override val quiz: QuipoQuizQuiz,
    override val category: QuipoQuizCategory
) : QuipoQuizQuestionOverview {

    override fun toComponent(id: String, language: Language): QuestionComponent {
        return QuestionMCQComponent(
            id = id,
            questionOverview = this,
            language = language
        )
    }
}

interface QuipoQuizQuestionOverview {
    val question: QuipoQuizQuestion
    val quiz: QuipoQuizQuiz
    val category: QuipoQuizCategory

    /**
     * Convert the overview to a component.
     * @param id Identifier.
     * @param language Language to display the information.
     * @return Component.
     */
    fun toComponent(id: String, language: Language): QuestionComponent
}
