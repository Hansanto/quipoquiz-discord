package io.github.hansanto.quipoquiz.cache

import io.github.hansanto.quipoquiz.extension.delete
import io.github.hansanto.quipoquiz.extension.exists
import io.github.hansanto.quipoquiz.extension.readText
import io.github.hansanto.quipoquiz.extension.resolve
import io.github.hansanto.quipoquiz.extension.writeText
import io.github.hansanto.quipoquiz.util.createTmpDir
import io.github.hansanto.quipoquiz.util.matcher.assertCacheDataWithFlexibleExpiration
import io.github.hansanto.quipoquiz.util.matcher.expirationAfterWrite
import io.kotest.assertions.json.shouldEqualJson
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.io.files.Path
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.seconds

class FileCacheTest : ShouldSpec({

    lateinit var cache: Cache<String, String>

    lateinit var tmpDir: Path

    beforeTest {
        tmpDir = createTmpDir()

        cache = CacheSupplier.file(
            expirationAfterWrite = expirationAfterWrite,
            directory = tmpDir,
            serializer = String.serializer(),
            format = Json
        )
    }

    afterTest {
        tmpDir.delete()
    }

    should("get returns null if file not exists") {
        val key = "myFile"
        val path = tmpDir.resolve(key)
        path.exists() shouldBe false
        cache.get(key) shouldBe null
    }

    should("get returns null if file contains invalid data") {
        val key = "myFile"
        val path = tmpDir.resolve(key)
        path.writeText("Hello World")
        cache.get(key) shouldBe null
    }

    should("get returns null if file exists but expired") {
        val key = "myFile"
        val path = tmpDir.resolve(key)

        val expiration = Clock.System.now() - 1.seconds
        cache.setCacheData(key, CacheData("Hello World", expiration))
        path.exists() shouldBe true

        cache.get(key) shouldBe null
        // Verifies that the file is not deleted
        path.exists() shouldBe true
    }

    should("get returns value if file exists and not expired") {
        val key = "myFile"
        val expectedValue = "Hello World"
        val path = tmpDir.resolve(key)

        val expiration = Clock.System.now() + 1.hours
        cache.setCacheData(key, CacheData(expectedValue, expiration))
        path.exists() shouldBe true

        cache.get(key) shouldBe expectedValue
    }

    should("getOrSet sets value if file not exists") {
        val key = "myFile"
        val expectedValue = "Hello"

        val path = tmpDir.resolve(key)
        path.exists() shouldBe false

        cache.getOrSet(key) { expectedValue } shouldBe expectedValue
        path.exists() shouldBe true

        val cacheData = readCacheData(path)
        assertCacheDataWithFlexibleExpiration(cacheData, expectedValue)
    }

    should("getOrSet returns stored value if file exists and not expired") {
        val key = "myFile"
        val expectedValue = "Hello World"
        val path = tmpDir.resolve(key)

        val expiration = Clock.System.now() + 1.hours
        cache.setCacheData(key, CacheData(expectedValue, expiration))
        path.exists() shouldBe true

        // Does nothing because the file is not expired
        cache.getOrSet(key) { "Hello" } shouldBe expectedValue

        readCacheData(path) shouldBe CacheData(
            expectedValue,
            expiration
        )
    }

    should("getOrSet sets value if file exists but expired") {
        val key = "myFile"
        val expectedValue = "Hello"
        val path = tmpDir.resolve(key)

        val expiration = Clock.System.now() - 1.seconds
        cache.setCacheData(key, CacheData("Hello World", expiration))
        path.exists() shouldBe true

        // Modifies the file because it is expired
        cache.getOrSet(key) { expectedValue } shouldBe expectedValue
        path.exists() shouldBe true

        val cacheData = readCacheData(path)
        assertCacheDataWithFlexibleExpiration(cacheData, expectedValue)
    }

    should("set writes in the file using key value if file not exists") {
        val key = "myFile"
        val expectedValue = "Hello"

        val path = tmpDir.resolve(key)
        path.exists() shouldBe false

        cache.set(key, expectedValue)
        path.exists() shouldBe true

        val cacheData = readCacheData(path)
        assertCacheDataWithFlexibleExpiration(cacheData, expectedValue)
    }

    should("set writes in the file using key value if file exists but expired") {
        val key = "myFile"
        val expectedValue = "Hello"
        val path = tmpDir.resolve(key)

        val expiration = Clock.System.now() - 1.seconds
        cache.setCacheData(key, CacheData("Hello World", expiration))
        path.exists() shouldBe true

        cache.set(key, expectedValue)
        path.exists() shouldBe true

        val cacheData = readCacheData(path)
        assertCacheDataWithFlexibleExpiration(cacheData, expectedValue)
    }

    should("set writes in the file using key value if file exists and not expired") {
        val key = "myFile"
        val expectedValue = "Hello"
        val path = tmpDir.resolve(key)

        val expiration = Clock.System.now() + 1.hours
        cache.setCacheData(key, CacheData("Hello World", expiration))
        path.exists() shouldBe true

        cache.set(key, expectedValue)
        path.exists() shouldBe true

        val cacheData = readCacheData(path)
        assertCacheDataWithFlexibleExpiration(cacheData, expectedValue)
    }

    should("getCacheData returns null if file not exists") {
        val key = "myFile"
        val path = tmpDir.resolve(key)
        path.exists() shouldBe false

        cache.getCacheData(key) shouldBe null
    }

    should("getCacheData returns the data if file exists and is expired") {
        getCacheDataAlwaysReturnsValue(tmpDir, cache, Clock.System.now() - 1.seconds)
    }

    should("getCacheData returns the data if file exists and is not expired") {
        getCacheDataAlwaysReturnsValue(tmpDir, cache, Clock.System.now() + 1.hours)
    }

    should("setCacheData writes in the file using key value if file not exists") {
        val key = "myFile"
        val path = tmpDir.resolve(key)
        createTmpCacheDataAndVerifyFileContent(cache, path, key, "Hello World", Clock.System.now())
    }

    should("setCacheData writes in the file using key value if file exists but expired") {
        setCacheDataAlwaysWritesValue(tmpDir, cache, Clock.System.now() - 1.hours)
    }

    should("setCacheData writes in the file using key value if file exists and not expired") {
        setCacheDataAlwaysWritesValue(tmpDir, cache, Clock.System.now() + 1.hours)
    }
})

private suspend fun setCacheDataAlwaysWritesValue(tmpDir: Path, cache: Cache<String, String>, expiration: Instant) {
    val key = "myFile"

    val path = tmpDir.resolve(key)
    cache.setCacheData(key, CacheData("Hello World", expiration))
    path.exists() shouldBe true

    createTmpCacheDataAndVerifyFileContent(cache, path, key, "Hello", Instant.fromEpochMilliseconds(0))
}

private suspend fun getCacheDataAlwaysReturnsValue(tmpDir: Path, cache: Cache<String, String>, expiration: Instant) {
    val key = "myFile"
    val path = tmpDir.resolve(key)

    cache.setCacheData(key, CacheData("Hello World", expiration))
    path.exists() shouldBe true

    val cacheData = cache.getCacheData(key)
    cacheData shouldBe CacheData(
        "Hello World",
        expiration
    )
}

private suspend fun createTmpCacheDataAndVerifyFileContent(
    cache: Cache<String, String>,
    path: Path,
    key: String,
    value: String,
    expiration: Instant
) {
    val newCacheData = CacheData(value, expiration)
    cache.setCacheData(key, newCacheData)

    path.exists() shouldBe true
    path.readText() shouldEqualJson Json.encodeToString(
        CacheData.serializer(String.serializer()),
        newCacheData
    )
}

private fun readCacheData(path: Path): CacheData<String> {
    return Json.decodeFromString(
        CacheData.serializer(String.serializer()),
        path.readText()
    )
}
