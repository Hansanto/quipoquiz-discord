package io.github.hansanto.quipoquiz.extension

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.job
import kotlinx.coroutines.plus

/**
 * Create a new [CoroutineScope] from the receiver [CoroutineScope] with a [SupervisorJob].
 */
fun CoroutineScope.createChildrenScope(): CoroutineScope {
    return this + SupervisorJob(coroutineContext.job)
}
