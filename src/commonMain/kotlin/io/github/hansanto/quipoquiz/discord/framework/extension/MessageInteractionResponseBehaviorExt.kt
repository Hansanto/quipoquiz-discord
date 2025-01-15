package io.github.hansanto.quipoquiz.discord.framework.extension

import dev.kord.common.entity.Snowflake
import dev.kord.core.Kord
import dev.kord.core.behavior.interaction.response.DeferredEphemeralMessageInteractionResponseBehavior
import dev.kord.core.behavior.interaction.response.DeferredMessageInteractionResponseBehavior
import dev.kord.core.behavior.interaction.response.DeferredPublicMessageInteractionResponseBehavior
import dev.kord.core.behavior.interaction.response.EphemeralMessageInteractionResponseBehavior
import dev.kord.core.behavior.interaction.response.MessageInteractionResponseBehavior
import dev.kord.core.behavior.interaction.response.PublicMessageInteractionResponseBehavior
import dev.kord.core.supplier.EntitySupplier

/**
 * Returns true if the response is public, false otherwise.
 */
val DeferredMessageInteractionResponseBehavior.isPublic: Boolean
    get() = when (this) {
        is DeferredPublicMessageInteractionResponseBehavior -> true
        is DeferredEphemeralMessageInteractionResponseBehavior -> false
        else -> throw IllegalArgumentException("Unable to determine if the response is public with $this")
    }

/**
 * Returns true if the response is public, false otherwise.
 */
val MessageInteractionResponseBehavior.isPublic: Boolean
    get() = when (this) {
        is PublicMessageInteractionResponseBehavior -> true
        is EphemeralMessageInteractionResponseBehavior -> false
        else -> throw IllegalArgumentException("Unable to determine if the response is public with $this")
    }

/**
 * Converts a [DeferredMessageInteractionResponseBehavior] to a [MessageInteractionResponseBehavior].
 * @return The instance of [MessageInteractionResponseBehavior].
 */
@Suppress("ktlint:standard:max-line-length")
fun DeferredMessageInteractionResponseBehavior.toMessageInteractionResponseBehavior(): MessageInteractionResponseBehavior =
    if (isPublic) {
        object : PublicMessageInteractionResponseBehavior {
            override val applicationId: Snowflake get() = this@toMessageInteractionResponseBehavior.applicationId
            override val kord: Kord get() = this@toMessageInteractionResponseBehavior.kord
            override val supplier: EntitySupplier get() = this@toMessageInteractionResponseBehavior.supplier
            override val token: String get() = this@toMessageInteractionResponseBehavior.token
        }
    } else {
        object : EphemeralMessageInteractionResponseBehavior {
            override val applicationId: Snowflake get() = this@toMessageInteractionResponseBehavior.applicationId
            override val kord: Kord get() = this@toMessageInteractionResponseBehavior.kord
            override val supplier: EntitySupplier get() = this@toMessageInteractionResponseBehavior.supplier
            override val token: String get() = this@toMessageInteractionResponseBehavior.token
        }
    }
