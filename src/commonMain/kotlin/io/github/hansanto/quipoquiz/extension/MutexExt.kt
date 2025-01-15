package io.github.hansanto.quipoquiz.extension

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Wait until the mutex is unlocked.
 * @receiver The mutex to wait for.
 */
suspend fun Mutex.waitUntilUnlocked() {
    // If the mutex is locked, we will wait until it is unlocked.
    // Once it is unlocked, we will lock it again and release it immediately.
    withLock {
        // Do nothing
    }
}
