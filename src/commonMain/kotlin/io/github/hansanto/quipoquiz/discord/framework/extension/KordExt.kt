package io.github.hansanto.quipoquiz.discord.framework.extension

import dev.kord.core.Kord
import dev.kord.core.event.Event
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex

typealias KordLocale = dev.kord.common.Locale

val kordLogger = KotlinLogging.logger("Kord")

/**
 * Convenience method that will invoke the [consumer] on every event [T] created by [Kord.events].
 *
 * The events are buffered in an [unlimited][Channel.UNLIMITED] [buffer][Flow.buffer] and
 * [launched][CoroutineScope.launch] in the supplied [launchInScope], which is [Kord] by default.
 * Each event will be [launched][CoroutineScope.launch] inside the [executionScope] separately and
 * any thrown [Throwable] will be caught and logged.
 *
 * The returned [Job] is a reference to the created coroutine, call [Job.cancel] to cancel the processing of any further
 * events for this [consumer].
 */
inline fun <reified T : Event> Kord.onEvent(
    executionScope: CoroutineScope = this,
    launchInScope: CoroutineScope = this,
    noinline consumer: suspend T.() -> Unit
): Job = events.buffer(Channel.UNLIMITED).filterIsInstance<T>()
    .onEach { event ->
        executionScope
            .launch {
                runCatching {
                    consumer(event)
                }.onFailure(kordLogger::catching)
            }
    }
    .launchIn(launchInScope)

/**
 * Listen for a single event of type [T] and invoke the [consumer] on it.
 * @see onEvent
 */
inline fun <reified T : Event> Kord.onSingleEvent(
    executionScope: CoroutineScope = this,
    launchInScope: CoroutineScope = this,
    noinline consumer: suspend T.() -> Unit
): Job {
    val mutex = Mutex()

    lateinit var job: Job
    job = onEvent<T>(executionScope, launchInScope) {
        if (!mutex.tryLock() || !job.isActive) return@onEvent
        try {
            consumer(this)
        } finally {
            job.cancel()
            mutex.unlock()
        }
    }
    return job
}
