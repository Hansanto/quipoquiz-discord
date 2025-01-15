package io.github.hansanto.quipoquiz.discord.framework.command

import de.comahe.i18n4k.messages.MessageBundleLocalizedString
import dev.kord.core.event.interaction.ApplicationCommandInteractionCreateEvent
import dev.kord.core.event.interaction.ChatInputCommandInteractionCreateEvent
import dev.kord.core.event.interaction.MessageCommandInteractionCreateEvent
import dev.kord.core.event.interaction.UserCommandInteractionCreateEvent
import io.github.hansanto.quipoquiz.config.BotConfiguration

/**
 * Represents a command.
 */
sealed interface Command<E : ApplicationCommandInteractionCreateEvent> {

    /**
     * Name of the command not localized.
     * With this variable, we can get the root name of the command for each language.
     */
    val bundleName: MessageBundleLocalizedString

    /**
     * Name of the command in the root language.
     * @return The name of the command in the root language.
     */
    fun rootName(): String {
        return bundleName(BotConfiguration.defaultLanguage.i18nLocale)
    }

    /**
     * Execute the command.
     * @param event The event that triggered the command.
     */
    suspend fun execute(event: E)
}

/**
 * Represents a command executable through the chat.
 */
interface ChatInputCommand : Command<ChatInputCommandInteractionCreateEvent> {

    /**
     * Arguments of the command.
     */
    val arguments: Collection<CommandArgument<*>>

    /**
     * Description of the command not localized.
     * With this variable, we can get the root description of the command for each language.
     */
    val bundleDescription: MessageBundleLocalizedString

    /**
     * Description of the command in the root language.
     * @return The description of the command in the root language.
     */
    fun rootDescription(): String {
        return bundleDescription(BotConfiguration.defaultLanguage.i18nLocale)
    }
}

/**
 * Represents a command executable by clicking on a user.
 */
interface UserCommand : Command<UserCommandInteractionCreateEvent>

/**
 * Represents a command executable by clicking on a message.
 */
interface MessageCommand : Command<MessageCommandInteractionCreateEvent>
