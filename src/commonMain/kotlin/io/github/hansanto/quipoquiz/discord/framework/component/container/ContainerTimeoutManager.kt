package io.github.hansanto.quipoquiz.discord.framework.component.container

import dev.kord.core.Kord
import io.github.hansanto.quipoquiz.discord.framework.component.container.exception.AliveTimeoutException
import io.github.hansanto.quipoquiz.discord.framework.component.container.exception.IdleTimeoutException
import io.github.hansanto.quipoquiz.discord.framework.util.TimeoutManager
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.time.Duration

class ContainerTimeoutManager(
    val kord: Kord,
    /**
     * Coroutine scope to launch the timeout jobs.
     */
    val container: Container,
    /**
     * Duration to wait before cancelling the container.
     * The container will be cancelled after this duration no matter if it's often updated.
     */
    val maxAliveTimeout: Duration,
    /**
     * Duration to wait before cancelling the container after the last update.
     * The container will be cancelled after this duration if it's not updated.
     */
    val maxIdleTimeout: Duration
) : TimeoutManager {

    private var aliveTimeoutJob: Job? = null

    private var idleTimeoutJob: Job? = null

    private val mutex = Mutex()

    override suspend fun startAliveTimeout(): Boolean {
        mutex.withLock {
            if (aliveTimeoutJob?.isActive == true) {
                return false
            }
            aliveTimeoutJob = container.launch {
                delay(maxAliveTimeout)
                cancelContainer(AliveTimeoutException())
            }.let { return it.isActive }
        }
    }

    override suspend fun cancelAliveTimeout(): Boolean {
        mutex.withLock {
            aliveTimeoutJob?.let {
                if (it.isActive) {
                    it.cancelAndJoin()
                    return true
                }
            }
            return false
        }
    }

    override suspend fun startIdleTimeout(): Boolean {
        mutex.withLock {
            if (idleTimeoutJob?.isActive == true) {
                return false
            }
            idleTimeoutJob = container.launch {
                delay(maxIdleTimeout)
                cancelContainer(IdleTimeoutException())
            }.let { return it.isActive }
        }
    }

    override suspend fun cancelIdleTimeout(): Boolean {
        mutex.withLock {
            idleTimeoutJob?.let {
                if (it.isActive) {
                    it.cancelAndJoin()
                    return true
                }
            }
            return false
        }
    }

    /**
     * Cancel the container with the given [cause].
     * @param cause Cancellation cause.
     */
    private fun cancelContainer(cause: CancellationException) {
        // Necessary to cancel the container
        // Because the container can be updated after the cancel
        // Consequently, we can't use the coroutine used for the timeout
        kord.launch {
            container.cancel(cause)
        }
    }
}
