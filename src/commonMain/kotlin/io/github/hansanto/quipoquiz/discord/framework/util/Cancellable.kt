package io.github.hansanto.quipoquiz.discord.framework.util

import kotlinx.coroutines.CancellationException

typealias CancellationHandler = suspend (CancellationException?) -> Unit

interface Cancellable {

    /**
     * Check if the component is alive or not.
     */
    fun isActive(): Boolean

    /**
     * Cancel the container.
     * @param cause Cancellation cause.
     * @return `true` if the container was cancelled successfully, `false` if the container was already cancelled.
     */
    suspend fun cancel(cause: CancellationException? = null): Boolean

    /**
     * Add a cancellation handler to the container.
     * The handler is called when the container is cancelled by calling [cancel] function.
     * @param block Cancellation handler.
     */
    fun onCancel(block: CancellationHandler)
}
