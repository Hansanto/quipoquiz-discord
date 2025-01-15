package io.github.hansanto.quipoquiz.discord.command.argument

import de.comahe.i18n4k.messages.MessageBundleLocalizedString
import dev.kord.core.entity.interaction.InteractionCommand
import dev.kord.rest.builder.interaction.BaseInputChatBuilder
import dev.kord.rest.builder.interaction.integer
import io.github.hansanto.generated.i18n.Messages
import io.github.hansanto.quipoquiz.Language
import io.github.hansanto.quipoquiz.config.CommandConfiguration
import io.github.hansanto.quipoquiz.discord.framework.command.CommandArgument
import io.github.hansanto.quipoquiz.withLocale

class NumberOfLifeOptionalArgument : CommandArgument<Int?> {

    override val bundleName: MessageBundleLocalizedString
        get() = Messages.argument_life_name

    override val bundleDescription: MessageBundleLocalizedString
        get() = Messages.argument_life_description

    override fun register(builder: BaseInputChatBuilder) {
        builder.integer(
            name = rootName(),
            description = rootDescription()
        ) {
            Language.withLocale { i18nLocale, kordLocale ->
                name(kordLocale, bundleName(i18nLocale))
                description(kordLocale, bundleDescription(i18nLocale))
            }

            required = false
            this.minValue = CommandConfiguration.LifeArgument.minimum.toLong()
            this.maxValue = CommandConfiguration.LifeArgument.maximum.toLong()
        }
    }

    override fun from(command: InteractionCommand): Int? {
        return command.integers[rootName()]?.toInt()
    }
}
