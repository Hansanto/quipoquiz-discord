package io.github.hansanto.quipoquiz.quipoquiz

import de.comahe.i18n4k.Locale
import io.github.hansanto.generated.graphql.fragment.DetailedCategory
import io.github.hansanto.generated.graphql.fragment.Quiz
import io.github.hansanto.generated.graphql.fragment.Quiz.Questions_true_or_false
import io.github.hansanto.generated.i18n.Messages
import io.github.hansanto.quipoquiz.Language
import io.github.hansanto.quipoquiz.cache.Cache
import io.github.hansanto.quipoquiz.cache.CacheSupplier
import io.github.hansanto.quipoquiz.config.QuipoQuizConfiguration
import io.github.hansanto.quipoquiz.extension.removeHtmlTag
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.toCollection
import kotlinx.coroutines.flow.toList

interface QuipoQuizService {
    /**
     * Get the entire data of QuipoQuiz for all languages.
     *
     * @return The entire data of QuipoQuiz.
     */
    suspend fun getQuizData(): QuipoQuizData
}

/**
 * Service to interact with QuipoQuiz data using the repository.
 * The data can be cached to avoid requesting the server each time.
 */
class QuipoQuizServiceImpl(
    /**
     * Repository to interact with QuipoQuiz API.
     */
    private val repository: QuipoQuizRepository,
    /**
     * Cache to store the data.
     */
    private val cache: Cache<String, QuipoQuizData> = CacheSupplier.persistentMemoryWithFileFallback(
        QuipoQuizConfiguration.cacheExpiration,
        QuipoQuizConfiguration.cacheDirectory
    ),
    /**
     * `true` if quizzes without a category should be included in the data, `false` otherwise.
     */
    private val includeQuizWithoutCategory: Boolean = QuipoQuizConfiguration.includeQuizWithoutCategory,
    /**
     * The default color of the category.
     */
    private val defaultColor: String = QuipoQuizConfiguration.defaultCategoryColor
) : QuipoQuizService {

    override suspend fun getQuizData(): QuipoQuizData {
        return cache.getOrSet("all_quizzes") { getCategoriesWithQuizzes() }
    }

    /**
     * Get the list of categories available in QuipoQuiz.
     *
     * This method is not cached and will request the data from the server.
     *
     * @param siteId Language to retrieve the categories.
     * @return The list of categories.
     */
    private fun getCategories(
        siteId: QuipoQuizSiteId,
        allQuizzes: Map<QuipoQuizCategoryId?, List<QuipoQuizQuiz>>
    ): Flow<QuipoQuizCategory> {
        return repository.getCategories(siteId).mapNotNull {
            val id = it.id?.let { QuipoQuizCategoryId(it) } ?: return@mapNotNull null
            val quizzes = allQuizzes[id] ?: return@mapNotNull null
            buildCategory(it, quizzes)
        }
    }

    /**
     * Get the list of quizzes available in QuipoQuiz for a specific language.
     *
     * This method is not cached and will request the data from the server.
     *
     * @param siteId Language to retrieve the quizzes.
     * @return The list of quizzes.
     */
    private fun getQuizzes(siteId: QuipoQuizSiteId): Flow<QuipoQuizQuiz> {
        return repository.getQuizzes(siteId).mapNotNull(::buildQuiz)
    }

    /**
     * Get the list of categories with quizzes available in QuipoQuiz for all languages.
     * Each category is associated with his quizzes.
     * If a quiz has no category and [includeQuizWithoutCategory] is true, it will be put in a default category.
     *
     * This method is not cached and will request the data from the server.
     *
     * @return The list of categories with quizzes.
     */
    private suspend fun getCategoriesWithQuizzes(): QuipoQuizData {
        return Language.entries.associateWith { language ->
            val siteId = language.site

            // For quiz without a category, we're going to put them in a default category
            val allQuizzes = getQuizzes(siteId).toList().sortedWith(
                compareBy {
                    val id = it.id.value
                    id.toLongOrNull() ?: id
                }
            )

            val quizzesByCategory = allQuizzes.groupBy { it.categoryId }

            getCategories(siteId, quizzesByCategory).toCollection(mutableListOf()).apply {
                if (includeQuizWithoutCategory) {
                    quizzesByCategory[null]?.let { quizzes ->
                        add(createUnknownCategory(language.i18nLocale, quizzes))
                    }
                }
            }
        }
    }

    /**
     * Create a default category for quizzes without a category.
     * @return The default category.
     */
    private fun createUnknownCategory(locale: Locale, quizzes: List<QuipoQuizQuiz>) = QuipoQuizCategory(
        id = QuipoQuizCategoryId.none,
        name = Messages.quipoquiz_category_none(locale = locale),
        image = null,
        icon = null,
        color = defaultColor,
        quizzes = quizzes
    )

    /**
     * Build a category from a QuipoQuiz entry.
     * @param entry QuipoQuiz entry.
     * @return A new instance of [QuipoQuizCategory] or null if the entry is invalid.
     */
    private fun buildCategory(entry: DetailedCategory, quizzes: List<QuipoQuizQuiz>): QuipoQuizCategory? {
        return QuipoQuizCategory(
            id = entry.id?.let { QuipoQuizCategoryId(it) } ?: return null,
            name = entry.title ?: return null,
            image = entry.image.asSequence().filterNotNull().firstOrNull()?.url,
            icon = entry.icon.asSequence().filterNotNull().firstOrNull()?.url,
            color = entry.color ?: defaultColor,
            quizzes = quizzes
        )
    }

    /**
     * Build a quiz from a QuipoQuiz entry.
     * @param quizEntry Quiz entry.
     * @return A new instance of [QuipoQuizQuiz] or null if the entry is invalid.
     */
    private fun buildQuiz(quizEntry: Quiz): QuipoQuizQuiz? {
        val id = quizEntry.id?.let { QuipoQuizQuizId(it) } ?: return null
        val title = quizEntry.title?.clean() ?: return null

        val questionsTrueFalse = buildQuestionTrueFalse(quizEntry.questions_true_or_false, id)
        val questionsMCQ = buildQuestionMCQ(quizEntry.questions_multiple_choices, id)
        if (questionsTrueFalse.isEmpty() && questionsMCQ.isEmpty()) return null

        val categoryId = quizEntry.quiz_category
            .asSequence()
            .filterNotNull()
            .firstOrNull()
            ?.id
            ?.let { QuipoQuizCategoryId(it) }

        return QuipoQuizQuiz(
            id = id,
            categoryId = categoryId,
            title = title,
            questionsTrueFalse = questionsTrueFalse,
            questionsMCQ = questionsMCQ
        )
    }

    /**
     * Build a list of true or false questions from a QuipoQuiz entry.
     * @param questionsEntry Quiz entry.
     * @return A list of [QuipoQuizQuestionTrueFalse], the invalid entries are ignored.
     */
    private fun buildQuestionTrueFalse(
        questionsEntry: List<Questions_true_or_false?>,
        quizId: QuipoQuizQuizId
    ): List<QuipoQuizQuestionTrueFalse> {
        return questionsEntry
            .asSequence()
            .mapNotNull { it?.questionTrueOrFalse }
            .distinctBy { it.id }
            .mapNotNull { question ->
                val id = question.id?.let { QuipoQuizQuestionId(it) } ?: return@mapNotNull null
                val title = question.title?.clean() ?: return@mapNotNull null
                val answer = question.answer ?: return@mapNotNull null
                val explanation = question.anwser_explanation?.clean()

                val image = question.image
                    .asSequence()
                    .filterNotNull()
                    .firstOrNull()
                    ?.url

                val answerIsTrue = answer == true
                QuipoQuizQuestionTrueFalse(
                    id = id,
                    quizId = quizId,
                    image = image,
                    title = title,
                    explanation = explanation,
                    trueChoice = QuipoQuizAnswerChoiceTrue(
                        goodAnswer = answerIsTrue
                    ),
                    falseChoice = QuipoQuizAnswerChoiceFalse(
                        goodAnswer = !answerIsTrue
                    )
                )
            }
            .toList()
    }

    private fun buildQuestionMCQ(
        questionsEntry: List<Quiz.Questions_multiple_choice?>,
        quizId: QuipoQuizQuizId
    ): List<QuipoQuizQuestionMCQ> {
        return questionsEntry
            .asSequence()
            .mapNotNull { it?.questionMultipleChoices }
            .distinctBy { it.id }
            .mapNotNull { question ->
                val id = question.id?.let { QuipoQuizQuestionId(it) } ?: return@mapNotNull null
                val title = question.title?.clean() ?: return@mapNotNull null

                val answerChoices = question
                    .anwser_choices
                    ?.asSequence()
                    ?.filterNotNull()
                    ?.map { it.answerChoice }
                    ?.mapIndexedNotNull buildMCQ@{ numberChoice, answerChoice ->
                        val choice = answerChoice.choice?.clean() ?: return@buildMCQ null
                        QuipoQuizAnswerChoiceMCQ(
                            id = QuipoQuizAnswerChoiceId(numberChoice),
                            choice = choice,
                            goodAnswer = answerChoice.good_answer == true
                        )
                    }
                    ?.toList()

                if (
                    answerChoices == null || // Do not allow no choices
                    answerChoices.size < 2 || // Do not allow less than 2 choices
                    !answerChoices.any { it.goodAnswer } // Do not allow questions without at least one good answer
                ) {
                    return@mapNotNull null
                }

                val explanation = question.anwser_explanation?.clean()

                val image = question.image
                    .asSequence()
                    .filterNotNull()
                    .firstOrNull()
                    ?.url

                QuipoQuizQuestionMCQ(
                    id = id,
                    quizId = quizId,
                    image = image,
                    title = title,
                    explanation = explanation,
                    choices = answerChoices
                )
            }
            .toList()
    }

    /**
     * Clean a string by removing HTML tags and trimming it.
     * @receiver String to clean.
     * @return The cleaned string.
     */
    private fun String.clean(): String {
        return removeHtmlTag().trim()
    }
}
