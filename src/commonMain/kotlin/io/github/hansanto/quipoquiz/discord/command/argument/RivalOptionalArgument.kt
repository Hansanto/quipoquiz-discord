package io.github.hansanto.quipoquiz.discord.command.argument

import de.comahe.i18n4k.messages.MessageBundleLocalizedString
import dev.kord.core.entity.User
import dev.kord.core.entity.interaction.InteractionCommand
import dev.kord.rest.builder.interaction.BaseInputChatBuilder
import dev.kord.rest.builder.interaction.user
import io.github.hansanto.generated.i18n.Messages
import io.github.hansanto.quipoquiz.Language
import io.github.hansanto.quipoquiz.discord.framework.command.CommandArgument
import io.github.hansanto.quipoquiz.withLocale

class RivalOptionalArgument : CommandArgument<User?> {

    override val bundleName: MessageBundleLocalizedString
        get() = Messages.argument_rival_name

    override val bundleDescription: MessageBundleLocalizedString
        get() = Messages.argument_rival_description

    override fun register(builder: BaseInputChatBuilder) {
        builder.user(
            name = rootName(),
            description = rootDescription()
        ) {
            Language.withLocale { i18nLocale, kordLocale ->
                name(kordLocale, bundleName(i18nLocale))
                description(kordLocale, bundleDescription(i18nLocale))
            }

            required = false
        }
    }

    override fun from(command: InteractionCommand): User? {
        return command.users[rootName()]
    }
}
