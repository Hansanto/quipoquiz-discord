package io.github.hansanto.quipoquiz.extension

/**
 * Check if at least one element in the collection matches the given [predicate].
 * Even if an element matches the predicate, the iteration will continue until of the end.
 * @receiver Iterable with elements of type T
 * @param predicate Function that will receive each element and return a boolean
 * @return `true` if at least one element matches the predicate, `false` otherwise
 */
inline fun <T> Array<T>.forEachAnyMatch(predicate: (T) -> Boolean): Boolean {
    var match = false
    forEach {
        match = predicate(it) || match
    }
    return match
}
