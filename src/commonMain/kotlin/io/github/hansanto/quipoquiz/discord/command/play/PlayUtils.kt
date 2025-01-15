package io.github.hansanto.quipoquiz.discord.command.play

import dev.kord.core.entity.User
import dev.kord.core.entity.interaction.ApplicationCommandInteraction
import dev.kord.core.entity.interaction.ChatInputCommandInteraction
import io.github.hansanto.generated.i18n.Messages
import io.github.hansanto.quipoquiz.Language
import io.github.hansanto.quipoquiz.discord.framework.extension.ephemeralError
import io.github.hansanto.quipoquiz.quipoquiz.QuipoQuizCategory
import io.github.hansanto.quipoquiz.quipoquiz.QuipoQuizCategoryId
import io.github.hansanto.quipoquiz.quipoquiz.QuipoQuizQuestionOverview
import io.github.hansanto.quipoquiz.quipoquiz.QuipoQuizQuiz
import io.github.hansanto.quipoquiz.quipoquiz.QuipoQuizQuizId
import io.github.hansanto.quipoquiz.quipoquiz.toQuestionOverviews
import kotlin.contracts.contract

object PlayUtils {

    /**
     * Check if the rival is valid.
     * If the rival is not valid, an ephemeral error message will be sent.
     * @param interaction The interaction that triggered the command.
     * @param rival The rival to check.
     * @param language The language to use for the error message.
     * @return `true` if the rival is valid, `false` otherwise.
     */
    suspend fun rivalIsValid(interaction: ApplicationCommandInteraction, rival: User?, language: Language): Boolean {
        contract {
            returns(true) implies (rival != null)
        }

        return when {
            rival == null -> {
                interaction.ephemeralError(Messages.error_target_user_not_found(language.i18nLocale))
                false
            }

            rival.isBot -> {
                interaction.ephemeralError(Messages.error_target_user_is_bot(language.i18nLocale))
                false
            }

            rival.id == interaction.user.id -> {
                interaction.ephemeralError(Messages.error_target_user_is_self(language.i18nLocale))
                false
            }

            else -> true
        }
    }

    /**
     * Find the selected questions based on the quiz ID (in priority) or category ID.
     * If the quiz ID is provided, the questions from the quiz will be returned (even if the category ID is also provided).
     * If the category ID is provided, the questions from the category will be returned.
     * If neither the quiz ID nor the category ID is provided, all questions from all categories will be returned.
     * If an error occurs, an ephemeral error message will be sent.
     * @param interaction The interaction that triggered the command.
     * @param language The language to use for the error message.
     * @param categories The list of categories to search in.
     * @param categoryId The ID of the category to find.
     * @param quizId The ID of the quiz to find.
     * @return The sequence of questions if found, null in case of error.
     */
    suspend fun getSelectedQuestions(
        interaction: ChatInputCommandInteraction,
        language: Language,
        categories: List<QuipoQuizCategory>,
        categoryId: QuipoQuizCategoryId?,
        quizId: QuipoQuizQuizId?
    ): Sequence<QuipoQuizQuestionOverview>? = when {
        // If the quiz ID and category ID are both provided, we prioritize the quiz ID
        // because Discord caches the autocomplete result, so without an input in quiz ID argument,
        // the user can select a category ID, and all quiz IDs are displayed (even those from other categories).
        quizId != null -> getCategoryWithQuiz(categories, quizId)?.toQuestionOverviews() ?: run {
            interaction.ephemeralError(Messages.error_not_found_quiz(language.i18nLocale))
            return null
        }

        categoryId != null -> getCategory(categories, categoryId)?.toQuestionOverviews() ?: run {
            interaction.ephemeralError(Messages.error_not_found_category(language.i18nLocale))
            return null
        }

        else -> categories.toQuestionOverviews()
    }

    /**
     * Find the category with the given [categoryId].
     * @param categories The list of categories to search in.
     * @param categoryId The ID of the category to find.
     * @return The category if found, null otherwise.
     */
    private fun getCategory(categories: List<QuipoQuizCategory>, categoryId: QuipoQuizCategoryId): QuipoQuizCategory? =
        categories.find { it.id == categoryId }

    /**
     * Find the category and quiz with the given [quizId].
     * @return The category and quiz if found, null otherwise.
     */
    private fun getCategoryWithQuiz(
        categories: List<QuipoQuizCategory>,
        quizId: QuipoQuizQuizId
    ): Pair<QuipoQuizCategory, QuipoQuizQuiz>? = categories
        .asSequence()
        .flatMap { category -> category.quizzes.map { quiz -> category to quiz } }
        .find { it.second.id == quizId }
}
