package io.github.hansanto.quipoquiz.discord.framework.command

import de.comahe.i18n4k.messages.MessageBundleLocalizedString
import dev.kord.core.entity.interaction.InteractionCommand
import dev.kord.core.event.interaction.AutoCompleteInteractionCreateEvent
import dev.kord.rest.builder.interaction.BaseInputChatBuilder
import io.github.hansanto.quipoquiz.config.BotConfiguration

/**
 * Represents a command argument.
 * @param T Type of the argument.
 */
interface CommandArgument<T> {

    /**
     * Name of the argument not localized.
     * With this variable, we can get the root name of the command for each language.
     */
    val bundleName: MessageBundleLocalizedString

    /**
     * Description of the argument not localized.
     * With this variable, we can get the root description of the command for each language.
     */
    val bundleDescription: MessageBundleLocalizedString

    /**
     * `true` if the argument should be autocompleted.
     */
    val autocomplete: Boolean
        get() = false

    /**
     * Create an option for the command argument.
     * @param builder The option builder.
     */
    fun register(builder: BaseInputChatBuilder)

    /**
     * Name of the command in the root language.
     * @return The name of the command in the root language.
     */
    fun rootName(): String {
        return bundleName(BotConfiguration.defaultLanguage.i18nLocale)
    }

    /**
     * Description of the command in the root language.
     * @return The description of the command in the root language.
     */
    fun rootDescription(): String {
        return bundleDescription(BotConfiguration.defaultLanguage.i18nLocale)
    }

    /**
     * Get the argument from the command.
     * @param command The command received.
     * @return The argument value.
     */
    fun from(command: InteractionCommand): T

    /**
     * Apply the autocompletion logic for the argument.
     * @param event The autocompletion event.
     * @return `true` if the suggestion was applied.
     */
    suspend fun autoComplete(event: AutoCompleteInteractionCreateEvent): Boolean = false
}
