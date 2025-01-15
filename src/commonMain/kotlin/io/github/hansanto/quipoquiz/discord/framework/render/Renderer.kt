package io.github.hansanto.quipoquiz.discord.framework.render

import dev.kord.core.behavior.interaction.response.DeferredMessageInteractionResponseBehavior
import dev.kord.core.behavior.interaction.response.MessageInteractionResponseBehavior
import dev.kord.core.event.interaction.ButtonInteractionCreateEvent
import dev.kord.core.event.interaction.ComponentInteractionCreateEvent
import io.github.hansanto.quipoquiz.discord.framework.component.EmbedComponent
import io.github.hansanto.quipoquiz.discord.framework.component.RowComponent
import io.github.hansanto.quipoquiz.discord.framework.component.container.builder.InteractionDeltaBuilder
import io.github.hansanto.quipoquiz.discord.framework.component.container.builder.MessageBuilder

/**
 * Interface for components that can be rendered to the response.
 */
interface View {

    /**
     * Render the components to the response.
     * @param event Event of the interaction.
     */
    suspend fun render(event: DeferredMessageInteractionResponseBehavior)

    /**
     * Render the components to the response.
     * @param event Event of the interaction.
     */
    suspend fun render(event: MessageInteractionResponseBehavior)

    /**
     * Render the components to the response.
     * @param event Event of the interaction.
     */
    suspend fun render(event: ComponentInteractionCreateEvent)

    /**
     * Acknowledge the interaction and does not render the components.
     * @param event Event of the interaction.
     */
    suspend fun acknowledge(event: ButtonInteractionCreateEvent)
}

/**
 * Renderer [messageBuilder] to add components to the response.
 */
interface Renderer {

    /**
     * Builder that contains the components.
     */
    val messageBuilder: MessageBuilder

    /**
     * Render the components to the response.
     */
    suspend fun render(builder: InteractionDeltaBuilder)

    /**
     * Render the [EmbedComponents][EmbedComponent] to the response.
     * @param builder Builder of the response.
     */
    suspend fun renderEmbeds(builder: InteractionDeltaBuilder)

    /**
     * Render the [RowComponents][RowComponent] to the response.
     * @param builder Builder of the response.
     */
    suspend fun renderRowComponents(builder: InteractionDeltaBuilder)
}
