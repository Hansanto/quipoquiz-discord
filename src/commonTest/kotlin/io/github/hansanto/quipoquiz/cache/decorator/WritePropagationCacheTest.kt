package io.github.hansanto.quipoquiz.cache.decorator

import io.github.hansanto.quipoquiz.cache.Cache
import io.github.hansanto.quipoquiz.cache.CacheData
import io.github.hansanto.quipoquiz.cache.CacheSupplier
import io.github.hansanto.quipoquiz.util.matcher.expirationAfterWrite
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import kotlinx.datetime.Clock
import kotlin.time.Duration.Companion.seconds

class WritePropagationCacheTest : ShouldSpec({

    lateinit var main: Cache<String, String>
    lateinit var second: Cache<String, String>
    lateinit var cache: Cache<String, String>

    beforeTest {
        main = CacheSupplier.memory(expirationAfterWrite)
        second = CacheSupplier.memory(expirationAfterWrite)
        cache = main.writePropagation(second)
    }

    should("get returns null if both return null") {
        val key = "myKey"
        cache.get(key) shouldBe null
    }

    should("get returns value if main returns value") {
        val key = "myKey"
        val expectedValue = "myValue"

        main.set(key, expectedValue)

        cache.get(key) shouldBe expectedValue
        main.get(key) shouldBe expectedValue
        second.get(key) shouldBe null
    }

    should("get returns null if second returns value") {
        val key = "myKey"
        val expectedValue = "myValue"

        second.set(key, expectedValue)

        cache.get(key) shouldBe null
        main.get(key) shouldBe null
        second.get(key) shouldBe expectedValue
    }

    should("getOrSet sets value in main and second if both return null") {
        val key = "myKey"
        val expectedValue = "myValue"

        cache.getOrSet(key) { expectedValue } shouldBe expectedValue

        cache.get(key) shouldBe expectedValue
        main.get(key) shouldBe expectedValue
        second.get(key) shouldBe expectedValue
    }

    should("getOrSet sets value in main and second if main does not return value") {
        val key = "myKey"
        val value = "myValue"
        val expectedValue = "newValue"

        second.set(key, value)

        cache.getOrSet(key) { expectedValue } shouldBe expectedValue

        cache.get(key) shouldBe expectedValue
        main.get(key) shouldBe expectedValue
        second.get(key) shouldBe expectedValue
    }

    should("getOrSet returns value from main if main returns value") {
        val key = "myKey"
        val expectedValue = "myValue"

        main.set(key, expectedValue)

        cache.getOrSet(key) { "newValue" } shouldBe expectedValue

        cache.get(key) shouldBe expectedValue
        main.get(key) shouldBe expectedValue
        second.get(key) shouldBe null
    }

    should("set sets value in main and second if both do not have value") {
        val key = "myKey"
        val expectedValue = "myValue"

        cache.set(key, expectedValue)

        cache.get(key) shouldBe expectedValue
        main.get(key) shouldBe expectedValue
        second.get(key) shouldBe expectedValue
    }

    should("set sets value in main and second if both have value") {
        val key = "myKey"
        val value = "myValue"
        val expectedValue = "newValue"

        main.set(key, value)
        second.set(key, value)

        cache.set(key, expectedValue)

        cache.get(key) shouldBe expectedValue
        main.get(key) shouldBe expectedValue
        second.get(key) shouldBe expectedValue
    }

    should("getCacheData returns null if both return null") {
        val key = "myKey"
        cache.getCacheData(key) shouldBe null
    }

    should("getCacheData returns value if main returns value") {
        val key = "myKey"
        val expectedValue = "myValue"
        val expiration = Clock.System.now() + 10.seconds

        val cacheData = CacheData(expectedValue, expiration)
        main.setCacheData(key, cacheData)

        cache.getCacheData(key) shouldBe cacheData
        main.getCacheData(key) shouldBe cacheData
        second.getCacheData(key) shouldBe null
    }

    should("getCacheData returns null if second returns value") {
        val key = "myKey"
        val expectedValue = "myValue"
        val expiration = Clock.System.now() + 10.seconds

        val cacheData = CacheData(expectedValue, expiration)
        second.setCacheData(key, cacheData)

        cache.getCacheData(key) shouldBe null
        main.getCacheData(key) shouldBe null
        second.getCacheData(key) shouldBe cacheData
    }

    should("setCacheData sets value in main and second") {
        val key = "myKey"
        val expectedValue = "myValue"
        val expiration = Clock.System.now() + 10.seconds

        val cacheData = CacheData(expectedValue, expiration)
        cache.setCacheData(key, cacheData)

        cache.getCacheData(key) shouldBe cacheData
        main.getCacheData(key) shouldBe cacheData
        second.getCacheData(key) shouldBe cacheData
    }
})
