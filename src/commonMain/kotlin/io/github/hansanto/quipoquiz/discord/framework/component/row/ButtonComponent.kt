package io.github.hansanto.quipoquiz.discord.framework.component.row

import dev.kord.core.Kord
import dev.kord.core.event.interaction.ButtonInteractionCreateEvent
import io.github.hansanto.quipoquiz.discord.framework.builder.component.CustomActionRowBuilder
import io.github.hansanto.quipoquiz.discord.framework.extension.onEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job

abstract class ButtonComponent : AbstractRowComponent<ButtonInteractionCreateEvent>() {

    override val width: Int
        get() = 1
}

/**
 * Represents a clickable button component.
 */
abstract class ClickableButtonComponent(
    /**
     * `true` to disable the button, `false` otherwise.
     */
    val disabled: Boolean,
    /**
     * The Kord instance.
     */
    protected val kord: Kord,
    protected val executionEventScope: CoroutineScope,
    protected val launchInEventScope: CoroutineScope
) : ButtonComponent() {

    override suspend fun render(builder: CustomActionRowBuilder) {
        if (!disabled) {
            super.render(builder)
        } else {
            renderActionRow(builder)
        }
    }

    override fun createListener(): Job {
        require(!disabled) { "The button is disabled, the listener cannot be created." }
        return kord.onEvent<ButtonInteractionCreateEvent>(
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

/**
 * Represents a non-clickable button component.
 */
abstract class NonClickableButtonComponent : ButtonComponent() {

    override suspend fun render(builder: CustomActionRowBuilder) {
        renderActionRow(builder)
    }

    override fun createListener(): Job {
        throw UnsupportedOperationException("This button is not usable.")
    }
}
