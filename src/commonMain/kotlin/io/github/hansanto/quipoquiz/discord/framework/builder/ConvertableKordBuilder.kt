package io.github.hansanto.quipoquiz.discord.framework.builder

import dev.kord.common.annotation.KordDsl

/**
 * Converts a list of [ConvertableKordBuilder] to a list of the target type.
 * @receiver List of convertable builders.
 * @return List of converted objects.
 */
fun <T> Iterable<ConvertableKordBuilder<T>>.convert(): MutableList<T> = mapTo(mutableListOf()) { it.convert() }

/**
 * Defines a builder that can be converted to a target type.
 * @param T The target type.
 */
@KordDsl
interface ConvertableKordBuilder<T> {

    /**
     * Converts the builder to the target type.
     * @return The converted object.
     */
    fun convert(): T
}
