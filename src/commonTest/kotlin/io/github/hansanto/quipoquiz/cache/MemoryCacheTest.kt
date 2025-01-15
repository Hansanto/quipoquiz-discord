package io.github.hansanto.quipoquiz.cache

import io.github.hansanto.quipoquiz.util.matcher.assertCacheDataWithFlexibleExpiration
import io.github.hansanto.quipoquiz.util.matcher.expirationAfterWrite
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import kotlinx.datetime.Clock
import kotlin.time.Duration.Companion.seconds

class MemoryCacheTest : ShouldSpec({

    lateinit var cache: Cache<String, String>

    beforeTest {
        cache = CacheSupplier.memory(
            expirationAfterWrite = expirationAfterWrite
        )
    }

    should("get returns null if key not exists") {
        val key = "myKey"
        cache.get(key) shouldBe null
    }

    should("get returns null if key exists but expired") {
        val key = "myKey"
        val expiration = Clock.System.now() - 1.seconds
        cache.setCacheData(key, CacheData("myValue", expiration))
        cache.get(key) shouldBe null
    }

    should("get returns value if key exists and not expired") {
        val key = "myKey"
        val expiration = Clock.System.now() + 10.seconds
        cache.setCacheData(key, CacheData("myValue", expiration))
        cache.get(key) shouldBe "myValue"
    }

    should("getOrSet sets value if key not exists") {
        val key = "myKey"
        val expectedValue = "myValue"
        cache.getOrSet(key) { expectedValue } shouldBe expectedValue
        cache.get(key) shouldBe expectedValue
    }

    should("getOrSet returns value if key exists and not expired") {
        val key = "myKey"
        val expectedValue = "myValue"
        val expiration = Clock.System.now() + 10.seconds
        cache.setCacheData(key, CacheData(expectedValue, expiration))
        cache.getOrSet(key) { "newValue" } shouldBe expectedValue
    }

    should("getOrSet sets value if key exists but expired") {
        val key = "myKey"
        val expectedValue = "newValue"
        val expiration = Clock.System.now() - 1.seconds
        cache.setCacheData(key, CacheData("myValue", expiration))
        cache.getOrSet(key) { expectedValue } shouldBe expectedValue
        cache.get(key) shouldBe expectedValue
    }

    should("set sets value") {
        val key = "myKey"
        val expectedValue = "myValue"
        cache.set(key, expectedValue)

        val cacheData = cache.getCacheData(key)!!
        assertCacheDataWithFlexibleExpiration(cacheData, expectedValue)
    }

    should("getCacheData returns null if key not exists") {
        val key = "myKey"
        cache.getCacheData(key) shouldBe null
    }

    should("getCacheData returns value if key exists and not expired") {
        val key = "myKey"
        val expectedValue = "myValue"
        val expiration = Clock.System.now() + 10.seconds
        cache.setCacheData(key, CacheData(expectedValue, expiration))
        cache.getCacheData(key) shouldBe CacheData(expectedValue, expiration)
    }

    should("getCacheData returns value if key exists and expired") {
        val key = "myKey"
        val expectedValue = "myValue"
        val expiration = Clock.System.now() - 1.seconds
        cache.setCacheData(key, CacheData(expectedValue, expiration))
        cache.getCacheData(key) shouldBe CacheData(expectedValue, expiration)
    }
})
