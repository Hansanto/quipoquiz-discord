package io.github.hansanto.quipoquiz.discord.command.ping

import de.comahe.i18n4k.messages.MessageBundleLocalizedString
import dev.kord.core.Kord
import dev.kord.core.behavior.interaction.response.respond
import dev.kord.core.event.interaction.ChatInputCommandInteractionCreateEvent
import io.github.hansanto.generated.i18n.Messages
import io.github.hansanto.quipoquiz.discord.framework.command.ChatInputCommand
import io.github.hansanto.quipoquiz.discord.framework.command.CommandArgument

class PingCommand(private val kord: Kord) : ChatInputCommand {

    override val bundleName: MessageBundleLocalizedString
        get() = Messages.command_ping_name

    override val bundleDescription: MessageBundleLocalizedString
        get() = Messages.command_ping_description

    override val arguments: Collection<CommandArgument<*>>
        get() = emptyList()

    override suspend fun execute(event: ChatInputCommandInteractionCreateEvent) {
        event.interaction.deferEphemeralResponse().respond {
            val delay = kord.gateway.averagePing
            val delayString = delay?.inWholeMilliseconds?.toString() ?: "???"
            content = "Pong! ($delayString ms)"
        }
    }
}
