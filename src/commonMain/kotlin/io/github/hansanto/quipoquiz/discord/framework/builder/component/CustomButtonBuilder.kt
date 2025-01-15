package io.github.hansanto.quipoquiz.discord.framework.builder.component

import dev.kord.common.annotation.KordDsl
import dev.kord.common.entity.ButtonStyle
import dev.kord.common.entity.DiscordPartialEmoji
import dev.kord.rest.builder.component.ActionRowComponentBuilder
import dev.kord.rest.builder.component.ButtonBuilder

@KordDsl
sealed class CustomButtonBuilder : CustomActionRowComponentBuilder() {

    /**
     * @see ButtonBuilder.label
     */
    var label: String? = null

    /**
     * @see ButtonBuilder.emoji
     */
    var emoji: DiscordPartialEmoji? = null

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false
        if (!super.equals(other)) return false

        other as CustomButtonBuilder

        if (label != other.label) return false
        if (emoji != other.emoji) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + (label?.hashCode() ?: 0)
        result = 31 * result + (emoji?.hashCode() ?: 0)
        return result
    }

    /**
     * @see ButtonBuilder.InteractionButtonBuilder
     */
    @KordDsl
    class CustomInteractionButtonBuilder(
        var style: ButtonStyle,
        var customId: String
    ) : CustomButtonBuilder() {

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other == null || this::class != other::class) return false
            if (!super.equals(other)) return false

            other as CustomInteractionButtonBuilder

            if (style != other.style) return false
            if (customId != other.customId) return false

            return true
        }

        override fun hashCode(): Int {
            var result = super.hashCode()
            result = 31 * result + style.hashCode()
            result = 31 * result + customId.hashCode()
            return result
        }

        override fun convert(): ActionRowComponentBuilder {
            return ButtonBuilder.InteractionButtonBuilder(style, customId).also {
                it.label = label
                it.emoji = emoji
                it.disabled = disabled
            }
        }
    }

    /**
     * @see ButtonBuilder.LinkButtonBuilder
     */
    @KordDsl
    class CustomLinkButtonBuilder(
        var url: String
    ) : CustomButtonBuilder() {

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other == null || this::class != other::class) return false
            if (!super.equals(other)) return false

            other as CustomLinkButtonBuilder

            return url == other.url
        }

        override fun hashCode(): Int {
            var result = super.hashCode()
            result = 31 * result + url.hashCode()
            return result
        }

        override fun convert(): ActionRowComponentBuilder {
            return ButtonBuilder.LinkButtonBuilder(url).also {
                it.label = label
                it.emoji = emoji
                it.disabled = disabled
            }
        }
    }
}
