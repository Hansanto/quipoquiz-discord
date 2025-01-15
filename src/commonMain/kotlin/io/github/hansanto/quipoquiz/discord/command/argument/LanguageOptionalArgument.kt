package io.github.hansanto.quipoquiz.discord.command.argument

import de.comahe.i18n4k.messages.MessageBundleLocalizedString
import dev.kord.core.entity.interaction.InteractionCommand
import dev.kord.rest.builder.interaction.BaseInputChatBuilder
import dev.kord.rest.builder.interaction.string
import io.github.hansanto.generated.i18n.Messages
import io.github.hansanto.quipoquiz.Language
import io.github.hansanto.quipoquiz.Language.ENGLISH
import io.github.hansanto.quipoquiz.Language.FRENCH
import io.github.hansanto.quipoquiz.config.BotConfiguration
import io.github.hansanto.quipoquiz.discord.framework.command.CommandArgument
import io.github.hansanto.quipoquiz.withLocale

class LanguageOptionalArgument : CommandArgument<Language?> {

    override val bundleName: MessageBundleLocalizedString
        get() = Messages.argument_language_name

    override val bundleDescription: MessageBundleLocalizedString
        get() = Messages.argument_language_description

    override fun register(builder: BaseInputChatBuilder) {
        builder.string(
            name = rootName(),
            description = rootDescription()
        ) {
            Language.withLocale { i18nLocale, kordLocale ->
                name(kordLocale, bundleName(i18nLocale))
                description(kordLocale, bundleDescription(i18nLocale))
            }

            val defaultLanguageLocale = BotConfiguration.defaultLanguage.i18nLocale
            choice(Messages.language_french(defaultLanguageLocale), FRENCH.ordinal.toString()) {
                Language.withLocale { i18nLocale, kordLocale ->
                    name(kordLocale, Messages.language_french(i18nLocale))
                }
            }

            choice(Messages.language_english(defaultLanguageLocale), ENGLISH.ordinal.toString()) {
                Language.withLocale { i18nLocale, kordLocale ->
                    name(kordLocale, Messages.language_english(i18nLocale))
                }
            }

            required = false
        }
    }

    override fun from(command: InteractionCommand): Language? {
        return command.strings[rootName()]?.let {
            val ordinal = it.toInt()
            Language.entries.getOrNull(ordinal)
        }
    }
}
