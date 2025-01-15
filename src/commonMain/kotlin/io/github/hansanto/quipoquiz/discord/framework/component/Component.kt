package io.github.hansanto.quipoquiz.discord.framework.component

import dev.kord.rest.builder.message.EmbedBuilder
import io.github.hansanto.quipoquiz.discord.framework.builder.component.CustomActionRowBuilder
import io.github.hansanto.quipoquiz.discord.framework.builder.message.CustomEmbedBuilder
import io.github.hansanto.quipoquiz.discord.framework.component.container.builder.MessageBuilder
import io.github.hansanto.quipoquiz.util.Identifiable

/**
 * Visual element that can be rendered in Discord messages.
 * @param T The type of the builder to render the component.
 */
interface Component<T> : Identifiable {

    /**
     * Render the component.
     * Will add the visual element to the builder.
     * @param builder The builder to render the component.
     */
    suspend fun render(builder: T)

    /**
     * Unregisters the entity.
     */
    suspend fun cancel() {}
}

/**
 * Component that can contain other components and register them when the component is rendered.
 * Produces no visual element by itself.
 */
interface GroupComponent : Component<Unit> {

    /**
     * Builder where the components will be rendered.
     */
    val messageBuilder: MessageBuilder

    override suspend fun render(builder: Unit) {
        registerComponents()
    }

    /**
     * Register the components to render.
     */
    suspend fun registerComponents()
}

/**
 * Component that can be rendered in an [embed][EmbedBuilder] to display rich content.
 */
interface EmbedComponent : Component<CustomEmbedBuilder>

/**
 * Interactable component that can be rendered in an [action row][CustomActionRowBuilder]
 * to display buttons, selectors, etc.
 */
interface RowComponent : Component<CustomActionRowBuilder> {

    /**
     * Width of the component.
     * This size is defined by Discord and cannot be changed.
     */
    val width: Int
}
