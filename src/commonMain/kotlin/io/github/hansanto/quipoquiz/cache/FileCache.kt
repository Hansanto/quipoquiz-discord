package io.github.hansanto.quipoquiz.cache

import io.github.hansanto.quipoquiz.extension.createDirectories
import io.github.hansanto.quipoquiz.extension.exists
import io.github.hansanto.quipoquiz.extension.readText
import io.github.hansanto.quipoquiz.extension.resolve
import io.github.hansanto.quipoquiz.extension.writeText
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.io.files.Path
import kotlinx.serialization.KSerializer
import kotlinx.serialization.StringFormat
import kotlinx.serialization.json.Json
import kotlin.time.Duration

private val log = KotlinLogging.logger {}

/**
 * Cache to store information in the file system.
 * A file will be created in [directory] with the name of the key.
 * @param V The type of the data to store.
 */
class FileCache<V>(
    /**
     * @see AbstractCache.expirationAfterWrite
     */
    expirationAfterWrite: Duration,
    /**
     * The directory to store the cache files.
     */
    private val directory: Path,
    /**
     * The serializer to serialize and deserialize the data.
     */
    private val serializer: KSerializer<V>,
    /**
     * The format to serialize and deserialize the data.
     */
    private val format: StringFormat = Json
) : AbstractCache<String, V>(expirationAfterWrite) {

    override suspend fun getCacheData(key: String): CacheData<V>? {
        val path = getCachePath(key)
        if (path.exists()) {
            log.debug { "Reading cache from file [$path]" }
            try {
                return format.decodeFromString(CacheData.serializer(serializer), path.readText())
            } catch (e: Exception) {
                log.warn(e) { "Unable to read cache from file [$path]" }
            }
        } else {
            log.debug { "Cache file [$path] does not exist" }
        }
        return null
    }

    override suspend fun setCacheData(key: String, value: CacheData<V>) {
        val path = getCachePath(key)
        log.debug { "Writing cache to file [$path]" }
        directory.createDirectories()
        path.writeText(format.encodeToString(CacheData.serializer(serializer), value))
    }

    /**
     * Get the cache file path.
     * @param name Name of the cache file.
     * @return Path of the file.
     */
    private fun getCachePath(name: String): Path {
        return directory.resolve(name)
    }
}
