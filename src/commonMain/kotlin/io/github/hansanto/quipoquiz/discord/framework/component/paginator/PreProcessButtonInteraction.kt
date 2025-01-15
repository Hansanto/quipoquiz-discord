package io.github.hansanto.quipoquiz.discord.framework.component.paginator

import dev.kord.core.entity.interaction.ButtonInteraction
import io.github.hansanto.generated.i18n.Messages
import io.github.hansanto.quipoquiz.Language
import io.github.hansanto.quipoquiz.discord.framework.component.container.Container
import io.github.hansanto.quipoquiz.discord.framework.extension.respondEphemeralError
import io.github.hansanto.quipoquiz.from
import io.github.hansanto.quipoquiz.game.player.PlayerManager

/**
 * Chain multiple [PreProcessButtonInteraction] into a single [PreProcessButtonInteraction].
 */
fun PreProcessButtonInteraction.chainWith(other: PreProcessButtonInteraction): PreProcessButtonInteraction {
    return ChainPreProcessButtonInteraction(this, other)
}

/**
 * Interface to process action or verify conditions before processing the button interaction.
 */
fun interface PreProcessButtonInteraction {

    /**
     * Process the button interaction before the button is processed.
     * @param interaction Button interaction to process.
     * @return `true` if the button interaction should be processed, `false` otherwise.
     */
    suspend fun perform(interaction: ButtonInteraction): Boolean
}

/**
 * Chain multiple [PreProcessButtonInteraction] into a single [PreProcessButtonInteraction].
 */
class ChainPreProcessButtonInteraction(
    val first: PreProcessButtonInteraction,
    val second: PreProcessButtonInteraction
) : PreProcessButtonInteraction {
    override suspend fun perform(interaction: ButtonInteraction): Boolean {
        return first.perform(interaction) && second.perform(interaction)
    }
}

/**
 * Check if the user is the owner of the container.
 * If the user is not the owner, an ephemeral message will be sent to inform the user.
 */
class OwnerUserCheckButtonInteraction(
    /**
     * Container to check if the user is the owner.
     */
    val container: Container
) : PreProcessButtonInteraction {

    override suspend fun perform(interaction: ButtonInteraction): Boolean {
        if (container.isOwner(interaction.user.id)) return true

        val language = Language.from(interaction.locale)
        interaction.respondEphemeralError(Messages.error_only_owner_can(language.i18nLocale))
        return false
    }
}

/**
 * Check if the user is authorized to interact with the container.
 * If the user is not authorized, an ephemeral message will be sent to inform the user.
 */
class AuthorizedUserCheckButtonInteraction(
    val container: Container
) : PreProcessButtonInteraction {

    override suspend fun perform(interaction: ButtonInteraction): Boolean {
        if (container.isAuthorized(interaction.user.id)) return true

        val language = Language.from(interaction.locale)
        interaction.respondEphemeralError(Messages.error_not_authorized(language.i18nLocale))
        return false
    }
}

/**
 * Check if the player has life.
 * If the player doesn't have life, an ephemeral message will be sent to inform the user.
 */
class PlayerHasLifeCheckButtonInteraction(val playerManager: PlayerManager) : PreProcessButtonInteraction {

    override suspend fun perform(interaction: ButtonInteraction): Boolean {
        val player = playerManager.getPlayer(interaction.user) ?: return true
        if (player.hasLife()) return true

        val language = Language.from(interaction.locale)
        interaction.respondEphemeralError(Messages.error_play_no_life(language.i18nLocale))
        return false
    }
}
