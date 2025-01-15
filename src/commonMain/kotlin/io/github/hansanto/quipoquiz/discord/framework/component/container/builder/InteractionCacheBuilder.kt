package io.github.hansanto.quipoquiz.discord.framework.component.container.builder

import dev.kord.rest.builder.message.modify.InteractionResponseModifyBuilder
import io.github.hansanto.quipoquiz.discord.framework.builder.component.CustomActionRowBuilder
import io.github.hansanto.quipoquiz.discord.framework.builder.component.CustomMessageComponentBuilder
import io.github.hansanto.quipoquiz.discord.framework.builder.convert
import io.github.hansanto.quipoquiz.discord.framework.builder.message.CustomEmbedBuilder
import kotlin.contracts.InvocationKind.EXACTLY_ONCE
import kotlin.contracts.contract

/**
 * Builder for the interaction response.
 * @param embeds The list of embeds to be added to the interaction response.
 * @param components The list of components to be added to the interaction response.
 */
data class InteractionCacheBuilder(
    /**
     * @see InteractionResponseModifyBuilder.embeds
     */
    var embeds: MutableList<CustomEmbedBuilder> = mutableListOf(),
    /**
     * @see InteractionResponseModifyBuilder.components
     */
    var components: MutableList<CustomMessageComponentBuilder> = mutableListOf()
)

/**
 * Builder for the delta of the interaction response.
 * Based on the last rendering, if the embeds or components are not `null`, they will be updated.
 * But if they are `null`, that means they are similar to the last rendering and will not be updated.
 * @param embeds The list of embeds to be added to the interaction response.
 * @param components The list of components to be added to the interaction response.
 */
data class InteractionDeltaBuilder(
    /**
     * @see InteractionResponseModifyBuilder.embeds
     * If the embeds is `null`, this means that the embeds will not be updated.
     */
    var embeds: MutableList<CustomEmbedBuilder>? = null,
    /**
     * @see InteractionResponseModifyBuilder.components
     * If the components is `null`, this means that the components will not be updated.
     */
    var components: MutableList<CustomMessageComponentBuilder>? = null
) {

    constructor(builder: InteractionCacheBuilder) : this(
        builder.embeds.toMutableList(),
        builder.components.toMutableList()
    )

    /**
     * Add an embed to the interaction response.
     * @param builder The builder for the embed.
     */
    inline fun embed(builder: CustomEmbedBuilder.() -> Unit) {
        contract { callsInPlace(builder, EXACTLY_ONCE) }
        val embed = CustomEmbedBuilder().apply(builder)
        embeds?.add(embed) ?: run { embeds = mutableListOf(embed) }
    }

    /**
     * Add an action row to the interaction response.
     * @param builder The builder for the action row.
     */
    inline fun actionRow(builder: CustomActionRowBuilder.() -> Unit) {
        contract { callsInPlace(builder, EXACTLY_ONCE) }
        val actionRow = CustomActionRowBuilder().apply(builder)
        components?.add(actionRow) ?: run { components = mutableListOf(actionRow) }
    }

    /**
     * Apply the update to the builder.
     * If [embeds] is `null`, [InteractionResponseModifyBuilder.embeds] will not be updated
     * and will remain the same.
     * If [components] is `null`, [InteractionResponseModifyBuilder.components] will not be updated
     * and will remain the same.
     */
    fun applyUpdate(builder: InteractionResponseModifyBuilder) {
        embeds?.let { builder.embeds = it.convert() }
        components?.let { builder.components = it.convert() }
        // If the embeds are deleted by the user (cross button), the embeds will be replaced
        builder.suppressEmbeds = false
    }

    /**
     * Check if the builder has an update to apply.
     * @return `true` if [embeds] or [components] is not `null`, `false` otherwise.
     */
    fun hasUpdate(): Boolean {
        return embeds != null || components != null
    }
}
