package io.github.hansanto.quipoquiz.extension

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe

class CollectionExtTest : ShouldSpec({

    should("containsExactly returns true if both collections are empty") {
        val list1 = emptyList<Int>()
        val list2 = emptyList<Int>()
        list1.containsExactly(list2) shouldBe true
    }

    should("containsExactly returns false if collections have different sizes") {
        val list1 = listOf(1, 2, 3)
        val list2 = listOf(1, 2)
        list1.containsExactly(list2) shouldBe false
    }

    should("containsExactly returns false if collections have different elements") {
        val list1 = listOf(1, 2, 3)
        val list2 = listOf(1, 2, 4)
        list1.containsExactly(list2) shouldBe false
    }

    should("containsExactly returns true if collections have the same elements") {
        val list1 = listOf(1, 2, 3)
        val list2 = listOf(1, 2, 3)
        list1.containsExactly(list2) shouldBe true
    }

    should("containsExactly returns true if collections have the same elements in different order") {
        val list1 = listOf(1, 2, 3)
        val list2 = listOf(3, 2, 1)
        list1.containsExactly(list2) shouldBe true
    }
})
