package io.github.hansanto.quipoquiz.config

import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.network.http.HttpNetworkTransport
import com.apollographql.ktor.http.KtorHttpEngine
import io.github.hansanto.quipoquiz.extension.resolve
import io.github.hansanto.quipoquiz.util.environment.env
import kotlinx.io.files.Path
import kotlinx.io.files.SystemTemporaryDirectory
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.seconds

/**
 * Configuration about QuipoQuiz components.
 */
object QuipoQuizConfiguration {

    /**
     * The default page size for the API requests.
     * This allows avoiding overloading the server with one big request.
     */
    val pageSize: Int by env(
        key = "QUIPOQUIZ_REQUEST_PAGE_SIZE",
        converter = String::toInt,
        defaultValue = { 10 }
    ) {
        require(it > 0) { "The page size must be greater than 0" }
    }

    /**
     * The default color of the category.
     * When a category does not have a color, this color will be used.
     */
    val defaultCategoryColor: String by env(
        key = "QUIPOQUIZ_DEFAULT_CATEGORY_COLOR",
        converter = { it },
        defaultValue = { "#2A75FF" }
    )

    /**
     * The expiration interval of the cache.
     * By default, the cache will expire after 1 day.
     */
    val cacheExpiration: Duration by env(
        key = "QUIPOQUIZ_CACHE_EXPIRATION",
        converter = { it.toLong().seconds },
        defaultValue = { 1.days }
    ) {
        require(it > 0.seconds) { "The cache expiration must be greater than 0 seconds" }
    }

    /**
     * The directory where the cache will be stored.
     * By default, the directory will be "quipoquiz" in the temporary directory of the system.
     */
    val cacheDirectory: Path by env(
        key = "QUIPOQUIZ_CACHE_DIRECTORY",
        converter = ::Path,
        defaultValue = { SystemTemporaryDirectory.resolve("quipoquiz") }
    )

    /**
     * `true` if quizzes without category should be included in the data, `false` otherwise.
     * Default is `false`.
     */
    val includeQuizWithoutCategory: Boolean by env(
        key = "QUIPOQUIZ_INCLUDE_QUIZ_WITHOUT_CATEGORY",
        converter = { it.toBoolean() },
        defaultValue = { false }
    )

    /**
     * The URL of the QuipoQuiz API.
     */
    private val url: String by env(
        key = "QUIPOQUIZ_URL",
        converter = { it },
        defaultValue = { "https://cms.quipoquiz.com/api" }
    )

    /**
     * The token to authenticate with QuipoQuiz API.
     */
    private val token: String by env(
        key = "QUIPOQUIZ_TOKEN",
        converter = { it }
    )

    /**
     * Create a GraphQL client to interact with QuipoQuiz API.
     * @param url URL of the API. By default, it will use the URL from the configuration.
     * @param token Token to authenticate with the API. By default, it will use the token from the configuration.
     * @return Client.
     */
    fun createClient(url: String = this.url, token: String = this.token): ApolloClient {
        return ApolloClient.Builder()
            .addHttpHeader("Authorization", "Bearer $token")
            .networkTransport(
                HttpNetworkTransport.Builder()
                    .serverUrl(url)
                    .httpEngine(KtorHttpEngine())
                    .build()
            ).build()
    }
}
