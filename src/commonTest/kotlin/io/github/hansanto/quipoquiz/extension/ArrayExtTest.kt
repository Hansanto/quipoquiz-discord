package io.github.hansanto.quipoquiz.extension

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe

class ArrayExtTest : ShouldSpec({

    should("forEachAnyMatch returns false for empty collection") {
        val array = emptyArray<Int>()
        array.forEachAnyMatch { it == 1 } shouldBe false
    }

    should("forEachAnyMatch returns false if no element matches the predicate") {
        val array = arrayOf(1, 2, 3, 4, 5)
        array.forEachAnyMatch { it == 6 } shouldBe false
    }

    should("forEachAnyMatch returns true if at least one element matches the predicate") {
        val array = arrayOf(1, 2, 3, 4, 5)
        array.forEachAnyMatch { it == 3 } shouldBe true
    }
})
