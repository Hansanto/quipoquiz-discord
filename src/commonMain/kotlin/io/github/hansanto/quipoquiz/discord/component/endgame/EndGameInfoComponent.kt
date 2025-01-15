package io.github.hansanto.quipoquiz.discord.component.endgame

import io.github.hansanto.generated.i18n.Messages
import io.github.hansanto.quipoquiz.Language
import io.github.hansanto.quipoquiz.config.GameConfiguration
import io.github.hansanto.quipoquiz.discord.extension.I18nLocale
import io.github.hansanto.quipoquiz.discord.framework.builder.message.CustomEmbedBuilder
import io.github.hansanto.quipoquiz.discord.framework.component.EmbedComponent
import io.github.hansanto.quipoquiz.discord.framework.component.container.exception.AliveTimeoutException
import io.github.hansanto.quipoquiz.discord.framework.component.container.exception.IdleTimeoutException
import kotlinx.coroutines.CancellationException

class EndGameInfoComponent(
    override val id: String,
    private val language: Language,
    private val cause: CancellationException? = null
) : EmbedComponent {

    override suspend fun render(builder: CustomEmbedBuilder) {
        val locale = language.i18nLocale
        builder.description = buildString {
            getErrorMessage(locale)?.let {
                appendLine(it)
                append('\n')
            }
            appendLine(Messages.game_end_thanks(locale))
            appendLine(Messages.quipoquiz_find_quizzes(locale))
        }

        builder.color = GameConfiguration.defaultColor
    }

    private fun getErrorMessage(locale: I18nLocale): String? {
        return when (cause) {
            is IdleTimeoutException -> Messages.game_end_timeout_idle(locale)
            is AliveTimeoutException -> Messages.game_end_timeout_alive(locale)
            else -> null
        }
    }
}
