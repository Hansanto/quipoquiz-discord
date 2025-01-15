package io.github.hansanto.quipoquiz.discord.framework.builder.component

import dev.kord.common.annotation.KordDsl
import dev.kord.common.entity.DiscordPartialEmoji
import dev.kord.rest.builder.component.SelectOptionBuilder
import io.github.hansanto.quipoquiz.discord.framework.builder.ConvertableKordBuilder

/**
 * @see SelectOptionBuilder
 */
@KordDsl
class CustomSelectOptionBuilder(
    var label: String,
    var value: String
) : ConvertableKordBuilder<SelectOptionBuilder> {

    /**
     * @see SelectOptionBuilder.description
     */
    var description: String? = null

    /**
     * @see SelectOptionBuilder.emoji
     */
    var emoji: DiscordPartialEmoji? = null

    /**
     * @see SelectOptionBuilder.default
     */
    var default: Boolean? = null

    override fun convert(): SelectOptionBuilder {
        return SelectOptionBuilder(label, value).also {
            it.description = description
            it.emoji = emoji
            it.default = default
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as CustomSelectOptionBuilder

        if (label != other.label) return false
        if (value != other.value) return false
        if (description != other.description) return false
        if (emoji != other.emoji) return false
        if (default != other.default) return false

        return true
    }

    override fun hashCode(): Int {
        var result = label.hashCode()
        result = 31 * result + value.hashCode()
        result = 31 * result + (description?.hashCode() ?: 0)
        result = 31 * result + (emoji?.hashCode() ?: 0)
        result = 31 * result + (default?.hashCode() ?: 0)
        return result
    }
}
