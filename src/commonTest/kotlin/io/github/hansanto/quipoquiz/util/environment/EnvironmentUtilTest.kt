package io.github.hansanto.quipoquiz.util.environment

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.ShouldSpec
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

class EnvironmentUtilTest : ShouldSpec({

    should("int requireGreaterThan min does not throw exception") {
        1.requireGreaterThan(0)
        10.requireGreaterThan(9)
        (-1).requireGreaterThan(-2)
    }

    should("int requireGreaterThan min throws exception") {
        shouldThrow<IllegalArgumentException> {
            0.requireGreaterThan(0)
        }
        shouldThrow<IllegalArgumentException> {
            9.requireGreaterThan(10)
        }
        shouldThrow<IllegalArgumentException> {
            (-2).requireGreaterThan(-1)
        }
    }

    should("duration requireGreaterThan min does not throw exception") {
        (1.seconds).requireGreaterThan(0.seconds)
        (10.seconds).requireGreaterThan(9.seconds)
        (2.milliseconds).requireGreaterThan(1.milliseconds)
    }

    should("duration requireGreaterThan min throws exception") {
        shouldThrow<IllegalArgumentException> {
            (0.seconds).requireGreaterThan(0.seconds)
        }
        shouldThrow<IllegalArgumentException> {
            (9.seconds).requireGreaterThan(10.seconds)
        }
        shouldThrow<IllegalArgumentException> {
            (1.milliseconds).requireGreaterThan(2.milliseconds)
        }
    }
})
