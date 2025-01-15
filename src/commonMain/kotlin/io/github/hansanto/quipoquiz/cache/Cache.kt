package io.github.hansanto.quipoquiz.cache

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import kotlin.contracts.contract
import kotlin.time.Duration

/**
 * Check if the cache data is invalid.
 * @receiver Cache data to be checked.
 * @return `true` if the cache data is invalid, `false` otherwise.
 */
fun CacheData<*>?.isInvalid(): Boolean {
    contract {
        returns(false) implies (this@isInvalid != null)
    }
    return this == null || isExpired()
}

/**
 * Represents temporary cache data with expiration time.
 * @param T Type of data to be cached.
 */
@Serializable
data class CacheData<T>(
    /**
     * Data to be cached.
     */
    val data: T,
    /**
     * Moment when the data will be expired.
     */
    val expiration: Instant
) {

    /**
     * Check if the data is expired.
     * @return `true` if the data is expired, `false` otherwise.
     */
    fun isExpired(): Boolean {
        return expiration <= Clock.System.now()
    }
}

/**
 * Create cache data.
 * The expiration time will be calculated based on the current time and the [Cache.expirationAfterWrite]
 * @receiver Cache with the expiration time.
 * @param value Data to be cached.
 * @return The instance of the cache data.
 */
fun <V> Cache<*, V>.createCacheData(value: V): CacheData<V> {
    return CacheData(
        data = value,
        expiration = Clock.System.now() + expirationAfterWrite
    )
}

interface Cache<K, V> {

    /**
     * Duration of expiration after the data is written.
     */
    val expirationAfterWrite: Duration

    /**
     * Get the data from the cache.
     * @param key Key associated with the data.
     * @return The instance of the data if exists and valid, `null` otherwise.
     */
    suspend fun get(key: K): V?

    /**
     * Get the data from the cache.
     * If the key is not associated with any data, the loader will be called and the result will be cached.
     * @param key Key associated with the data.
     * @param loader Loader to get the data if not exists.
     * @return The instance of the data got from the cache or loader.
     */
    suspend fun getOrSet(key: K, loader: suspend () -> V): V

    /**
     * Set the data to the cache.
     * @param key Key associated with the data.
     * @param value Data to be set.
     */
    suspend fun set(key: K, value: V)

    /**
     * Get the cache data.
     * @param key Key associated with the data.
     * @return The instance of the cache data if exists, `null` otherwise.
     */
    suspend fun getCacheData(key: K): CacheData<V>?

    /**
     * Set the cache data.
     * @param key Key associated with the data.
     * @param value Cache data to be set.
     */
    suspend fun setCacheData(key: K, value: CacheData<V>)
}

abstract class AbstractCache<K, V>(
    override val expirationAfterWrite: Duration
) : Cache<K, V> {

    override suspend fun get(key: K): V? {
        val cacheData = getCacheData(key)
        return if (cacheData.isInvalid()) {
            null
        } else {
            cacheData.data
        }
    }

    override suspend fun getOrSet(key: K, loader: suspend () -> V): V {
        return get(key) ?: loader().also { set(key, it) }
    }

    override suspend fun set(key: K, value: V) {
        setCacheData(key, createCacheData(value))
    }
}
