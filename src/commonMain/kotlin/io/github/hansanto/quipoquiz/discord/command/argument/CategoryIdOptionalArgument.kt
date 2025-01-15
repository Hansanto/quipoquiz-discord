package io.github.hansanto.quipoquiz.discord.command.argument

import de.comahe.i18n4k.messages.MessageBundleLocalizedString
import dev.kord.core.behavior.interaction.suggestString
import dev.kord.core.entity.interaction.InteractionCommand
import dev.kord.core.event.interaction.AutoCompleteInteractionCreateEvent
import dev.kord.rest.builder.interaction.BaseInputChatBuilder
import dev.kord.rest.builder.interaction.string
import io.github.hansanto.generated.i18n.Messages
import io.github.hansanto.quipoquiz.Language
import io.github.hansanto.quipoquiz.config.CommandConfiguration
import io.github.hansanto.quipoquiz.discord.framework.command.CommandArgument
import io.github.hansanto.quipoquiz.quipoquiz.QuipoQuizCategoryId
import io.github.hansanto.quipoquiz.quipoquiz.QuipoQuizService
import io.github.hansanto.quipoquiz.withLocale

class CategoryIdOptionalArgument(
    private val service: QuipoQuizService,
    private val languageOptionalArgument: LanguageOptionalArgument
) : CommandArgument<QuipoQuizCategoryId?> {

    override val autocomplete: Boolean
        get() = true

    override val bundleName: MessageBundleLocalizedString
        get() = Messages.argument_category_name

    override val bundleDescription: MessageBundleLocalizedString
        get() = Messages.argument_category_description

    override fun register(builder: BaseInputChatBuilder) {
        builder.string(
            name = rootName(),
            description = rootDescription()
        ) {
            Language.withLocale { i18nLocale, kordLocale ->
                name(kordLocale, Messages.argument_category_name(i18nLocale))
                description(kordLocale, Messages.argument_category_description(i18nLocale))
            }

            required = false
            autocomplete = true
        }
    }

    override fun from(command: InteractionCommand): QuipoQuizCategoryId? {
        return command.strings[rootName()]?.let { QuipoQuizCategoryId(it) }
    }

    override suspend fun autoComplete(event: AutoCompleteInteractionCreateEvent): Boolean {
        val interaction = event.interaction
        val userValue = interaction.focusedOption.value

        val language = languageOptionalArgument.from(interaction.command)
        val categories = service.getQuizData()[language]
            ?.filter { it.name.contains(userValue, ignoreCase = true) }
            ?.sortedByDescending { it.name.startsWith(userValue, ignoreCase = true) }
            ?.take(CommandConfiguration.maximumSizeAutoComplete)
            ?: emptyList()

        interaction.suggestString {
            categories.forEach {
                choice(it.name, it.id.value)
            }
        }

        return true
    }
}
