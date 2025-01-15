@file:Suppress("PropertyName")

package io.github.hansanto.quipoquiz.discord.framework.builder.component

import dev.kord.common.annotation.KordDsl
import dev.kord.rest.builder.component.ActionRowComponentBuilder
import dev.kord.rest.builder.component.MessageComponentBuilder
import io.github.hansanto.quipoquiz.discord.framework.builder.ConvertableKordBuilder

@KordDsl
abstract class CustomActionRowComponentBuilder : ConvertableKordBuilder<ActionRowComponentBuilder> {

    var disabled: Boolean? = null

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as CustomActionRowComponentBuilder

        return disabled == other.disabled
    }

    override fun hashCode(): Int {
        return disabled?.hashCode() ?: 0
    }
}

/**
 * @see dev.kord.rest.builder.component.MessageComponentBuilder
 */
@KordDsl
sealed interface CustomMessageComponentBuilder : ConvertableKordBuilder<MessageComponentBuilder>
