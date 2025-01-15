package io.github.hansanto.quipoquiz.util.matcher

import io.kotest.matchers.longs.between
import io.kotest.matchers.shouldBe
import kotlinx.datetime.Instant

infix fun Instant.shouldBeBetween(range: ClosedRange<Instant>) {
    this.toEpochMilliseconds() shouldBe between(
        range.start.toEpochMilliseconds(),
        range.endInclusive.toEpochMilliseconds()
    )
}
