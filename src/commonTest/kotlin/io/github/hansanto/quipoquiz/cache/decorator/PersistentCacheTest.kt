package io.github.hansanto.quipoquiz.cache.decorator

import io.github.hansanto.quipoquiz.cache.Cache
import io.github.hansanto.quipoquiz.cache.CacheData
import io.github.hansanto.quipoquiz.cache.CacheSupplier
import io.github.hansanto.quipoquiz.util.matcher.expirationAfterWrite
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.datetime.Clock
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

class PersistentCacheTest : ShouldSpec({

    lateinit var main: Cache<String, String>
    lateinit var cache: Cache<String, String>

    beforeTest {
        main = CacheSupplier.memory(expirationAfterWrite)
        cache = main.persistent()
    }

    should("get returns null if main does not have value") {
        val key = "myKey"
        cache.get(key) shouldBe null
    }

    should("get returns value if main have non expired value") {
        val key = "myKey"
        val expectedValue = "myValue"

        main.set(key, expectedValue)

        cache.get(key) shouldBe expectedValue
        main.get(key) shouldBe expectedValue
    }

    should("get returns value if main have expired value") {
        val key = "myKey"
        val expectedValue = "myValue"
        val expiration = Clock.System.now() - 1.seconds

        main.setCacheData(key, CacheData(expectedValue, expiration))

        cache.get(key) shouldBe expectedValue
        main.get(key) shouldBe null
    }

    should("getOrSet sets value in main if main does not have value") {
        val key = "myKey"
        val expectedValue = "myValue"

        cache.getOrSet(key) { expectedValue } shouldBe expectedValue

        cache.get(key) shouldBe expectedValue
        main.get(key) shouldBe expectedValue
    }

    should("getOrSet sets value in main if main have expired value") {
        val key = "myKey"
        val expectedValue = "myValue"
        val expiration = Clock.System.now() - 1.seconds

        main.setCacheData(key, CacheData(expectedValue, expiration))

        cache.getOrSet(key) { expectedValue } shouldBe expectedValue

        cache.get(key) shouldBe expectedValue
        main.get(key) shouldBe expectedValue
    }

    should("getOrSet sets value in main is thread-safe") {
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
        main.get(key) shouldBe expectedValue
    }

    should("getOrSet returns expired value for coroutine that is not the first to set") {
        val key = "myKey"
        val initValue = "initValue"
        val expectedValue = "myValue"
        val expiration = Clock.System.now() - 1.seconds

        main.setCacheData(key, CacheData(initValue, expiration))

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
        main.get(key) shouldBe null

        mutex.unlock()
        modifier.await() shouldBe expectedValue

        cache.get(key) shouldBe expectedValue
        main.get(key) shouldBe expectedValue
    }

    should("set sets value in main") {
        val key = "myKey"
        val expectedValue = "myValue"

        cache.set(key, expectedValue)

        cache.get(key) shouldBe expectedValue
        main.get(key) shouldBe expectedValue
    }

    should("getCacheData returns null if main does not have value") {
        val key = "myKey"
        cache.getCacheData(key) shouldBe null
    }

    should("getCacheData returns value if main have non expired value") {
        val key = "myKey"
        val expectedValue = "myValue"
        val expiration = Clock.System.now() + 10.seconds

        val cacheData = CacheData(expectedValue, expiration)
        main.setCacheData(key, cacheData)

        cache.getCacheData(key) shouldBe cacheData
    }

    should("getCacheData returns value if main have expired value") {
        val key = "myKey"
        val expectedValue = "myValue"
        val expiration = Clock.System.now() - 1.seconds

        val cacheData = CacheData(expectedValue, expiration)
        main.setCacheData(key, cacheData)

        cache.getCacheData(key) shouldBe cacheData
        main.getCacheData(key) shouldBe cacheData
    }
})
