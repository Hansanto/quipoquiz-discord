package io.github.hansanto.quipoquiz.extension

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex

class MutexExtTest : ShouldSpec({

    should("wait until unlocked") {
        val mutex = Mutex()
        mutex.lock()

        val job = launch {
            mutex.waitUntilUnlocked()
        }

        delay(50)
        job.isActive shouldBe true
        job.isCompleted shouldBe false

        mutex.unlock()

        delay(50)
        job.isCompleted shouldBe true
        job.isActive shouldBe false
    }
})
