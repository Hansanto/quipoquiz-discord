package io.github.hansanto.quipoquiz.discord.framework.builder.component

import dev.kord.common.annotation.KordDsl
import dev.kord.common.entity.ChannelType
import dev.kord.common.entity.ComponentType
import dev.kord.common.entity.Snowflake
import dev.kord.rest.builder.component.ActionRowComponentBuilder
import dev.kord.rest.builder.component.ChannelSelectBuilder
import dev.kord.rest.builder.component.MentionableSelectBuilder
import dev.kord.rest.builder.component.RoleSelectBuilder
import dev.kord.rest.builder.component.SelectMenuBuilder
import dev.kord.rest.builder.component.StringSelectBuilder
import dev.kord.rest.builder.component.UserSelectBuilder
import io.github.hansanto.quipoquiz.discord.framework.builder.convert
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

/**
 * @see SelectMenuBuilder
 */
@KordDsl
sealed class CustomSelectMenuBuilder(var customId: String) : CustomActionRowComponentBuilder() {

    /**
     * @see SelectMenuBuilder.allowedValues
     */
    var allowedValues: ClosedRange<Int> = 1..1

    /**
     * @see SelectMenuBuilder.placeholder
     */
    var placeholder: String? = null

    protected abstract val type: ComponentType

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false
        if (!super.equals(other)) return false

        other as CustomSelectMenuBuilder

        if (customId != other.customId) return false
        if (allowedValues != other.allowedValues) return false
        if (placeholder != other.placeholder) return false
        if (type != other.type) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + customId.hashCode()
        result = 31 * result + allowedValues.hashCode()
        result = 31 * result + (placeholder?.hashCode() ?: 0)
        result = 31 * result + type.hashCode()
        return result
    }
}

/**
 * @see StringSelectBuilder
 */
@KordDsl
class CustomStringSelectBuilder(customId: String) : CustomSelectMenuBuilder(customId) {
    override val type: ComponentType get() = ComponentType.StringSelect

    /**
     * @see StringSelectBuilder.options
     */
    var options: MutableList<CustomSelectOptionBuilder> = mutableListOf()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false
        if (!super.equals(other)) return false

        other as CustomStringSelectBuilder

        if (type != other.type) return false
        if (options != other.options) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + type.hashCode()
        result = 31 * result + options.hashCode()
        return result
    }

    override fun convert(): ActionRowComponentBuilder {
        return StringSelectBuilder(customId).also {
            it.options = options.convert()
            it.allowedValues = allowedValues
            it.placeholder = placeholder
            it.disabled = disabled
        }
    }
}

/**
 * Adds a new option to the select menu with the given [label] and [value] that can be configured by the [builder].
 *
 * @param label The user-facing name of the option, max 100 characters.
 * @param value The dev-defined value of the option, max 100 characters.
 */
inline fun CustomStringSelectBuilder.option(
    label: String,
    value: String,
    builder: CustomSelectOptionBuilder.() -> Unit = {}
) {
    contract { callsInPlace(builder, InvocationKind.EXACTLY_ONCE) }

    options.add(CustomSelectOptionBuilder(label = label, value = value).apply(builder))
}

/**
 * @see UserSelectBuilder
 */
@KordDsl
class CustomUserSelectBuilder(customId: String) : CustomSelectMenuBuilder(customId) {

    override val type: ComponentType get() = ComponentType.UserSelect

    /**
     * @see UserSelectBuilder.defaultUsers
     */
    val defaultUsers: MutableList<Snowflake> = mutableListOf()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false
        if (!super.equals(other)) return false

        other as CustomUserSelectBuilder

        if (type != other.type) return false
        if (defaultUsers != other.defaultUsers) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + type.hashCode()
        result = 31 * result + defaultUsers.hashCode()
        return result
    }

    override fun convert(): ActionRowComponentBuilder {
        return UserSelectBuilder(customId).also {
            it.allowedValues = allowedValues
            it.placeholder = placeholder
            it.defaultUsers.addAll(defaultUsers)
            it.disabled = disabled
        }
    }
}

/**
 * @see RoleSelectBuilder
 */
@KordDsl
class CustomRoleSelectBuilder(customId: String) : CustomSelectMenuBuilder(customId) {
    override val type: ComponentType get() = ComponentType.RoleSelect

    /**
     * @see RoleSelectBuilder.defaultRoles
     */
    val defaultRoles: MutableList<Snowflake> = mutableListOf()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false
        if (!super.equals(other)) return false

        other as CustomRoleSelectBuilder

        if (type != other.type) return false
        if (defaultRoles != other.defaultRoles) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + type.hashCode()
        result = 31 * result + defaultRoles.hashCode()
        return result
    }

    override fun convert(): ActionRowComponentBuilder {
        return RoleSelectBuilder(customId).also {
            it.allowedValues = allowedValues
            it.placeholder = placeholder
            it.defaultRoles.addAll(defaultRoles)
            it.disabled = disabled
        }
    }
}

@KordDsl
class CustomMentionableSelectBuilder(customId: String) : CustomSelectMenuBuilder(customId) {
    override val type: ComponentType get() = ComponentType.MentionableSelect

    /**
     * @see MentionableSelectBuilder.defaultUsers
     */
    val defaultUsers: MutableList<Snowflake> = mutableListOf()

    /**
     * @see MentionableSelectBuilder.defaultRoles
     */
    val defaultRoles: MutableList<Snowflake> = mutableListOf()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false
        if (!super.equals(other)) return false

        other as CustomMentionableSelectBuilder

        if (type != other.type) return false
        if (defaultUsers != other.defaultUsers) return false
        if (defaultRoles != other.defaultRoles) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + type.hashCode()
        result = 31 * result + defaultUsers.hashCode()
        result = 31 * result + defaultRoles.hashCode()
        return result
    }

    override fun convert(): ActionRowComponentBuilder {
        return MentionableSelectBuilder(customId).also {
            it.allowedValues = allowedValues
            it.placeholder = placeholder
            it.defaultUsers.addAll(defaultUsers)
            it.defaultRoles.addAll(defaultRoles)
            it.disabled = disabled
        }
    }
}

@KordDsl
class CustomChannelSelectBuilder(customId: String) : CustomSelectMenuBuilder(customId) {
    override val type: ComponentType get() = ComponentType.ChannelSelect

    /**
     * @see ChannelSelectBuilder.channelTypes
     */
    var channelTypes: MutableList<ChannelType>? = null

    /**
     * @see ChannelSelectBuilder.defaultChannels
     */
    val defaultChannels: MutableList<Snowflake> = mutableListOf()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false
        if (!super.equals(other)) return false

        other as CustomChannelSelectBuilder

        if (type != other.type) return false
        if (channelTypes != other.channelTypes) return false
        if (defaultChannels != other.defaultChannels) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + type.hashCode()
        result = 31 * result + (channelTypes?.hashCode() ?: 0)
        result = 31 * result + defaultChannels.hashCode()
        return result
    }

    override fun convert(): ActionRowComponentBuilder {
        return ChannelSelectBuilder(customId).also {
            it.allowedValues = allowedValues
            it.placeholder = placeholder
            it.channelTypes = channelTypes
            it.defaultChannels.addAll(defaultChannels)
            it.disabled = disabled
        }
    }
}

fun CustomChannelSelectBuilder.channelType(type: ChannelType) {
    channelTypes?.add(type) ?: run { channelTypes = mutableListOf(type) }
}
