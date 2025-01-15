package io.github.hansanto.quipoquiz.discord.framework.builder.component

import dev.kord.common.annotation.KordDsl
import dev.kord.common.entity.ButtonStyle
import dev.kord.common.entity.TextInputStyle
import dev.kord.rest.builder.component.ActionRowBuilder
import dev.kord.rest.builder.component.MessageComponentBuilder
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

@KordDsl
class CustomActionRowBuilder : CustomMessageComponentBuilder {
    val components: MutableList<CustomActionRowComponentBuilder> = mutableListOf()

    /**
     * @see ActionRowBuilder.interactionButton
     */
    inline fun interactionButton(
        style: ButtonStyle,
        customId: String,
        builder: CustomButtonBuilder.CustomInteractionButtonBuilder.() -> Unit
    ) {
        contract {
            callsInPlace(builder, InvocationKind.EXACTLY_ONCE)
        }

        components.add(
            CustomButtonBuilder.CustomInteractionButtonBuilder(style, customId).apply(builder)
        )
    }

    /**
     * @see ActionRowBuilder.linkButton
     */
    inline fun linkButton(url: String, builder: CustomButtonBuilder.CustomLinkButtonBuilder.() -> Unit) {
        contract {
            callsInPlace(builder, InvocationKind.EXACTLY_ONCE)
        }

        components.add(
            CustomButtonBuilder.CustomLinkButtonBuilder(url).apply(builder)
        )
    }

    /**
     * @see ActionRowBuilder.stringSelect
     */
    inline fun stringSelect(customId: String, builder: CustomStringSelectBuilder.() -> Unit) {
        contract {
            callsInPlace(builder, InvocationKind.EXACTLY_ONCE)
        }

        components.add(CustomStringSelectBuilder(customId).apply(builder))
    }

    /**
     * @see ActionRowBuilder.userSelect
     */
    inline fun userSelect(customId: String, builder: CustomUserSelectBuilder.() -> Unit = {}) {
        contract {
            callsInPlace(builder, InvocationKind.EXACTLY_ONCE)
        }

        components.add(CustomUserSelectBuilder(customId).apply(builder))
    }

    /**
     * @see ActionRowBuilder.roleSelect
     */
    inline fun roleSelect(customId: String, builder: CustomRoleSelectBuilder.() -> Unit = {}) {
        contract {
            callsInPlace(builder, InvocationKind.EXACTLY_ONCE)
        }

        components.add(CustomRoleSelectBuilder(customId).apply(builder))
    }

    /**
     * @see ActionRowBuilder.mentionableSelect
     */
    inline fun mentionableSelect(customId: String, builder: CustomMentionableSelectBuilder.() -> Unit = {}) {
        contract {
            callsInPlace(builder, InvocationKind.EXACTLY_ONCE)
        }

        components.add(CustomMentionableSelectBuilder(customId).apply(builder))
    }

    /**
     * @see ActionRowBuilder.channelSelect
     */
    inline fun channelSelect(customId: String, builder: CustomChannelSelectBuilder.() -> Unit = {}) {
        contract {
            callsInPlace(builder, InvocationKind.EXACTLY_ONCE)
        }

        components.add(CustomChannelSelectBuilder(customId).apply(builder))
    }

    /**
     * @see ActionRowBuilder.textInput
     */
    inline fun textInput(
        style: TextInputStyle,
        customId: String,
        label: String,
        builder: CustomTextInputBuilder.() -> Unit = {}
    ) {
        contract {
            callsInPlace(builder, InvocationKind.EXACTLY_ONCE)
        }

        components.add(CustomTextInputBuilder(style, customId, label).apply(builder))
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as CustomActionRowBuilder

        return components == other.components
    }

    override fun hashCode(): Int {
        return components.hashCode()
    }

    override fun convert(): MessageComponentBuilder {
        return ActionRowBuilder().also {
            it.components.addAll(components.map(CustomActionRowComponentBuilder::convert))
        }
    }
}
