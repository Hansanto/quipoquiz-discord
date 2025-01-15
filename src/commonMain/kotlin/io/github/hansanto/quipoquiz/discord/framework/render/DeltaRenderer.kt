package io.github.hansanto.quipoquiz.discord.framework.render

import io.github.hansanto.quipoquiz.discord.framework.component.container.builder.InteractionDeltaBuilder
import io.github.hansanto.quipoquiz.discord.framework.component.container.builder.MessageBuilder

/**
 * A renderer that only updates the differences between the current and the new state.
 * This is useful to avoid unnecessary updates in the interaction response.
 */
class DeltaRenderer(private val renderer: Renderer) : Renderer {

    override val messageBuilder: MessageBuilder
        get() = renderer.messageBuilder

    override suspend fun render(builder: InteractionDeltaBuilder) {
        val tmpBuilder = InteractionDeltaBuilder()
        renderer.render(tmpBuilder)
        setComponents(builder, tmpBuilder)
        setEmbeds(builder, tmpBuilder)
    }

    override suspend fun renderEmbeds(builder: InteractionDeltaBuilder) {
        val tmpBuilder = InteractionDeltaBuilder()
        renderer.renderEmbeds(tmpBuilder)
        setEmbeds(builder, tmpBuilder)
    }

    override suspend fun renderRowComponents(builder: InteractionDeltaBuilder) {
        val tmpBuilder = InteractionDeltaBuilder()
        renderer.renderRowComponents(tmpBuilder)
        setComponents(builder, tmpBuilder)
    }

    /**
     * Set the [InteractionDeltaBuilder.components] of [builderToUpdate] to null if it is similar to [tmpBuilder].
     * Otherwise, set it to the new components.
     * @param builderToUpdate The builder to update.
     * @param tmpBuilder The builder with the new components.
     */
    private fun setComponents(builderToUpdate: InteractionDeltaBuilder, tmpBuilder: InteractionDeltaBuilder) {
        val components = tmpBuilder.components
        // Set to null if similar to avoid unnecessary updates
        builderToUpdate.components = if (builderToUpdate.components != components) components else null
    }

    /**
     * Set the [InteractionDeltaBuilder.embeds] of [builderToUpdate] to null if it is similar to [tmpBuilder].
     * Otherwise, set it to the new embeds.
     * @param builderToUpdate The builder to update.
     * @param tmpBuilder The builder with the new embeds.
     */
    private fun setEmbeds(builderToUpdate: InteractionDeltaBuilder, tmpBuilder: InteractionDeltaBuilder) {
        val embeds = tmpBuilder.embeds
        // Set to null if similar to avoid unnecessary updates
        builderToUpdate.embeds = if (builderToUpdate.embeds != embeds) embeds else null
    }
}
