package io.github.hansanto.quipoquiz.discord.framework.extension

import dev.kord.common.Color
import dev.kord.core.behavior.interaction.ActionInteractionBehavior
import dev.kord.core.behavior.interaction.respondEphemeral
import dev.kord.core.behavior.interaction.response.DeferredMessageInteractionResponseBehavior
import dev.kord.core.behavior.interaction.response.MessageInteractionResponseBehavior
import dev.kord.core.behavior.interaction.response.edit
import dev.kord.core.behavior.interaction.response.respond
import dev.kord.rest.builder.message.EmbedBuilder
import dev.kord.rest.builder.message.create.InteractionResponseCreateBuilder
import dev.kord.rest.builder.message.embed
import dev.kord.rest.builder.message.modify.InteractionResponseModifyBuilder
import dev.kord.x.emoji.Emojis

/**
 * Build the embed with an error message.
 * @param message Error message.
 * @receiver Embed builder.
 */
private fun EmbedBuilder.error(message: String) {
    description = "${Emojis.x} $message"
    color = Color(255, 0, 0)
}

/**
 * Respond with an ephemeral error message.
 * @param message Error message.
 */
suspend fun DeferredMessageInteractionResponseBehavior.respondError(message: String) {
    respond {
        sendError(message)
    }
}

/**
 * Respond with an ephemeral error message.
 * @param message Error message.
 * @receiver Interaction response builder.
 */
suspend fun MessageInteractionResponseBehavior.editError(message: String) {
    edit {
        reset()
        sendError(message)
    }
}

/**
 * Respond with an ephemeral error message.
 * @param message Error message.
 * @receiver Interaction response builder.
 */
suspend fun ActionInteractionBehavior.respondEphemeralError(message: String) {
    respondEphemeral {
        sendError(message)
    }
}

/**
 * Send an error message.
 * @param message Error message.
 * @receiver Interaction response builder.
 */
private fun InteractionResponseModifyBuilder.sendError(message: String) {
    embed { error(message) }
    suppressEmbeds = false
}

/**
 * Send an error message.
 * @param message Error message.
 * @receiver Interaction response builder.
 */
private fun InteractionResponseCreateBuilder.sendError(message: String) {
    embed { error(message) }
    suppressEmbeds = false
}

/**
 * Remove all the content of the interaction response.
 * If the embeds are deleted, the display of the embeds will be restored.
 * @receiver Interaction response builder.
 */
fun InteractionResponseModifyBuilder.reset() {
    content = null
    embeds = mutableListOf()
    flags = null
    suppressEmbeds = false
    allowedMentions = null
    components = mutableListOf()
    files.clear()
    attachments = mutableListOf()
}
