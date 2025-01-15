package io.github.hansanto.quipoquiz.extension

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.job
import kotlin.coroutines.EmptyCoroutineContext

class CoroutineScopeExtTest : ShouldSpec({

    should("create children scope") {
        val parentScope = CoroutineScope(EmptyCoroutineContext)
        val childrenScope = parentScope.createChildrenScope()
        parentScope.coroutineContext.job.children.contains(childrenScope.coroutineContext.job) shouldBe true
    }
})
