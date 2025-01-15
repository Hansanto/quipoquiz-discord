package io.github.hansanto.quipoquiz.cache.decorator

import io.github.hansanto.quipoquiz.cache.AbstractCache
import io.github.hansanto.quipoquiz.cache.Cache
import io.github.hansanto.quipoquiz.cache.CacheData

/**
 * Create a cache that falls back to another cache if the data is not found in the main cache.
 * @receiver Main cache.
 * @param fallback Secondary cache.
 * @return New cache.
 */
fun <K, V> Cache<K, V>.fallback(fallback: Cache<K, V>): Cache<K, V> {
    return FallbackCache(this, fallback)
}

/**
 * Cache that falls back to another cache if the data is not found in the main cache.
 * @param K Type of the key to store.
 * @param V Type of data to be cached.
 */
class FallbackCache<K, V>(
    /**
     * Main cache.
     */
    private val cache: Cache<K, V>,
    /**
     * Secondary cache.
     */
    private val fallback: Cache<K, V>
) : AbstractCache<K, V>(cache.expirationAfterWrite) {

    override suspend fun getCacheData(key: K): CacheData<V>? {
        return cache.getCacheData(key) ?: fallback.getCacheData(key)
    }

    override suspend fun setCacheData(key: K, value: CacheData<V>) {
        cache.setCacheData(key, value)
    }
}
