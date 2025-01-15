package io.github.hansanto.quipoquiz.discord.framework.extension

import dev.kord.core.behavior.interaction.ActionInteractionBehavior
import dev.kord.core.behavior.interaction.ComponentInteractionBehavior

/**
 * Respond with an ephemeral error message.
 * @param message Error message.
 * @receiver Component interaction behavior.
 */
suspend fun ComponentInteractionBehavior.ephemeralError(message: String) {
    deferEphemeralMessageUpdate().editError(message)
}

/**
 * Respond with an ephemeral error message.
 * @param message Error message.
 * @receiver Interaction response builder.
 */
suspend fun ActionInteractionBehavior.ephemeralError(message: String) {
    deferEphemeralResponse().respondError(message)
}
