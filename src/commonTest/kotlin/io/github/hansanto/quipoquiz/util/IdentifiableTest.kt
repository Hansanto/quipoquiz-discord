package io.github.hansanto.quipoquiz.util

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe

class IdentifiableTest : ShouldSpec({

    should("create id use parent and child id") {
        val parentId = "parent"
        val childId = "child"
        val expectedId = "parent-child"
        val actualId = createId(parentId, childId)
        actualId shouldBe expectedId
    }
})
