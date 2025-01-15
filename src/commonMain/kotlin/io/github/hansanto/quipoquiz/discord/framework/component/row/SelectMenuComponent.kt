package io.github.hansanto.quipoquiz.discord.framework.component.row

import dev.kord.core.Kord
import dev.kord.core.event.interaction.SelectMenuInteractionCreateEvent
import io.github.hansanto.quipoquiz.discord.framework.component.container.builder.SingleRowMessageBuilder.Companion.DISCORD_WIDTH_ROW
import io.github.hansanto.quipoquiz.discord.framework.extension.onEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job

/**
 * Represents a navigation component of the paginator.
 */
abstract class SelectMenuComponent(
    /**
     * The Kord instance.
     */
    protected val kord: Kord,
    protected val executionEventScope: CoroutineScope,
    protected val launchInEventScope: CoroutineScope
) : AbstractRowComponent<SelectMenuInteractionCreateEvent>() {

    override val width: Int
        get() = DISCORD_WIDTH_ROW

    override fun createListener(): Job {
        return kord.onEvent<SelectMenuInteractionCreateEvent>(
            executionScope = executionEventScope,
            launchInScope = launchInEventScope
        ) {
            if (interaction.componentId != id) {
                return@onEvent
            }

            action?.invoke(this)
        }
    }
}
