package io.github.hansanto.quipoquiz.util.matcher

import io.github.hansanto.quipoquiz.cache.CacheData
import io.kotest.matchers.shouldBe
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.minutes

val expirationAfterWrite: Duration = 1.days

inline fun <reified T> assertCacheDataWithFlexibleExpiration(
    actual: CacheData<T>,
    data: String,
    expiration: Instant = Clock.System.now() + expirationAfterWrite
) {
    actual.data shouldBe data
    actual.expiration shouldBeBetween (expiration - 1.minutes)..(expiration + 1.minutes)
}
