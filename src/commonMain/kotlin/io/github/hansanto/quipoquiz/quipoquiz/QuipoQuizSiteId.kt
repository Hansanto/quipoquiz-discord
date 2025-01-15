package io.github.hansanto.quipoquiz.quipoquiz

import kotlinx.serialization.Serializable

/**
 * Enum to represent the site id of QuipoQuiz.
 * A site id is a unique identifier for a language.
 */
@Serializable
enum class QuipoQuizSiteId(
    /**
     * The unique identifier for the site.
     */
    val id: String
) {
    FR("1"),
    EN("2")
}
