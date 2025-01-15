package io.github.hansanto.quipoquiz.discord.framework.component.container.exception.handler

import dev.kord.core.Kord
import dev.kord.core.behavior.interaction.response.DeferredMessageInteractionResponseBehavior
import dev.kord.core.behavior.interaction.response.MessageInteractionResponseBehavior
import io.github.hansanto.generated.i18n.Messages
import io.github.hansanto.quipoquiz.Language
import io.github.hansanto.quipoquiz.discord.framework.component.container.Container
import io.github.hansanto.quipoquiz.discord.framework.exception.DiscordException
import io.github.hansanto.quipoquiz.discord.framework.extension.editError
import io.github.hansanto.quipoquiz.discord.framework.extension.respondError
import kotlinx.coroutines.launch

/**
 * If the exception is an instance of [DiscordException], return it.
 * Otherwise, return a new instance of [DiscordException] with basic information to avoid exposing internal errors.
 * @param exception Exception to check.
 * @param language Language to create the default exception.
 * @return [DiscordException] instance.
 */
private fun getDiscordExceptionOrDefault(exception: Exception, language: Language): DiscordException {
    return when (exception) {
        is DiscordException -> exception
        else -> DiscordException(Messages.error_update(language.i18nLocale))
    }
}

/**
 * Chain two response error handlers.
 * @receiver The first response error handler.
 * @param second The second response error handler.
 */
fun CreationResponseErrorHandler.chainWith(second: CreationResponseErrorHandler): ChainCreationResponseErrorHandler {
    return ChainCreationResponseErrorHandler(this, second)
}

/**
 * Interface to handle an exception to send information to the user.
 */
interface CreationResponseErrorHandler {

    /**
     * Send the problem information to the user.
     * @param response Response that will be used to send the information.
     * @param exception Exception to retrieve the error information.
     */
    suspend fun handle(response: DeferredMessageInteractionResponseBehavior, exception: Exception)

    /**
     * Send the problem information to the user.
     * @param response Response that will be used to send the information.
     * @param exception Exception to retrieve the error information.
     */
    suspend fun handle(response: MessageInteractionResponseBehavior, exception: Exception)
}

/**
 * Chain two response error handlers.
 * @receiver The first response error handler.
 * @param second The second response error handler.
 */
class ChainCreationResponseErrorHandler(
    val first: CreationResponseErrorHandler,
    val second: CreationResponseErrorHandler
) : CreationResponseErrorHandler {

    override suspend fun handle(response: DeferredMessageInteractionResponseBehavior, exception: Exception) {
        first.handle(response, exception)
        second.handle(response, exception)
    }

    override suspend fun handle(response: MessageInteractionResponseBehavior, exception: Exception) {
        first.handle(response, exception)
        second.handle(response, exception)
    }
}

/**
 * Implementation of [CreationResponseErrorHandler] to display the error message to the user.
 */
class DisplayReasonCreationResponseErrorHandler(val language: Language) : CreationResponseErrorHandler {

    override suspend fun handle(response: DeferredMessageInteractionResponseBehavior, exception: Exception) {
        val discordException = getDiscordExceptionOrDefault(exception, language)
        response.respondError(discordException.reason)
    }

    override suspend fun handle(response: MessageInteractionResponseBehavior, exception: Exception) {
        val discordException = getDiscordExceptionOrDefault(exception, language)
        response.editError(discordException.reason)
    }
}

/**
 * Implementation of [CreationResponseErrorHandler] to cancel the container when an error occurs.
 */
class ContainerShutdownCreationResponseErrorHandler(
    val kord: Kord,
    val container: Container,
    val language: Language
) : CreationResponseErrorHandler {

    override suspend fun handle(response: DeferredMessageInteractionResponseBehavior, exception: Exception) {
        val discordException = getDiscordExceptionOrDefault(exception, language)
        kord.launch {
            container.cancel(discordException)
        }
    }

    override suspend fun handle(response: MessageInteractionResponseBehavior, exception: Exception) {
        val discordException = getDiscordExceptionOrDefault(exception, language)
        kord.launch {
            container.cancel(discordException)
        }
    }
}
