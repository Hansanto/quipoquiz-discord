package io.github.hansanto.quipoquiz.cache

import io.github.hansanto.quipoquiz.cache.decorator.fallback
import io.github.hansanto.quipoquiz.cache.decorator.persistent
import io.github.hansanto.quipoquiz.cache.decorator.readPropagation
import io.github.hansanto.quipoquiz.cache.decorator.writePropagation
import kotlinx.io.files.Path
import kotlinx.serialization.KSerializer
import kotlinx.serialization.StringFormat
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import kotlin.time.Duration

interface CacheSupplier {

    companion object {
        /**
         * Create a cache with memory storage.
         * @param expirationAfterWrite Duration of expiration after the data is written.
         * @return New cache.
         */
        inline fun <reified K, reified V> memory(expirationAfterWrite: Duration): Cache<K, V> =
            MemoryCache(expirationAfterWrite)

        /**
         * Create a cache with file storage.
         * @param expirationAfterWrite Duration of expiration after the data is written.
         * @param directory Directory where the data will be stored.
         * @param format Format to serialize and deserialize the data.
         * @param serializer Serializer to serialize and deserialize the data.
         * @return New cache.
         */
        inline fun <reified V> file(
            expirationAfterWrite: Duration,
            directory: Path,
            format: StringFormat = Json,
            serializer: KSerializer<V> = format.serializersModule.serializer()
        ): Cache<String, V> = FileCache(
            expirationAfterWrite = expirationAfterWrite,
            directory = directory,
            format = format,
            serializer = serializer
        )

        /**
         * Cache with the following strategy:
         * 1. Get in memory
         * 2. If found and not expired, return value
         * 3. If found and expired, return value
         * 4. If not found, get in file
         * 5. If found in file, store in memory
         * 6. If not found in the file, return null
         * 7. When set data, set in memory and file
         * @param expirationAfterWrite Duration of expiration after the data is written.
         * @param directory Directory where the data will be stored.
         * @param format Format to serialize and deserialize the data.
         * @param serializer Serializer to serialize and deserialize the data.
         * @return New cache.
         */
        inline fun <reified V> persistentMemoryWithFileFallback(
            expirationAfterWrite: Duration,
            directory: Path,
            format: StringFormat = Json,
            serializer: KSerializer<V> = format.serializersModule.serializer()
        ): Cache<String, V> = memoryWithFileFallback(
            expirationAfterWrite = expirationAfterWrite,
            directory = directory,
            format = format,
            serializer = serializer
        ).persistent()

        /**
         * Cache with the following strategy:
         * 1. Get in memory
         * 2. If found and not expired, return value
         * 3. If found and expired, get in file
         * 4. If not found, get in file
         * 5. If found in file, store in memory
         * 6. If not found in the file, return null
         * 7. When set data, set in memory and file
         * @param expirationAfterWrite Duration of expiration after the data is written.
         * @param directory Directory where the data will be stored.
         * @param format Format to serialize and deserialize the data.
         * @param serializer Serializer to serialize and deserialize the data.
         * @return New cache.
         */
        inline fun <reified V> memoryWithFileFallback(
            expirationAfterWrite: Duration,
            directory: Path,
            format: StringFormat = Json,
            serializer: KSerializer<V> = format.serializersModule.serializer()
        ): Cache<String, V> {
            val memory = memory<String, V>(
                expirationAfterWrite = expirationAfterWrite
            )
            val file = file(
                expirationAfterWrite = expirationAfterWrite,
                directory = directory,
                format = format,
                serializer = serializer
            )
            // 1. & 2. & 6.
            return memory
                // 7.
                .writePropagation(file)
                // 3.
                .fallback(
                    // 5.
                    file.readPropagation(memory)
                )
        }
    }
}
