package io.github.hansanto.quipoquiz.quipoquiz

import de.comahe.i18n4k.Locale
import dev.mokkery.answering.returns
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.mock
import dev.mokkery.verify.VerifyMode.Companion.exactly
import dev.mokkery.verifySuspend
import io.github.hansanto.generated.graphql.fragment.AnswerChoice
import io.github.hansanto.generated.graphql.fragment.DetailedCategory
import io.github.hansanto.generated.graphql.fragment.QuestionMultipleChoices
import io.github.hansanto.generated.graphql.fragment.QuestionTrueOrFalse
import io.github.hansanto.generated.graphql.fragment.Quiz
import io.github.hansanto.generated.i18n.Messages
import io.github.hansanto.quipoquiz.Language
import io.github.hansanto.quipoquiz.cache.Cache
import io.github.hansanto.quipoquiz.cache.CacheSupplier
import io.github.hansanto.quipoquiz.util.matcher.expirationAfterWrite
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.flow

private const val EXPECTED_CACHE_KEY = "all_quizzes"

class QuipoQuizServiceImplTest : ShouldSpec({

    lateinit var repository: QuipoQuizRepository

    lateinit var cache: Cache<String, QuipoQuizData>

    lateinit var service: QuipoQuizServiceImpl

    beforeTest {
        repository = mock { }
        cache = CacheSupplier.memory<String, QuipoQuizData>(expirationAfterWrite)
        service = QuipoQuizServiceImpl(repository, cache)
    }

    should("getQuizData returns new value set to cache") {
        val (_, frQuizzes) = mockCategoryWithQuiz(repository, QuipoQuizSiteId.FR, 1) {
            add(
                createQuiz(
                    id = 2,
                    categoryId = it,
                    questionsTrueFalse = listOf(createQuestionTrueOrFalse(id = 100)),
                    questionsMultipleChoice = listOf(createQuestionMCQ(id = 110))
                )
            )
        }
        val (_, enQuizzes) = mockCategoryWithQuiz(repository, QuipoQuizSiteId.EN, 2) {
            add(
                createQuiz(
                    id = 3,
                    categoryId = it,
                    questionsTrueFalse = listOf(createQuestionTrueOrFalse(id = 200)),
                    questionsMultipleChoice = listOf(createQuestionMCQ(id = 210))
                )
            )
        }

        val expectedData = mapOf(
            Language.FRENCH to listOf(
                createBuiltCategory(
                    id = QuipoQuizCategoryId("1"),
                    quizzes = frQuizzes.map { createBuiltQuiz(it) }
                )
            ),
            Language.ENGLISH to listOf(
                createBuiltCategory(
                    id = QuipoQuizCategoryId("2"),
                    quizzes = enQuizzes.map { createBuiltQuiz(it) }
                )
            )
        )
        service.getQuizData() shouldBe expectedData

        QuipoQuizSiteId.entries.forEach {
            verifySuspend(exactly(1)) { repository.getCategories(it) }
            verifySuspend(exactly(1)) { repository.getQuizzes(it) }
        }

        cache.get(EXPECTED_CACHE_KEY) shouldBe expectedData
    }

    should("getQuizData returns data from cache") {
        val expectedData = mapOf(
            Language.FRENCH to listOf(
                createBuiltCategory(id = QuipoQuizCategoryId("1"), quizzes = emptyList())
            )
        )

        cache.set(EXPECTED_CACHE_KEY, expectedData)

        service.getQuizData() shouldBe expectedData
        verifySuspend(exactly(0)) { repository.getCategories(any()) }
        verifySuspend(exactly(0)) { repository.getQuizzes(any()) }
    }

    should("getQuizData returns empty data if no category and exclude quizzes without category") {
        mockGetCategories(repository, QuipoQuizSiteId.FR) {
        }
        mockGetQuizzes(repository, QuipoQuizSiteId.FR) {
            emit(
                createQuiz(
                    id = 0,
                    categoryId = null,
                    questionsTrueFalse = listOf(createQuestionTrueOrFalse(id = 100)),
                    questionsMultipleChoice = listOf(createQuestionMCQ(id = 110))
                )
            )
        }

        mockGetCategories(repository, QuipoQuizSiteId.EN) {
        }
        mockGetQuizzes(repository, QuipoQuizSiteId.EN) {
            emit(
                createQuiz(
                    id = 1,
                    categoryId = null,
                    questionsTrueFalse = listOf(createQuestionTrueOrFalse(id = 200)),
                    questionsMultipleChoice = listOf(createQuestionMCQ(id = 210))
                )
            )
        }

        val serviceWithoutOption = QuipoQuizServiceImpl(repository, cache, includeQuizWithoutCategory = false)
        serviceWithoutOption.getQuizData() shouldBe mapOf(
            Language.FRENCH to emptyList(),
            Language.ENGLISH to emptyList()
        )
    }

    should("getQuizData returns quizzes without category if no category and include them enabled") {
        val (frCat1, frCat1Quiz) = mockCategoryWithQuiz(repository, QuipoQuizSiteId.FR, 1) {
            add(
                createQuiz(
                    id = 1,
                    categoryId = it,
                    questionsTrueFalse = listOf(createQuestionTrueOrFalse(id = 101)),
                    questionsMultipleChoice = listOf(createQuestionMCQ(id = 111))
                )
            )
        }
        val frNoCatQuiz = listOf(
            createQuiz(
                id = 0,
                categoryId = null,
                questionsTrueFalse = listOf(createQuestionTrueOrFalse(id = 100)),
                questionsMultipleChoice = listOf(createQuestionMCQ(id = 110))
            )
        )

        val (enCat2, enCat2Quiz) = mockCategoryWithQuiz(repository, QuipoQuizSiteId.EN, 2) {
            add(
                createQuiz(
                    id = 2,
                    categoryId = it,
                    questionsTrueFalse = listOf(createQuestionTrueOrFalse(id = 200)),
                    questionsMultipleChoice = listOf(createQuestionMCQ(id = 210))
                )
            )
        }
        val enNoCatQuiz = listOf(
            createQuiz(
                id = 3,
                categoryId = null,
                questionsTrueFalse = listOf(createQuestionTrueOrFalse(id = 201)),
                questionsMultipleChoice = listOf(createQuestionMCQ(id = 210))
            )
        )

        mockGetQuizzes(repository, QuipoQuizSiteId.FR) {
            frCat1Quiz.forEach { emit(it) }
            frNoCatQuiz.forEach { emit(it) }
        }

        mockGetQuizzes(repository, QuipoQuizSiteId.EN) {
            enCat2Quiz.forEach { emit(it) }
            enNoCatQuiz.forEach { emit(it) }
        }

        val defaultColor = "This is my default color"
        val serviceWithOption = QuipoQuizServiceImpl(
            repository,
            cache,
            includeQuizWithoutCategory = true,
            defaultColor = defaultColor
        )

        serviceWithOption.getQuizData() shouldBe mapOf(
            Language.FRENCH to listOf(
                createBuiltCategory(
                    id = QuipoQuizCategoryId(frCat1.id!!),
                    quizzes = frCat1Quiz.map { createBuiltQuiz(it) }
                ),
                createBuiltUnknownCategory(
                    Language.FRENCH.i18nLocale,
                    defaultColor,
                    frNoCatQuiz.map { createBuiltQuiz(it) }
                )
            ),
            Language.ENGLISH to listOf(
                createBuiltCategory(
                    id = QuipoQuizCategoryId(enCat2.id!!),
                    quizzes = enCat2Quiz.map { createBuiltQuiz(it) }
                ),
                createBuiltUnknownCategory(
                    Language.ENGLISH.i18nLocale,
                    defaultColor,
                    enNoCatQuiz.map { createBuiltQuiz(it) }
                )
            )
        )

        QuipoQuizSiteId.entries.forEach {
            verifySuspend(exactly(1)) { repository.getCategories(it) }
            verifySuspend(exactly(1)) { repository.getQuizzes(it) }
        }
    }

    should(
        "getQuizData includes quizzes without category when there is at least one category and include them enabled"
    ) {
        val frNoCatQuiz = listOf(
            createQuiz(
                id = 0,
                categoryId = null,
                questionsTrueFalse = listOf(createQuestionTrueOrFalse(id = 100)),
                questionsMultipleChoice = listOf(createQuestionMCQ(id = 110))
            )
        )
        val (frCat1, frCat1Quiz) = mockCategoryWithQuiz(repository, QuipoQuizSiteId.FR, 1) {
            add(
                createQuiz(
                    id = 1,
                    categoryId = it,
                    questionsTrueFalse = listOf(createQuestionTrueOrFalse(id = 101)),
                    questionsMultipleChoice = listOf(createQuestionMCQ(id = 111))
                )
            )
        }

        val enNoCatQuiz = listOf(
            createQuiz(
                id = 2,
                categoryId = null,
                questionsTrueFalse = listOf(createQuestionTrueOrFalse(id = 200)),
                questionsMultipleChoice = listOf(createQuestionMCQ(id = 210))
            ),
            createQuiz(
                id = 3,
                categoryId = null,
                questionsTrueFalse = listOf(
                    createQuestionTrueOrFalse(id = 201),
                    createQuestionTrueOrFalse(id = 202)
                ),
                questionsMultipleChoice = listOf(
                    createQuestionMCQ(id = 211),
                    createQuestionMCQ(id = 212)
                )
            )
        )
        val (enCat2, enCat2Quiz) = mockCategoryWithQuiz(repository, QuipoQuizSiteId.EN, 2) {
            add(
                createQuiz(
                    id = 4,
                    categoryId = it,
                    questionsTrueFalse = listOf(createQuestionTrueOrFalse(id = 203)),
                    questionsMultipleChoice = listOf(createQuestionMCQ(id = 213))
                )
            )
        }

        mockGetQuizzes(repository, QuipoQuizSiteId.FR) {
            frCat1Quiz.forEach { emit(it) }
            frNoCatQuiz.forEach { emit(it) }
        }

        mockGetQuizzes(repository, QuipoQuizSiteId.EN) {
            enCat2Quiz.forEach { emit(it) }
            enNoCatQuiz.forEach { emit(it) }
        }

        val defaultColor = "My color"
        val serviceWithOption = QuipoQuizServiceImpl(
            repository,
            cache,
            includeQuizWithoutCategory = true,
            defaultColor = defaultColor
        )

        serviceWithOption.getQuizData() shouldBe mapOf(
            Language.FRENCH to listOf(
                createBuiltCategory(
                    id = QuipoQuizCategoryId(frCat1.id!!),
                    quizzes = frCat1Quiz.map { createBuiltQuiz(it) }
                ),
                createBuiltUnknownCategory(
                    Language.FRENCH.i18nLocale,
                    defaultColor,
                    frNoCatQuiz.map { createBuiltQuiz(it) }
                )
            ),
            Language.ENGLISH to listOf(
                createBuiltCategory(
                    id = QuipoQuizCategoryId(enCat2.id!!),
                    quizzes = enCat2Quiz.map { createBuiltQuiz(it) }
                ),
                createBuiltUnknownCategory(
                    Language.ENGLISH.i18nLocale,
                    defaultColor,
                    enNoCatQuiz.map { createBuiltQuiz(it) }
                )
            )
        )

        QuipoQuizSiteId.entries.forEach {
            verifySuspend(exactly(1)) { repository.getCategories(it) }
            verifySuspend(exactly(1)) { repository.getQuizzes(it) }
        }
    }

    should(
        "getQuizData excludes quizzes without category when there is at least one category and include them disabled"
    ) {
        val (frCat1, frCat1Quiz) = mockCategoryWithQuiz(repository, QuipoQuizSiteId.FR, 1) {
            add(
                createQuiz(
                    id = 2,
                    categoryId = it,
                    questionsTrueFalse = listOf(createQuestionTrueOrFalse(id = 100)),
                    questionsMultipleChoice = listOf(createQuestionMCQ(id = 110))
                )
            )
        }
        val frNoCatQuiz = listOf(
            createQuiz(
                id = 3,
                categoryId = null,
                questionsTrueFalse = listOf(createQuestionTrueOrFalse(id = 101)),
                questionsMultipleChoice = listOf(createQuestionMCQ(id = 111))
            )
        )

        val (enCat2, enCat2Quiz) = mockCategoryWithQuiz(repository, QuipoQuizSiteId.EN, 2) {
            add(
                createQuiz(
                    id = 3,
                    categoryId = it,
                    questionsTrueFalse = listOf(createQuestionTrueOrFalse(id = 200)),
                    questionsMultipleChoice = listOf(createQuestionMCQ(id = 210))
                )
            )
        }
        val enNoCatQuiz = listOf(
            createQuiz(
                id = 4,
                categoryId = null,
                questionsTrueFalse = listOf(createQuestionTrueOrFalse(id = 201)),
                questionsMultipleChoice = listOf(createQuestionMCQ(id = 211))
            ),
            createQuiz(
                id = 5,
                categoryId = null,
                questionsTrueFalse = listOf(
                    createQuestionTrueOrFalse(id = 202),
                    createQuestionTrueOrFalse(id = 203)
                ),
                questionsMultipleChoice = listOf(
                    createQuestionMCQ(id = 212),
                    createQuestionMCQ(id = 213)
                )
            )
        )

        mockGetQuizzes(repository, QuipoQuizSiteId.FR) {
            frCat1Quiz.forEach { emit(it) }
            frNoCatQuiz.forEach { emit(it) }
        }

        mockGetQuizzes(repository, QuipoQuizSiteId.EN) {
            enCat2Quiz.forEach { emit(it) }
            enNoCatQuiz.forEach { emit(it) }
        }

        val serviceWithoutOption = QuipoQuizServiceImpl(repository, cache, includeQuizWithoutCategory = false)
        serviceWithoutOption.getQuizData() shouldBe mapOf(
            Language.FRENCH to listOf(
                createBuiltCategory(
                    id = QuipoQuizCategoryId(frCat1.id!!),
                    quizzes = frCat1Quiz.map { createBuiltQuiz(it) }
                )
            ),
            Language.ENGLISH to listOf(
                createBuiltCategory(
                    id = QuipoQuizCategoryId(enCat2.id!!),
                    quizzes = enCat2Quiz.map { createBuiltQuiz(it) }
                )
            )
        )

        QuipoQuizSiteId.entries.forEach {
            verifySuspend(exactly(1)) { repository.getCategories(it) }
            verifySuspend(exactly(1)) { repository.getQuizzes(it) }
        }
    }

    should("getQuizData ignores quizzes linked to an unknown category") {
        val (frCat1, frCat1Quiz) = mockCategoryWithQuiz(repository, QuipoQuizSiteId.FR, 1) {
            add(
                createQuiz(
                    id = 2,
                    categoryId = it,
                    questionsTrueFalse = listOf(createQuestionTrueOrFalse(id = 100)),
                    questionsMultipleChoice = listOf(createQuestionMCQ(id = 110))
                )
            )
        }
        val frCat3Quiz = listOf(
            createQuiz(
                id = 3,
                categoryId = 3,
                questionsTrueFalse = listOf(createQuestionTrueOrFalse(id = 101)),
                questionsMultipleChoice = listOf(createQuestionMCQ(id = 111))
            )
        ) // is ignored because category 3 is unknown
        mockGetQuizzes(repository, QuipoQuizSiteId.FR) {
            frCat1Quiz.forEach { emit(it) }
            frCat3Quiz.forEach { emit(it) }
        }

        val (enCat2, enCat2Quiz) = mockCategoryWithQuiz(repository, QuipoQuizSiteId.EN, 2) {
            add(
                createQuiz(
                    id = 3,
                    categoryId = it,
                    questionsTrueFalse = listOf(createQuestionTrueOrFalse(id = 200)),
                    questionsMultipleChoice = listOf(createQuestionMCQ(id = 210))
                )
            )
        }
        val enCat4Quiz = listOf(
            createQuiz(
                id = 4,
                categoryId = 4,
                questionsTrueFalse = listOf(createQuestionTrueOrFalse(id = 201)),
                questionsMultipleChoice = listOf(createQuestionMCQ(id = 211))
            )
        ) // is ignored because category 4 is unknown
        mockGetQuizzes(repository, QuipoQuizSiteId.EN) {
            enCat2Quiz.forEach { emit(it) }
            enCat4Quiz.forEach { emit(it) }
        }

        service.getQuizData() shouldBe mapOf(
            Language.FRENCH to listOf(
                createBuiltCategory(
                    id = QuipoQuizCategoryId(frCat1.id!!),
                    quizzes = frCat1Quiz.map { createBuiltQuiz(it) }
                )
            ),
            Language.ENGLISH to listOf(
                createBuiltCategory(
                    id = QuipoQuizCategoryId(enCat2.id!!),
                    quizzes = enCat2Quiz.map { createBuiltQuiz(it) }
                )
            )
        )

        QuipoQuizSiteId.entries.forEach {
            verifySuspend(exactly(1)) { repository.getCategories(it) }
            verifySuspend(exactly(1)) { repository.getQuizzes(it) }
        }
    }

    should("getQuizData excludes category with no quizzes") {
        val (frCat1, frCat1Quiz) = mockCategoryWithQuiz(repository, QuipoQuizSiteId.FR, 1) {
            add(
                createQuiz(
                    id = 1,
                    categoryId = it,
                    questionsTrueFalse = listOf(createQuestionTrueOrFalse(id = 100)),
                    questionsMultipleChoice = listOf(createQuestionMCQ(id = 110))
                )
            )
            add(
                createQuiz(
                    id = 2,
                    categoryId = it,
                    questionsTrueFalse = listOf(createQuestionTrueOrFalse(id = 101)),
                    questionsMultipleChoice = listOf(createQuestionMCQ(id = 111))
                )
            )
        }

        mockGetCategories(repository, QuipoQuizSiteId.FR) {
            emit(createDetailedCategory(id = frCat1.id!!.toLong()))
            emit(createDetailedCategory(id = 2)) // is ignored
        }

        val (enCat2, enCat2Quiz) = mockCategoryWithQuiz(repository, QuipoQuizSiteId.EN, 2) {
            add(
                createQuiz(
                    id = 3,
                    categoryId = it,
                    questionsTrueFalse = listOf(createQuestionTrueOrFalse(id = 200)),
                    questionsMultipleChoice = listOf(createQuestionMCQ(id = 210))
                )
            )
            add(
                createQuiz(
                    id = 4,
                    categoryId = it,
                    questionsTrueFalse = listOf(createQuestionTrueOrFalse(id = 201)),
                    questionsMultipleChoice = listOf(createQuestionMCQ(id = 211))
                )
            )
        }

        mockGetCategories(repository, QuipoQuizSiteId.EN) {
            emit(createDetailedCategory(id = enCat2.id!!.toLong()))
            emit(createDetailedCategory(id = 4)) // is ignored
        }

        service.getQuizData() shouldBe mapOf(
            Language.FRENCH to listOf(
                createBuiltCategory(
                    id = QuipoQuizCategoryId(frCat1.id!!),
                    quizzes = frCat1Quiz.map { createBuiltQuiz(it) }
                )
            ),
            Language.ENGLISH to listOf(
                createBuiltCategory(
                    id = QuipoQuizCategoryId(enCat2.id!!),
                    quizzes = enCat2Quiz.map { createBuiltQuiz(it) }
                )
            )
        )

        QuipoQuizSiteId.entries.forEach {
            verifySuspend(exactly(1)) { repository.getCategories(it) }
            verifySuspend(exactly(1)) { repository.getQuizzes(it) }
        }
    }

    should("getQuizData excludes category without id or title") {
        mockGetCategories(repository, QuipoQuizSiteId.FR) {
            emit(createDetailedCategory(id = 1))
            emit(createDetailedCategory(id = null)) // is ignored
            emit(createDetailedCategory(id = 3, title = null)) // is ignored
        }

        mockGetCategories(repository, QuipoQuizSiteId.EN) {
            emit(createDetailedCategory(id = 4))
            emit(createDetailedCategory(id = null)) // is ignored
            emit(createDetailedCategory(id = 6, title = null)) // is ignored
        }

        val frQuizKept = createQuiz(
            id = 1,
            categoryId = 1,
            questionsTrueFalse = listOf(createQuestionTrueOrFalse(id = 100)),
            questionsMultipleChoice = listOf(createQuestionMCQ(id = 110))
        )
        mockGetQuizzes(repository, QuipoQuizSiteId.FR) {
            emit(frQuizKept)
            emit(
                createQuiz(
                    id = 2,
                    categoryId = 3,
                    questionsTrueFalse = listOf(createQuestionTrueOrFalse(id = 101)),
                    questionsMultipleChoice = listOf(createQuestionMCQ(id = 111))
                )
            )
        }

        val enQuizKept = createQuiz(
            id = 3,
            categoryId = 4,
            questionsTrueFalse = listOf(createQuestionTrueOrFalse(id = 200)),
            questionsMultipleChoice = listOf(createQuestionMCQ(id = 210))
        )
        mockGetQuizzes(repository, QuipoQuizSiteId.EN) {
            emit(enQuizKept)
            emit(
                createQuiz(
                    id = 4,
                    categoryId = 6,
                    questionsTrueFalse = listOf(createQuestionTrueOrFalse(id = 201)),
                    questionsMultipleChoice = listOf(createQuestionMCQ(id = 211))
                )
            )
        }

        service.getQuizData() shouldBe mapOf(
            Language.FRENCH to listOf(
                createBuiltCategory(id = QuipoQuizCategoryId("1"), quizzes = listOf(createBuiltQuiz(frQuizKept)))
            ),
            Language.ENGLISH to listOf(
                createBuiltCategory(id = QuipoQuizCategoryId("4"), quizzes = listOf(createBuiltQuiz(enQuizKept)))
            )
        )

        QuipoQuizSiteId.entries.forEach {
            verifySuspend(exactly(1)) { repository.getCategories(it) }
            verifySuspend(exactly(1)) { repository.getQuizzes(it) }
        }
    }

    should("getQuizData excludes quiz without id, title or questions") {
        val (frCat1, frCat1Quiz) = mockCategoryWithQuiz(repository, QuipoQuizSiteId.FR, 1) {
            add(
                createQuiz(
                    id = 1,
                    categoryId = it,
                    questionsTrueFalse = listOf(createQuestionTrueOrFalse(id = 100)),
                    questionsMultipleChoice = listOf(createQuestionMCQ(id = 110))
                )
            )
            add(
                createQuiz(
                    id = null,
                    categoryId = it,
                    questionsTrueFalse = listOf(createQuestionTrueOrFalse(id = 101)),
                    questionsMultipleChoice = listOf(createQuestionMCQ(id = 111))
                )
            ) // is ignored
            add(
                createQuiz(
                    id = 3,
                    title = null,
                    categoryId = it,
                    questionsTrueFalse = listOf(createQuestionTrueOrFalse(id = 102)),
                    questionsMultipleChoice = listOf(createQuestionMCQ(id = 112))
                )
            ) // is ignored
            add(
                createQuiz(
                    id = 4,
                    categoryId = it,
                    questionsTrueFalse = emptyList(),
                    questionsMultipleChoice = emptyList()
                )
            ) // is ignored
            add(
                createQuiz(
                    id = 5,
                    categoryId = it,
                    questionsTrueFalse = listOf(createQuestionTrueOrFalse(id = 103)),
                    questionsMultipleChoice = emptyList()
                )
            )
            add(
                createQuiz(
                    id = 6,
                    categoryId = it,
                    questionsTrueFalse = emptyList(),
                    questionsMultipleChoice = listOf(createQuestionMCQ(id = 113))
                )
            )
        }

        val (enCat2, enCat2Quiz) = mockCategoryWithQuiz(repository, QuipoQuizSiteId.EN, 2) {
            add(
                createQuiz(
                    id = 5,
                    categoryId = it,
                    questionsTrueFalse = listOf(createQuestionTrueOrFalse(id = 200)),
                    questionsMultipleChoice = listOf(createQuestionMCQ(id = 210))
                )
            )
            add(
                createQuiz(
                    id = null,
                    categoryId = it,
                    questionsTrueFalse = listOf(createQuestionTrueOrFalse(id = 201)),
                    questionsMultipleChoice = listOf(createQuestionMCQ(id = 211))
                )
            ) // is ignored
            add(
                createQuiz(
                    id = 7,
                    title = null,
                    categoryId = it,
                    questionsTrueFalse = listOf(createQuestionTrueOrFalse(id = 202)),
                    questionsMultipleChoice = listOf(createQuestionMCQ(id = 212))
                )
            ) // is ignored
            add(
                createQuiz(
                    id = 8,
                    categoryId = it,
                    questionsTrueFalse = emptyList(),
                    questionsMultipleChoice = emptyList()
                )
            ) // is ignored
            add(
                createQuiz(
                    id = 9,
                    categoryId = it,
                    questionsTrueFalse = listOf(createQuestionTrueOrFalse(id = 203)),
                    questionsMultipleChoice = emptyList()
                )
            )
            add(
                createQuiz(
                    id = 10,
                    categoryId = it,
                    questionsTrueFalse = emptyList(),
                    questionsMultipleChoice = listOf(createQuestionMCQ(id = 213))
                )
            )
        }

        fun filterAndCreateQuiz(quizzes: List<Quiz>, vararg ids: String): List<QuipoQuizQuiz> {
            val idSet = ids.toSet()
            return quizzes.filter { it.id in idSet }.map { createBuiltQuiz(it) }
        }

        service.getQuizData() shouldBe mapOf(
            Language.FRENCH to listOf(
                createBuiltCategory(
                    id = QuipoQuizCategoryId(frCat1.id!!),
                    quizzes = filterAndCreateQuiz(frCat1Quiz, "1", "5", "6")
                )
            ),
            Language.ENGLISH to listOf(
                createBuiltCategory(
                    id = QuipoQuizCategoryId(enCat2.id!!),
                    quizzes = filterAndCreateQuiz(enCat2Quiz, "5", "9", "10")
                )
            )
        )

        QuipoQuizSiteId.entries.forEach {
            verifySuspend(exactly(1)) { repository.getCategories(it) }
            verifySuspend(exactly(1)) { repository.getQuizzes(it) }
        }
    }

    should("getQuizData excludes mcq question without at least one good answer") {
        val (frCat1, frCat1Quiz) = mockCategoryWithQuiz(repository, QuipoQuizSiteId.FR, 1) {
            add(
                createQuiz(
                    id = 1,
                    categoryId = it,
                    questionsTrueFalse = emptyList(),
                    questionsMultipleChoice = listOf(
                        createQuestionMCQ(id = 110),
                        // is ignored
                        createQuestionMCQ(id = 111, choices = emptyList())
                    )
                )
            )
        }

        val (enCat2, enCat2Quiz) = mockCategoryWithQuiz(repository, QuipoQuizSiteId.EN, 2) {
            add(
                createQuiz(
                    id = 5,
                    categoryId = it,
                    questionsTrueFalse = emptyList(),
                    questionsMultipleChoice = listOf(
                        // is ignored
                        createQuestionMCQ(id = 210, choices = emptyList()),
                        createQuestionMCQ(id = 211)
                    )
                )
            )
        }

        service.getQuizData() shouldBe mapOf(
            Language.FRENCH to listOf(
                createBuiltCategory(
                    id = QuipoQuizCategoryId(frCat1.id!!),
                    quizzes = listOf(
                        createBuiltQuiz(
                            quiz = frCat1Quiz[0],
                            questionsMultipleChoice = createBuiltQuestionsMCQ(
                                frCat1Quiz[0].questions_multiple_choices.take(1),
                                QuipoQuizQuizId(frCat1Quiz[0].id!!)
                            )
                        )
                    )
                )
            ),
            Language.ENGLISH to listOf(
                createBuiltCategory(
                    id = QuipoQuizCategoryId(enCat2.id!!),
                    quizzes = listOf(
                        createBuiltQuiz(
                            quiz = enCat2Quiz[0],
                            questionsMultipleChoice = createBuiltQuestionsMCQ(
                                enCat2Quiz[0].questions_multiple_choices.drop(1),
                                QuipoQuizQuizId(enCat2Quiz[0].id!!)
                            )
                        )
                    )
                )
            )
        )

        QuipoQuizSiteId.entries.forEach {
            verifySuspend(exactly(1)) { repository.getCategories(it) }
            verifySuspend(exactly(1)) { repository.getQuizzes(it) }
        }
    }

    should("getQuizData excludes mcq question with at least two choices") {
        val (frCat1, frCat1Quiz) = mockCategoryWithQuiz(repository, QuipoQuizSiteId.FR, 1) {
            add(
                createQuiz(
                    id = 1,
                    categoryId = it,
                    questionsTrueFalse = emptyList(),
                    questionsMultipleChoice = listOf(
                        createQuestionMCQ(
                            id = 110,
                            choices = listOf(
                                "choice 1" to true,
                                "choice 2" to false,
                                "choice 3" to true
                            )
                        ),
                        // is ignored
                        createQuestionMCQ(
                            id = 111,
                            choices = listOf(
                                "choice 1" to true
                            )
                        )
                    )
                )
            )
        }

        val (enCat2, enCat2Quiz) = mockCategoryWithQuiz(repository, QuipoQuizSiteId.EN, 2) {
            add(
                createQuiz(
                    id = 5,
                    categoryId = it,
                    questionsTrueFalse = emptyList(),
                    questionsMultipleChoice = listOf(
                        // is ignored
                        createQuestionMCQ(
                            id = 210,
                            choices = listOf(
                                "choice 1" to false
                            )
                        ),
                        createQuestionMCQ(
                            id = 211,
                            choices = listOf(
                                "choice 1" to true,
                                "choice 2" to true
                            )
                        )
                    )
                )
            )
        }

        service.getQuizData() shouldBe mapOf(
            Language.FRENCH to listOf(
                createBuiltCategory(
                    id = QuipoQuizCategoryId(frCat1.id!!),
                    quizzes = listOf(
                        createBuiltQuiz(
                            quiz = frCat1Quiz[0],
                            questionsMultipleChoice = createBuiltQuestionsMCQ(
                                frCat1Quiz[0].questions_multiple_choices.take(1),
                                QuipoQuizQuizId(frCat1Quiz[0].id!!)
                            )
                        )
                    )
                )
            ),
            Language.ENGLISH to listOf(
                createBuiltCategory(
                    id = QuipoQuizCategoryId(enCat2.id!!),
                    quizzes = listOf(
                        createBuiltQuiz(
                            quiz = enCat2Quiz[0],
                            questionsMultipleChoice = createBuiltQuestionsMCQ(
                                enCat2Quiz[0].questions_multiple_choices.drop(1),
                                QuipoQuizQuizId(enCat2Quiz[0].id!!)
                            )
                        )
                    )
                )
            )
        )

        QuipoQuizSiteId.entries.forEach {
            verifySuspend(exactly(1)) { repository.getCategories(it) }
            verifySuspend(exactly(1)) { repository.getQuizzes(it) }
        }
    }

    should("getQuizData remove invalid character in quiz title") {
        val (frCat1, frCat1Quiz) = mockCategoryWithQuiz(repository, QuipoQuizSiteId.FR, 1) {
            add(
                createQuiz(
                    id = 1,
                    categoryId = it,
                    title = "   title 1   ",
                    questionsTrueFalse = listOf(createQuestionTrueOrFalse(id = 100)),
                    questionsMultipleChoice = listOf(createQuestionMCQ(id = 110))
                )
            )
            add(
                createQuiz(
                    id = 2,
                    categoryId = it,
                    title = "<p>  title <br>2   </p>",
                    questionsTrueFalse = listOf(createQuestionTrueOrFalse(id = 101)),
                    questionsMultipleChoice = listOf(createQuestionMCQ(id = 111))
                )
            )
        }

        val (enCat2, enCat2Quiz) = mockCategoryWithQuiz(repository, QuipoQuizSiteId.EN, 2) {
            add(
                createQuiz(
                    id = 3,
                    categoryId = it,
                    title = "   <a>title 3</a>   ",
                    questionsTrueFalse = listOf(createQuestionTrueOrFalse(id = 200)),
                    questionsMultipleChoice = listOf(createQuestionMCQ(id = 210))
                )
            )
            add(
                createQuiz(
                    id = 4,
                    categoryId = it,
                    title = "  <h1>title <p>4</p>   </h1>",
                    questionsTrueFalse = listOf(createQuestionTrueOrFalse(id = 201)),
                    questionsMultipleChoice = listOf(createQuestionMCQ(id = 211))
                )
            )
        }

        service.getQuizData() shouldBe mapOf(
            Language.FRENCH to listOf(
                createBuiltCategory(
                    id = QuipoQuizCategoryId(frCat1.id!!),
                    quizzes = listOf(
                        createBuiltQuiz(
                            id = QuipoQuizQuizId("1"),
                            categoryId = QuipoQuizCategoryId("1"),
                            title = "title 1",
                            questionsTrueFalse = createBuiltQuestionsTrueFalse(
                                frCat1Quiz[0].questions_true_or_false,
                                QuipoQuizQuizId("1")
                            ),
                            questionsMultipleChoice = createBuiltQuestionsMCQ(
                                frCat1Quiz[0].questions_multiple_choices,
                                QuipoQuizQuizId("1")
                            )
                        ),
                        createBuiltQuiz(
                            id = QuipoQuizQuizId("2"),
                            categoryId = QuipoQuizCategoryId("1"),
                            title = "title 2",
                            questionsTrueFalse = createBuiltQuestionsTrueFalse(
                                frCat1Quiz[1].questions_true_or_false,
                                QuipoQuizQuizId("2")
                            ),
                            questionsMultipleChoice = createBuiltQuestionsMCQ(
                                frCat1Quiz[1].questions_multiple_choices,
                                QuipoQuizQuizId("2")
                            )
                        )
                    )
                )
            ),
            Language.ENGLISH to listOf(
                createBuiltCategory(
                    id = QuipoQuizCategoryId(enCat2.id!!),
                    quizzes = listOf(
                        createBuiltQuiz(
                            id = QuipoQuizQuizId("3"),
                            categoryId = QuipoQuizCategoryId("2"),
                            title = "title 3",
                            questionsTrueFalse = createBuiltQuestionsTrueFalse(
                                enCat2Quiz[0].questions_true_or_false,
                                QuipoQuizQuizId("3")
                            ),
                            questionsMultipleChoice = createBuiltQuestionsMCQ(
                                enCat2Quiz[0].questions_multiple_choices,
                                QuipoQuizQuizId("3")
                            )
                        ),
                        createBuiltQuiz(
                            id = QuipoQuizQuizId("4"),
                            categoryId = QuipoQuizCategoryId("2"),
                            title = "title 4",
                            questionsTrueFalse = createBuiltQuestionsTrueFalse(
                                enCat2Quiz[1].questions_true_or_false,
                                QuipoQuizQuizId("4")
                            ),
                            questionsMultipleChoice = createBuiltQuestionsMCQ(
                                enCat2Quiz[1].questions_multiple_choices,
                                QuipoQuizQuizId("4")
                            )
                        )
                    )
                )
            )
        )

        QuipoQuizSiteId.entries.forEach {
            verifySuspend(exactly(1)) { repository.getCategories(it) }
            verifySuspend(exactly(1)) { repository.getQuizzes(it) }
        }
    }

    should("getQuizData excludes question without id, title or answer") {
        val (frCat1) = mockCategoryWithQuiz(repository, QuipoQuizSiteId.FR, 1) {
            add(
                createQuiz(
                    id = 1,
                    categoryId = it,
                    questionsTrueFalse = listOf(
                        createQuestionTrueOrFalse(id = 100),
                        // is ignored
                        createQuestionTrueOrFalse(id = null),
                        // is ignored
                        createQuestionTrueOrFalse(id = 102, title = null),
                        // is ignored
                        createQuestionTrueOrFalse(id = 103, answer = null),
                        createQuestionTrueOrFalse(id = 104, explanation = null)
                    ),
                    questionsMultipleChoice = listOf(
                        createQuestionMCQ(id = 110),
                        // is ignored
                        createQuestionMCQ(id = null),
                        // is ignored
                        createQuestionMCQ(id = 112, title = null),
                        // is ignored
                        createQuestionMCQ(id = 113, choices = emptyList()),
                        createQuestionMCQ(id = 114, explanation = null)
                    )
                )
            )
        }

        mockCategoryWithQuiz(repository, QuipoQuizSiteId.EN, 2) {
            add(
                createQuiz(
                    id = 2,
                    categoryId = it,
                    questionsTrueFalse = listOf(
                        // is ignored
                        createQuestionTrueOrFalse(id = null),
                        // is ignored
                        createQuestionTrueOrFalse(id = 200, title = null),
                        // is ignored
                        createQuestionTrueOrFalse(id = 201, answer = null)
                    ),
                    questionsMultipleChoice = listOf(
                        // is ignored
                        createQuestionMCQ(id = null),
                        // is ignored
                        createQuestionMCQ(id = 212, title = null),
                        // is ignored
                        createQuestionMCQ(id = 213, choices = emptyList())
                    )
                )
            )
        }

        service.getQuizData() shouldBe mapOf(
            Language.FRENCH to listOf(
                createBuiltCategory(
                    id = QuipoQuizCategoryId(frCat1.id!!),
                    quizzes = listOf(
                        createBuiltQuiz(
                            id = QuipoQuizQuizId("1"),
                            categoryId = QuipoQuizCategoryId("1"),
                            questionsTrueFalse = listOf(
                                createBuiltQuestionTrueFalse(QuipoQuizQuestionId("100"), QuipoQuizQuizId("1")),
                                createBuiltQuestionTrueFalse(
                                    QuipoQuizQuestionId("104"),
                                    QuipoQuizQuizId("1"),
                                    explanation = null
                                )
                            ),
                            questionsMultipleChoice = listOf(
                                createBuiltQuestionMCQ(QuipoQuizQuestionId("110"), QuipoQuizQuizId("1")),
                                createBuiltQuestionMCQ(
                                    QuipoQuizQuestionId("114"),
                                    QuipoQuizQuizId("1"),
                                    explanation = null
                                )
                            )
                        )
                    )
                )
            ),
            Language.ENGLISH to emptyList()
        )

        QuipoQuizSiteId.entries.forEach {
            verifySuspend(exactly(1)) { repository.getCategories(it) }
            verifySuspend(exactly(1)) { repository.getQuizzes(it) }
        }
    }

    should("getQuizData remove invalid character in question title and explanation") {
        val (frCat1) = mockCategoryWithQuiz(repository, QuipoQuizSiteId.FR, 1) {
            add(
                createQuiz(
                    id = 1,
                    categoryId = it,
                    questionsTrueFalse = listOf(
                        createQuestionTrueOrFalse(id = 100, title = "   title 1   "),
                        createQuestionTrueOrFalse(id = 101, title = "<p>  title <br>2   </p>"),
                        createQuestionTrueOrFalse(
                            id = 102,
                            title = "title 3",
                            explanation = "   <a>explanation 3</a>   "
                        )
                    ),
                    questionsMultipleChoice = listOf(
                        createQuestionMCQ(id = 110, title = "   title 4   "),
                        createQuestionMCQ(id = 111, title = "  <h1>title <p>5</p>   </h1>"),
                        createQuestionMCQ(
                            id = 112,
                            title = "title 6",
                            explanation = "   <x>explanation 6</x>   "
                        )
                    )
                )
            )
        }

        val (enCat2) = mockCategoryWithQuiz(repository, QuipoQuizSiteId.EN, 2) {
            add(
                createQuiz(
                    id = 2,
                    categoryId = it,
                    questionsTrueFalse = listOf(
                        createQuestionTrueOrFalse(id = 200, title = "   <a>title 4</a>   "),
                        createQuestionTrueOrFalse(id = 201, title = "  <h1>title <p>5</p>   </h1>"),
                        createQuestionTrueOrFalse(
                            id = 202,
                            title = "title 6",
                            explanation = "   <x>explanation <p>6</p><x>   "
                        )
                    ),
                    questionsMultipleChoice = listOf(
                        createQuestionMCQ(id = 210, title = "   title 7   "),
                        createQuestionMCQ(id = 211, title = "  <h1>title <p>8</p>   </h1>"),
                        createQuestionMCQ(
                            id = 212,
                            title = "title 9",
                            explanation = "   <x>explanation 9</x>   "
                        )
                    )
                )
            )
        }

        service.getQuizData() shouldBe mapOf(
            Language.FRENCH to listOf(
                createBuiltCategory(
                    id = QuipoQuizCategoryId(frCat1.id!!),
                    quizzes = listOf(
                        createBuiltQuiz(
                            id = QuipoQuizQuizId("1"),
                            categoryId = QuipoQuizCategoryId("1"),
                            questionsTrueFalse = listOf(
                                createBuiltQuestionTrueFalse(
                                    QuipoQuizQuestionId("100"),
                                    QuipoQuizQuizId("1"),
                                    title = "title 1"
                                ),
                                createBuiltQuestionTrueFalse(
                                    QuipoQuizQuestionId("101"),
                                    QuipoQuizQuizId("1"),
                                    title = "title 2"
                                ),
                                createBuiltQuestionTrueFalse(
                                    QuipoQuizQuestionId("102"),
                                    QuipoQuizQuizId("1"),
                                    title = "title 3",
                                    explanation = "explanation 3"
                                )
                            ),
                            questionsMultipleChoice = listOf(
                                createBuiltQuestionMCQ(
                                    QuipoQuizQuestionId("110"),
                                    QuipoQuizQuizId("1"),
                                    title = "title 4"
                                ),
                                createBuiltQuestionMCQ(
                                    QuipoQuizQuestionId("111"),
                                    QuipoQuizQuizId("1"),
                                    title = "title 5"
                                ),
                                createBuiltQuestionMCQ(
                                    QuipoQuizQuestionId("112"),
                                    QuipoQuizQuizId("1"),
                                    title = "title 6",
                                    explanation = "explanation 6"
                                )
                            )
                        )
                    )
                )
            ),
            Language.ENGLISH to listOf(
                createBuiltCategory(
                    id = QuipoQuizCategoryId(enCat2.id!!),
                    quizzes = listOf(
                        createBuiltQuiz(
                            id = QuipoQuizQuizId("2"),
                            categoryId = QuipoQuizCategoryId("2"),
                            questionsTrueFalse = listOf(
                                createBuiltQuestionTrueFalse(
                                    QuipoQuizQuestionId("200"),
                                    QuipoQuizQuizId("2"),
                                    title = "title 4"
                                ),
                                createBuiltQuestionTrueFalse(
                                    QuipoQuizQuestionId("201"),
                                    QuipoQuizQuizId("2"),
                                    title = "title 5"
                                ),
                                createBuiltQuestionTrueFalse(
                                    QuipoQuizQuestionId("202"),
                                    QuipoQuizQuizId("2"),
                                    title = "title 6",
                                    explanation = "explanation 6"
                                )
                            ),
                            questionsMultipleChoice = listOf(
                                createBuiltQuestionMCQ(
                                    QuipoQuizQuestionId("210"),
                                    QuipoQuizQuizId("2"),
                                    title = "title 7"
                                ),
                                createBuiltQuestionMCQ(
                                    QuipoQuizQuestionId("211"),
                                    QuipoQuizQuizId("2"),
                                    title = "title 8"
                                ),
                                createBuiltQuestionMCQ(
                                    QuipoQuizQuestionId("212"),
                                    QuipoQuizQuizId("2"),
                                    title = "title 9",
                                    explanation = "explanation 9"
                                )
                            )
                        )
                    )
                )
            )
        )

        QuipoQuizSiteId.entries.forEach {
            verifySuspend(exactly(1)) { repository.getCategories(it) }
            verifySuspend(exactly(1)) { repository.getQuizzes(it) }
        }
    }
})

private fun createBuiltCategory(
    id: QuipoQuizCategoryId,
    name: String = "title $id",
    image: String? = "url $id",
    icon: String? = "url $id",
    color: String = "color $id",
    quizzes: List<QuipoQuizQuiz>
) = QuipoQuizCategory(
    id = id,
    image = image,
    name = name,
    icon = icon,
    color = color,
    quizzes = quizzes
)

private fun createBuiltUnknownCategory(locale: Locale, color: String, quizzes: List<QuipoQuizQuiz>) = QuipoQuizCategory(
    id = QuipoQuizCategoryId.none,
    name = Messages.quipoquiz_category_none(locale = locale),
    image = null,
    icon = null,
    color = color,
    quizzes = quizzes
)

private fun createDetailedCategory(
    id: Long?,
    color: String? = "color $id",
    title: String? = "title $id",
    language: String? = "language $id",
    imageUrls: List<String> = listOf("url $id"),
    iconUrls: List<String> = listOf("url $id")
) = DetailedCategory(
    __typename = "",
    id = id?.toString(),
    color = color,
    title = title,
    language = language,
    image = imageUrls.map { DetailedCategory.Image(url = it) },
    icon = iconUrls.map { DetailedCategory.Icon(url = it) }
)

private fun createQuiz(
    id: Long?,
    categoryId: Long?,
    answerType: String? = "answer type $id",
    language: String? = "language $id",
    title: String? = "title $id",
    images: List<String> = listOf("url $id.2"),
    questionsTrueFalse: List<Quiz.Questions_true_or_false>,
    questionsMultipleChoice: List<Quiz.Questions_multiple_choice>
) = Quiz(
    __typename = "",
    id = id?.toString(),
    answer_type = answerType,
    language = language,
    title = title,
    quiz_category = listOf(Quiz.Quiz_category(id = categoryId?.toString())),
    quiz_image = images.map { Quiz.Quiz_image(url = it) },
    questions_true_or_false = questionsTrueFalse,
    questions_multiple_choices = questionsMultipleChoice
)

private fun createBuiltQuiz(
    id: QuipoQuizQuizId,
    categoryId: QuipoQuizCategoryId?,
    title: String = "title $id",
    questionsTrueFalse: List<QuipoQuizQuestionTrueFalse>,
    questionsMultipleChoice: List<QuipoQuizQuestionMCQ>
) = QuipoQuizQuiz(
    id = id,
    categoryId = categoryId,
    title = title,
    questionsTrueFalse = questionsTrueFalse,
    questionsMCQ = questionsMultipleChoice
)

private fun createBuiltQuiz(
    quiz: Quiz,
    id: QuipoQuizQuizId = QuipoQuizQuizId(quiz.id!!),
    categoryId: QuipoQuizCategoryId? = quiz.quiz_category.first()?.id?.let { QuipoQuizCategoryId(it) },
    title: String = quiz.title!!,
    questionsTrueFalse: List<QuipoQuizQuestionTrueFalse> = createBuiltQuestionsTrueFalse(
        quiz.questions_true_or_false,
        id
    ),
    questionsMultipleChoice: List<QuipoQuizQuestionMCQ> = createBuiltQuestionsMCQ(quiz.questions_multiple_choices, id)
): QuipoQuizQuiz {
    val id = QuipoQuizQuizId(quiz.id!!)
    return QuipoQuizQuiz(
        id = id,
        categoryId = categoryId,
        title = title,
        questionsTrueFalse = questionsTrueFalse,
        questionsMCQ = questionsMultipleChoice
    )
}

private fun createBuiltQuestionsTrueFalse(questions: List<Quiz.Questions_true_or_false?>, quizId: QuipoQuizQuizId) =
    questions.mapNotNull { q ->
        q?.questionTrueOrFalse?.let { createBuiltQuestionTrueFalse(it, quizId) }
    }

private fun createBuiltQuestionsMCQ(questions: List<Quiz.Questions_multiple_choice?>, quizId: QuipoQuizQuizId) =
    questions.mapNotNull { q ->
        q?.questionMultipleChoices?.let { createBuiltQuestionsMCQ(it, quizId) }
    }

private fun createQuestionTrueOrFalse(
    id: Long?,
    title: String? = "title $id",
    answer: Boolean? = true,
    explanation: String? = "answer explanation $id",
    images: List<String> = listOf("url $id")
) = Quiz.Questions_true_or_false(
    __typename = "",
    questionTrueOrFalse = QuestionTrueOrFalse(
        __typename = "",
        id = id?.toString(),
        title = title,
        answer = answer,
        anwser_explanation = explanation,
        image = images.map { QuestionTrueOrFalse.Image(url = it) }
    )
)

private fun createQuestionMCQ(
    id: Long?,
    title: String? = "title $id",
    explanation: String? = "answer explanation $id",
    images: List<String> = listOf("url $id"),
    choices: List<Pair<String, Boolean?>> = listOf(
        "choice 1" to true,
        "choice 2" to false,
        "choice 3" to false,
        "choice 4" to false
    )
) = Quiz.Questions_multiple_choice(
    __typename = "",
    questionMultipleChoices = QuestionMultipleChoices(
        __typename = "",
        id = id?.toString(),
        title = title,
        anwser_explanation = explanation,
        image = images.map { QuestionMultipleChoices.Image(url = it) },
        anwser_choices = choices.map {
            QuestionMultipleChoices.Anwser_choice(
                __typename = "",
                answerChoice = AnswerChoice(
                    __typename = "",
                    choice = it.first,
                    good_answer = it.second
                )
            )
        }
    )
)

private fun createBuiltQuestionTrueFalse(
    id: QuipoQuizQuestionId,
    quizId: QuipoQuizQuizId,
    image: String? = "url $id",
    title: String = "title $id",
    explanation: String? = "answer explanation $id",
    answer: Boolean = true
) = QuipoQuizQuestionTrueFalse(
    id = id,
    quizId = quizId,
    image = image,
    title = title,
    explanation = explanation,
    trueChoice = QuipoQuizAnswerChoiceTrue(goodAnswer = answer == true),
    falseChoice = QuipoQuizAnswerChoiceFalse(goodAnswer = answer == false)
)

private fun createBuiltQuestionTrueFalse(
    question: QuestionTrueOrFalse,
    quizId: QuipoQuizQuizId
): QuipoQuizQuestionTrueFalse {
    val answer = question.answer!!
    return QuipoQuizQuestionTrueFalse(
        id = QuipoQuizQuestionId(question.id!!),
        quizId = quizId,
        image = question.image.firstOrNull()?.url,
        title = question.title!!,
        explanation = question.anwser_explanation,
        trueChoice = QuipoQuizAnswerChoiceTrue(goodAnswer = answer == true),
        falseChoice = QuipoQuizAnswerChoiceFalse(goodAnswer = answer == false)
    )
}

private fun createBuiltQuestionMCQ(
    id: QuipoQuizQuestionId,
    quizId: QuipoQuizQuizId,
    image: String? = "url $id",
    title: String = "title $id",
    explanation: String? = "answer explanation $id",
    choices: Collection<QuipoQuizAnswerChoiceMCQ> = listOf(
        QuipoQuizAnswerChoiceMCQ(QuipoQuizAnswerChoiceId(0), "choice 1", true),
        QuipoQuizAnswerChoiceMCQ(QuipoQuizAnswerChoiceId(1), "choice 2", false),
        QuipoQuizAnswerChoiceMCQ(QuipoQuizAnswerChoiceId(2), "choice 3", false),
        QuipoQuizAnswerChoiceMCQ(QuipoQuizAnswerChoiceId(3), "choice 4", false)
    )
) = QuipoQuizQuestionMCQ(
    id = id,
    quizId = quizId,
    image = image,
    title = title,
    explanation = explanation,
    choices = choices
)

private fun createBuiltQuestionsMCQ(question: QuestionMultipleChoices, quizId: QuipoQuizQuizId): QuipoQuizQuestionMCQ {
    return QuipoQuizQuestionMCQ(
        id = QuipoQuizQuestionId(question.id!!),
        quizId = quizId,
        image = question.image.firstOrNull()?.url,
        title = question.title!!,
        explanation = question.anwser_explanation,
        choices = question.anwser_choices!!.mapIndexed { index, it ->
            val answerChoice = it!!.answerChoice
            QuipoQuizAnswerChoiceMCQ(
                id = QuipoQuizAnswerChoiceId(index),
                choice = answerChoice.choice!!,
                goodAnswer = answerChoice.good_answer!!
            )
        }
    )
}

private inline fun mockGetCategories(
    repository: QuipoQuizRepository,
    siteId: QuipoQuizSiteId,
    crossinline emitter: suspend FlowCollector<DetailedCategory>.() -> Unit
) {
    everySuspend { repository.getCategories(siteId) } returns flow {
        emitter()
    }
}

private inline fun mockGetQuizzes(
    repository: QuipoQuizRepository,
    siteId: QuipoQuizSiteId,
    crossinline builderAction: suspend FlowCollector<Quiz>.() -> Unit
) {
    everySuspend { repository.getQuizzes(siteId) } returns flow {
        builderAction()
    }
}

private inline fun mockCategoryWithQuiz(
    repository: QuipoQuizRepository,
    siteId: QuipoQuizSiteId,
    categoryId: Long,
    crossinline quizBuilder: MutableList<Quiz>.(Long) -> Unit
): Pair<DetailedCategory, List<Quiz>> {
    val category = createDetailedCategory(id = categoryId)
    everySuspend { repository.getCategories(siteId) } returns flow { emit(category) }

    val quizzes = buildList { quizBuilder(categoryId) }
    mockGetQuizzes(repository, siteId) { quizzes.forEach { emit(it) } }

    return category to quizzes
}
