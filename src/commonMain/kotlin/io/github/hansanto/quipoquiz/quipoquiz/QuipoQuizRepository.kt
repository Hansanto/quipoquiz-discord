package io.github.hansanto.quipoquiz.quipoquiz

import com.apollographql.apollo.ApolloClient
import io.github.hansanto.generated.graphql.CountQuizQuery
import io.github.hansanto.generated.graphql.GetCategoriesQuery
import io.github.hansanto.generated.graphql.GetQuizzesQuery
import io.github.hansanto.generated.graphql.fragment.DetailedCategory
import io.github.hansanto.generated.graphql.fragment.Quiz
import io.github.hansanto.quipoquiz.config.QuipoQuizConfiguration
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.flow.DEFAULT_CONCURRENCY
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.flatMapMerge
import kotlinx.coroutines.flow.flow
import kotlin.math.ceil

private val log = KotlinLogging.logger {}

interface QuipoQuizRepository {
    /**
     * Get the list of categories available in QuipoQuiz.
     *
     * This method is not cached and will request the data from the server.
     *
     * @param siteId Language to retrieve the categories.
     * @return The list of categories.
     */
    fun getCategories(siteId: QuipoQuizSiteId): Flow<DetailedCategory>

    /**
     * Get the list of quizzes available in QuipoQuiz for a specific language.
     *
     * This method is not cached and will request the data from the server.
     *
     * @param siteId Language to retrieve the quizzes.
     * @return The list of quizzes.
     */
    fun getQuizzes(siteId: QuipoQuizSiteId): Flow<Quiz>

    /**
     * Get the number of quizzes available for a specific language.
     * @param siteId Language to retrieve the quizzes.
     * @return The number of quizzes.
     */
    suspend fun getCountQuiz(siteId: QuipoQuizSiteId): Int
}

/**
 * Repository to interact with QuipoQuiz API.
 */
class QuipoQuizRepositoryImpl(
    /**
     * Client to interact with the API.
     */
    private val client: ApolloClient,
    /**
     * Size of the page to retrieve.
     */
    private val pageSize: Int = QuipoQuizConfiguration.pageSize
) : QuipoQuizRepository {

    override fun getCategories(siteId: QuipoQuizSiteId): Flow<DetailedCategory> {
        return flow {
            client.query(GetCategoriesQuery(siteId = listOf(siteId.id))).execute()
                .data
                ?.categoriesEntries
                ?.asSequence()
                ?.mapNotNull { it?.detailedCategory }
                ?.distinctBy { it.id }
                ?.forEach { emit(it) }
                ?: error("Unable to retrieve categories for site [$siteId]")
        }
    }

    override fun getQuizzes(siteId: QuipoQuizSiteId): Flow<Quiz> {
        return pagination(
            pageSize = pageSize,
            getCount = { getCountQuiz(siteId) },
            getPage = { getPageQuiz(siteId, it) }
        ).distinctUntilChangedBy { it.id }
    }

    override suspend fun getCountQuiz(siteId: QuipoQuizSiteId): Int {
        return client.query(CountQuizQuery(siteId = listOf(siteId.id))).execute()
            .data
            ?.entryCount
            ?: error("Unable to retrieve the number of quiz for siteId [$siteId]")
    }

    /**
     * Get the list of quizzes available in QuipoQuiz for a specific language and page.
     * @param siteId Language to retrieve the quizzes.
     * @param page Number of the page to retrieve.
     * @return The list of quizzes.
     */
    private fun getPageQuiz(siteId: QuipoQuizSiteId, page: Int): Flow<Quiz> {
        return flow {
            log.info { "Requesting page [$page] for siteId [$siteId]" }
            client.query(
                GetQuizzesQuery(
                    siteId = listOf(siteId.id),
                    limit = pageSize,
                    offset = pageSize * page
                )
            ).execute()
                .data
                ?.quizEntries
                ?.asSequence()
                ?.mapNotNull { it?.quiz }
                ?.forEach { emit(it) }
                ?: error("Unable to retrieve quizzes for siteId [$siteId] page [$page] with page size [$pageSize]")
        }
    }

    /**
     * Function to retrieve all elements at the same time using pagination.
     * @param pageSize Size of the page to retrieve.
     * @param getCount Get the total number of elements.
     * @param getPage Get the elements of a specific page.
     * @param concurrency Number of pages to retrieve at the same time.
     * @return A flow of all elements from all pages.
     */
    private inline fun <T> pagination(
        pageSize: Int,
        crossinline getCount: suspend () -> Int,
        crossinline getPage: suspend (Int) -> Flow<T>,
        concurrency: Int = DEFAULT_CONCURRENCY
    ): Flow<T> {
        return flow {
            val count = getCount()
            val numberOfPage = ceil(count.toDouble() / pageSize).toInt()
            repeat(numberOfPage) { emit(it) }
        }.flatMapMerge(concurrency = concurrency) { getPage(it) }
    }
}
