package io.github.hansanto.quipoquiz.cache

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import kotlinx.datetime.Clock
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

class CacheDataTest : ShouldSpec({

    should("isInvalid returns true if the data is null") {
        val cacheData: CacheData<*>? = null
        cacheData.isInvalid() shouldBe true
    }

    should("isInvalid returns true if the data is expired") {
        val cacheData = CacheData("data", Clock.System.now() - 1.seconds)
        cacheData.isInvalid() shouldBe true
    }

    should("isInvalid returns false if the data is not expired") {
        val cacheData = CacheData("data", Clock.System.now() + 1.seconds)
        cacheData.isInvalid() shouldBe false
    }

    should("isExpired returns true if the data is expired") {
        fun checkIsExpired(removeTime: Duration) {
            val beforeNow = Clock.System.now() - removeTime
            val cacheData = CacheData("data", beforeNow)
            cacheData.isExpired() shouldBe true
        }
        checkIsExpired(1.milliseconds)
        checkIsExpired(1.seconds)
        checkIsExpired(1.minutes)
        checkIsExpired(1.hours)
        checkIsExpired(1.days)
    }

    should("isExpired returns false if the data is not expired") {
        fun checkIsNotExpired(addTime: Duration) {
            val afterNow = Clock.System.now() + addTime
            val cacheData = CacheData("data", afterNow)
            cacheData.isExpired() shouldBe false
        }
        checkIsNotExpired(100.milliseconds)
        checkIsNotExpired(1.seconds)
        checkIsNotExpired(1.minutes)
        checkIsNotExpired(1.hours)
        checkIsNotExpired(1.days)
    }
})
