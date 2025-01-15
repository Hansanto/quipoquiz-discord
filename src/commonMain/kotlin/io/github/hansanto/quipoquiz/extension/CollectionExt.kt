package io.github.hansanto.quipoquiz.extension

/**
 * Check if all elements in both collections are present whatever the order.
 * @receiver Collection with elements of type T
 * @param other Collection with elements of type T
 * @return `true` if both collections contain the same elements, `false` otherwise
 */
fun <T> Collection<T>.containsExactly(other: Collection<T>): Boolean {
    return size == other.size && containsAll(other)
}
