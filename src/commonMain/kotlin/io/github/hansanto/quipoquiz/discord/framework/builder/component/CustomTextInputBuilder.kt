package io.github.hansanto.quipoquiz.discord.framework.builder.component

import dev.kord.common.annotation.KordDsl
import dev.kord.common.entity.TextInputStyle
import dev.kord.rest.builder.component.ActionRowComponentBuilder
import dev.kord.rest.builder.component.TextInputBuilder

/**
 * @see TextInputBuilder
 */
@KordDsl
class CustomTextInputBuilder(
    var style: TextInputStyle,
    var customId: String,
    var label: String
) : CustomActionRowComponentBuilder() {
    /**
     * @see TextInputBuilder.allowedLength
     */
    var allowedLength: ClosedRange<Int>? = null

    /**
     * @see TextInputBuilder.placeholder
     */
    var placeholder: String? = null

    /**
     * @see TextInputBuilder.value
     */
    var value: String? = null

    /**
     * @see TextInputBuilder.required
     */
    var required: Boolean? = null

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false
        if (!super.equals(other)) return false

        other as CustomTextInputBuilder

        if (style != other.style) return false
        if (customId != other.customId) return false
        if (label != other.label) return false
        if (allowedLength != other.allowedLength) return false
        if (placeholder != other.placeholder) return false
        if (value != other.value) return false
        if (required != other.required) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + style.hashCode()
        result = 31 * result + customId.hashCode()
        result = 31 * result + label.hashCode()
        result = 31 * result + (allowedLength?.hashCode() ?: 0)
        result = 31 * result + (placeholder?.hashCode() ?: 0)
        result = 31 * result + (value?.hashCode() ?: 0)
        result = 31 * result + (required?.hashCode() ?: 0)
        return result
    }

    override fun convert(): ActionRowComponentBuilder {
        return TextInputBuilder(style, customId, label).also {
            it.allowedLength = allowedLength
            it.placeholder = placeholder
            it.value = value
            it.required = required
        }
    }
}
