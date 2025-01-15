package io.github.hansanto.quipoquiz.cache.decorator

import io.github.hansanto.quipoquiz.cache.AbstractCache
import io.github.hansanto.quipoquiz.cache.Cache
import io.github.hansanto.quipoquiz.cache.CacheData

/**
 * Propagate the new data written to this cache to another cache.
 * @receiver Main cache where the data will be got and set.
 * @param second Cache where the new data will be propagated.
 * @return New cache.
 */
fun <K, V> Cache<K, V>.writePropagation(second: Cache<K, V>): Cache<K, V> {
    return WritePropagationCache(this, second)
}

/**
 * Cache that propagates the new data written to this cache to another cache.
 * @param K Type of the key to store.
 * @param V Type of data to be cached.
 */
class WritePropagationCache<K, V>(
    /**
     * Main cache where the data will be got and set.
     */
    private val cache: Cache<K, V>,
    /**
     * Cache where the new data will be propagated.
     */
    private val second: Cache<K, V>
) : AbstractCache<K, V>(cache.expirationAfterWrite) {

    override suspend fun getCacheData(key: K): CacheData<V>? {
        return cache.getCacheData(key)
    }

    override suspend fun setCacheData(key: K, value: CacheData<V>) {
        cache.setCacheData(key, value)
        second.setCacheData(key, value)
    }
}

/**
 * Propagate the data read from this cache to another cache.
 * @receiver Main cache where the data will be got and set.
 * @param second Cache where the data will be stored when obtained from the main cache.
 * @return Cache that propagates the data read from this cache to another cache.
 */
fun <K, V> Cache<K, V>.readPropagation(second: Cache<K, V>): Cache<K, V> {
    return ReadPropagationCache(this, second)
}

/**
 * Cache that stores a data found in the cache to another cache.
 * @param K Type of the key to store.
 * @param V Type of data to be cached.
 * @property cache Main cache where the data will be got and set.
 * @property second Cache where the data will be stored when obtained from the main cache.
 */
class ReadPropagationCache<K, V>(
    private val cache: Cache<K, V>,
    private val second: Cache<K, V>
) : AbstractCache<K, V>(cache.expirationAfterWrite) {

    override suspend fun getCacheData(key: K): CacheData<V>? {
        return cache.getCacheData(key)?.also {
            second.setCacheData(key, it)
        }
    }

    override suspend fun setCacheData(key: K, value: CacheData<V>) {
        cache.setCacheData(key, value)
    }
}
