package io.github.hansanto.quipoquiz.cache

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.time.Duration

/**
 * Cache to store information in memory.
 * @param K The type of the key to store.
 * @param V The type of the data to store.
 */
class MemoryCache<K, V>(
    /**
     * @see AbstractCache.expirationAfterWrite
     */
    expirationAfterWrite: Duration
) : AbstractCache<K, V>(expirationAfterWrite) {

    /**
     * Mutex to synchronize access to the cache.
     */
    private val mutex = Mutex()

    /**
     * Cache data stored in memory.
     */
    private val cache = mutableMapOf<K, CacheData<V>>()

    override suspend fun getCacheData(key: K): CacheData<V>? {
        return mutex.withLock {
            cache[key]
        }
    }

    override suspend fun getOrSet(key: K, loader: suspend () -> V): V {
        return mutex.withLock {
            val cacheData = cache[key]
            if (cacheData.isInvalid()) {
                loader().also {
                    cache[key] = createCacheData(it)
                }
            } else {
                cacheData.data
            }
        }
    }

    override suspend fun setCacheData(key: K, value: CacheData<V>) {
        mutex.withLock {
            cache[key] = value
        }
    }
}
