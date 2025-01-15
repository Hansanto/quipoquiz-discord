package io.github.hansanto.quipoquiz.cache.supplier

import io.github.hansanto.quipoquiz.cache.Cache
import io.github.hansanto.quipoquiz.cache.CacheData
import io.github.hansanto.quipoquiz.cache.CacheSupplier
import io.github.hansanto.quipoquiz.extension.delete
import io.github.hansanto.quipoquiz.extension.exists
import io.github.hansanto.quipoquiz.extension.readText
import io.github.hansanto.quipoquiz.extension.resolve
import io.github.hansanto.quipoquiz.extension.writeText
import io.github.hansanto.quipoquiz.util.createTmpDir
import io.github.hansanto.quipoquiz.util.matcher.expirationAfterWrite
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.io.files.Path
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

class MemoryWithFileFallbackTest : ShouldSpec({

    lateinit var tmpDir: Path
    lateinit var cache: Cache<String, String>

    beforeTest {
        tmpDir = createTmpDir()

        cache = CacheSupplier.persistentMemoryWithFileFallback(
            expirationAfterWrite = expirationAfterWrite,
            directory = tmpDir
        )
    }

    afterTest {
        tmpDir.delete()
    }

    should("get returns null if key not exists") {
        val key = "myKey"
        cache.get(key) shouldBe null
    }

    should("get stores in memory the non expired value returned by file") {
        val key = "myKey"
        val value = "Hello World"
        val expiration = Clock.System.now() + 1.seconds
        setCacheFile(tmpDir, key, value, expiration)
        cache.get(key) shouldBe value
        readMemoryCache(tmpDir, key) { cache.get(key) } shouldBe value
    }

    should("get stores in memory the expired value returned by file") {
        val key = "myKey"
        val value = "Hello World"
        val expiration = Clock.System.now() - 1.seconds
        setCacheFile(tmpDir, key, value, expiration)
        cache.get(key) shouldBe value
        deleteCacheFile(tmpDir, key)
        cache.get(key) shouldBe value
    }

    should("getOrSet sets value in memory and file if not exists") {
        val key = "myKey"
        val value = "Hello World"
        cache.getOrSet(key) { value } shouldBe value

        readCacheFile(tmpDir, key)?.data shouldBe value
        readMemoryCache(tmpDir, key) { cache.get(key) } shouldBe value
    }

    should("getOrSet sets value in memory and file if memory value is expired") {
        val key = "myKey"
        val value = "Hello World"
        val expectedValue = "Hello"
        val expiration = Clock.System.now() - 1.seconds

        cache.setCacheData(key, CacheData(value, expiration))
        deleteCacheFile(tmpDir, key)

        cache.getOrSet(key) { expectedValue } shouldBe expectedValue

        readCacheFile(tmpDir, key)?.data shouldBe expectedValue
        readMemoryCache(tmpDir, key) { cache.get(key) } shouldBe expectedValue
    }

    should("getOrSet sets value in memory and file if file value is expired") {
        val key = "myKey"
        val value = "Hello World"
        val expectedValue = "Hello"
        val expiration = Clock.System.now() - 1.seconds
        setCacheFile(tmpDir, key, value, expiration)

        cache.getOrSet(key) { expectedValue } shouldBe expectedValue
        readCacheFile(tmpDir, key)?.data shouldBe expectedValue
        readMemoryCache(tmpDir, key) { cache.get(key) } shouldBe expectedValue
    }

    should("getOrSet not sets value in memory and file if file value is not expired") {
        val key = "myKey"
        val value = "Hello World"
        val expiration = Clock.System.now() + 1.seconds
        setCacheFile(tmpDir, key, value, expiration)

        cache.getOrSet(key) { "Hello" } shouldBe value
        readCacheFile(tmpDir, key)?.data shouldBe value
        readMemoryCache(tmpDir, key) { cache.get(key) } shouldBe value
    }

    should("getOrSet not sets value in memory and file if memory is not expired") {
        val key = "myKey"
        val value = "Hello World"
        val expiration = Clock.System.now() + 1.seconds
        setCacheFile(tmpDir, key, value, expiration)

        cache.get(key) shouldBe value // to load in memory
        deleteCacheFile(tmpDir, key)

        cache.getOrSet(key) { "Hello" } shouldBe value
        readCacheFile(tmpDir, key)?.data shouldBe null
        readMemoryCache(tmpDir, key) { cache.get(key) } shouldBe value
    }

    should("getOrSet sets value is thread-safe") {
        val key = "myKey"
        val expectedValue = "myValue"

        var numberCall = 0
        List(1000) {
            async {
                cache.getOrSet(key) {
                    delay(100.milliseconds)
                    numberCall++
                    expectedValue
                }
            }
        }.awaitAll().forEach {
            it shouldBe expectedValue
        }

        numberCall shouldBe 1
        cache.get(key) shouldBe expectedValue
        readCacheFile(tmpDir, key)?.data shouldBe expectedValue
        readMemoryCache(tmpDir, key) { cache.get(key) } shouldBe expectedValue
    }

    should("getOrSet returns expired value for coroutine that is not the first to set") {
        val key = "myKey"
        val initValue = "initValue"
        val expectedValue = "myValue"
        val expiration = Clock.System.now() - 1.seconds

        cache.setCacheData(key, CacheData(initValue, expiration))

        val mutex = Mutex(locked = true)
        val modifier = async {
            cache.getOrSet(key) {
                mutex.withLock {
                    expectedValue
                }
            }
        }

        List(1000) {
            async {
                cache.getOrSet(key) {
                    expectedValue
                }
            }
        }.awaitAll().forEach {
            it shouldBe initValue
        }

        cache.get(key) shouldBe initValue
        readCacheFile(tmpDir, key)?.data shouldBe initValue
        readMemoryCache(tmpDir, key) { cache.get(key) } shouldBe initValue

        mutex.unlock()
        modifier.await() shouldBe expectedValue

        cache.get(key) shouldBe expectedValue
        readCacheFile(tmpDir, key)?.data shouldBe expectedValue
        readMemoryCache(tmpDir, key) { cache.get(key) } shouldBe expectedValue
    }

    should("set sets value in memory and file if not exists") {
        val key = "myKey"
        val value = "myValue"
        cache.set(key, value)
        readCacheFile(tmpDir, key)?.data shouldBe value
        readMemoryCache(tmpDir, key) { cache.get(key) } shouldBe value
    }

    should("set sets value in memory and file if memory value is expired") {
        val key = "myKey"
        val value = "myValue"
        val expectedValue = "Hello"
        val expiration = Clock.System.now() - 1.seconds
        cache.setCacheData(key, CacheData(value, expiration))
        cache.set(key, expectedValue)
        readCacheFile(tmpDir, key)?.data shouldBe expectedValue
        readMemoryCache(tmpDir, key) { cache.get(key) } shouldBe expectedValue
    }

    should("set sets value in memory and file if memory value is not expired") {
        val key = "myKey"
        val value = "myValue"
        val expectedValue = "Hello"
        val expiration = Clock.System.now() + 1.seconds
        cache.setCacheData(key, CacheData(value, expiration))
        cache.set(key, expectedValue)
        readCacheFile(tmpDir, key)?.data shouldBe expectedValue
        readMemoryCache(tmpDir, key) { cache.get(key) } shouldBe expectedValue
    }

    should("getCacheData returns null if key not exists") {
        val key = "myKey"
        cache.getCacheData(key) shouldBe null
    }

    should("getCacheData stores in memory the value returned by file when it is not expired") {
        val key = "myKey"
        val value = "Hello World"
        val expiration = Clock.System.now() + 1.seconds
        val cacheData = CacheData(value, expiration)
        setCacheFile(tmpDir, key, cacheData)

        cache.getCacheData(key) shouldBe cacheData
        readCacheFile(tmpDir, key) shouldBe cacheData
        readMemoryCache(tmpDir, key) { cache.getCacheData(key) } shouldBe cacheData
    }

    should("getCacheData stores in memory the value returned by fil when it is expired") {
        val key = "myKey"
        val value = "Hello World"
        val expiration = Clock.System.now() - 1.seconds
        val cacheData = CacheData(value, expiration)
        setCacheFile(tmpDir, key, cacheData)

        cache.getCacheData(key) shouldBe cacheData
        readCacheFile(tmpDir, key) shouldBe cacheData
        readMemoryCache(tmpDir, key) { cache.getCacheData(key) } shouldBe cacheData
    }

    should("getCacheData not stores in file if memory value is expired") {
        val key = "myKey"
        val value = "Hello World"
        val expiration = Clock.System.now() - 1.seconds
        val cacheData = CacheData(value, expiration)
        setCacheFile(tmpDir, key, cacheData)

        cache.getCacheData(key) shouldBe cacheData
        deleteCacheFile(tmpDir, key)
        cache.getCacheData(key) shouldBe cacheData
        readCacheFile(tmpDir, key) shouldBe null
        readMemoryCache(tmpDir, key) { cache.getCacheData(key) } shouldBe cacheData
    }

    should("getCacheData not stores in file if memory value is not expired") {
        val key = "myKey"
        val value = "Hello World"
        val expiration = Clock.System.now() + 1.seconds
        val cacheData = CacheData(value, expiration)
        setCacheFile(tmpDir, key, cacheData)

        cache.getCacheData(key) shouldBe cacheData
        deleteCacheFile(tmpDir, key)
        cache.getCacheData(key) shouldBe cacheData
        readCacheFile(tmpDir, key) shouldBe null
        readMemoryCache(tmpDir, key) { cache.getCacheData(key) } shouldBe cacheData
    }

    should("setCacheData sets value in memory and file if not exists") {
        val key = "myKey"
        val value = "myValue"
        val expiration = Clock.System.now() + 1.seconds
        val cacheData = CacheData(value, expiration)
        cache.setCacheData(key, cacheData)
        readCacheFile(tmpDir, key) shouldBe cacheData
        readMemoryCache(tmpDir, key) { cache.getCacheData(key) } shouldBe cacheData
    }

    should("setCacheData sets value in memory and file if memory value is expired") {
        val key = "myKey"
        val value = "myValue"
        val expiration = Clock.System.now() - 1.seconds
        val cacheData = CacheData(value, expiration)
        cache.setCacheData(key, cacheData)
        readCacheFile(tmpDir, key) shouldBe cacheData
        readMemoryCache(tmpDir, key) { cache.getCacheData(key) } shouldBe cacheData
    }

    should("setCacheData sets value in memory and file if memory value is not expired") {
        val key = "myKey"
        val value = "myValue"
        val expiration = Clock.System.now() + 1.seconds
        val cacheData = CacheData(value, expiration)
        cache.setCacheData(key, cacheData)
        readCacheFile(tmpDir, key) shouldBe cacheData
        readMemoryCache(tmpDir, key) { cache.getCacheData(key) } shouldBe cacheData
    }
})

private fun setCacheFile(tmpDir: Path, key: String, value: String, expiration: Instant) {
    setCacheFile(tmpDir, key, CacheData(value, expiration))
}

private fun setCacheFile(tmpDir: Path, key: String, value: CacheData<String>) {
    val path = tmpDir.resolve(key)
    path.writeText(
        Json.encodeToString(
            CacheData.serializer(String.serializer()),
            value
        )
    )
}

private fun readCacheFile(tmpDir: Path, key: String): CacheData<String>? {
    val path = tmpDir.resolve(key)
    if (!path.exists()) return null

    return Json.decodeFromString(
        CacheData.serializer(String.serializer()),
        path.readText()
    )
}

private inline fun <T> readMemoryCache(tmpDir: Path, key: String, get: () -> T): T? {
    val file = tmpDir.resolve(key)
    val memoryData: T?
    if (file.exists()) {
        val content = file.readText()
        file.delete()
        memoryData = get()
        file.writeText(content)
    } else {
        memoryData = get()
    }
    return memoryData
}

private fun deleteCacheFile(tmpDir: Path, key: String) {
    tmpDir.resolve(key).delete()
}
