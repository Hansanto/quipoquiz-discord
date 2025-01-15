package io.github.hansanto.quipoquiz.cache.decorator

import io.github.hansanto.quipoquiz.cache.AbstractCache
import io.github.hansanto.quipoquiz.cache.Cache
import io.github.hansanto.quipoquiz.cache.CacheData
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Create a new cache where the expired data from this cache is not invalidated.
 * @receiver Cache to be made persistent.
 * @return New cache.
 */
fun <K, V> Cache<K, V>.persistent(): Cache<K, V> {
    return PersistentCache(this)
}

/**
 * Cache where the expired data is not invalidated.
 * @param V Type of data to be cached.
 */
class PersistentCache<K, V>(
    /**
     * Cache where the data will be got and set.
     */
    private val cache: Cache<K, V>
) : AbstractCache<K, V>(cache.expirationAfterWrite) {

    /**
     * Mutex to control the access to [mutexes].
     */
    private val mapMutex = Mutex()

    /**
     * Mutexes to control the access to the data fetching.
     */
    private val mutexes = mutableMapOf<K, Mutex>()

    override suspend fun get(key: K): V? {
        return cache.getCacheData(key)?.data
    }

    override suspend fun getOrSet(key: K, loader: suspend () -> V): V {
        val tmpData = getCacheData(key)
        val mutex = getMutex(key)
        if (tmpData == null) {
            return if (mutex.tryLock()) {
                // Only one coroutine should fetch the data
                loadAndUnlockMutex(mutex, loader).also { set(key, it) }
            } else {
                // For another coroutine that wants data during fetching, wait for the data to be fetched
                mutex.withLock {
                    requireNotNull(get(key)) {
                        "Data must be cached after fetching but it is null"
                    }
                }
            }
        }

        // If data is not null, and not expired, return the data
        // If data is not null, and expired, if mutex lock is not acquired, return the data
        return if (tmpData.isExpired() && mutex.tryLock()) {
            // If data is not null, and expired, if mutex lock is acquired, fetch the data
            loadAndUnlockMutex(mutex, loader).also { set(key, it) }
        } else {
            tmpData.data
        }
    }

    override suspend fun getCacheData(key: K): CacheData<V>? {
        return cache.getCacheData(key)
    }

    override suspend fun setCacheData(key: K, value: CacheData<V>) {
        cache.setCacheData(key, value)
    }

    /**
     * Get the mutex for the key.
     * If the mutex is not found, create a new mutex.
     * @param key Key associated with the data.
     * @return Mutex for the key.
     */
    private suspend fun getMutex(key: K): Mutex {
        return mapMutex.withLock { mutexes.getOrPut(key) { Mutex() } }
    }

    /**
     * Load the data and unlock the mutex.
     * @param mutex Mutex to unlock when the data is fetched.
     * @param loader Loader to get the data.
     * @return The instance of the data got from the loader.
     */
    private suspend inline fun loadAndUnlockMutex(mutex: Mutex, crossinline loader: suspend () -> V): V {
        return try {
            loader()
        } finally {
            mutex.unlock()
        }
    }
}
