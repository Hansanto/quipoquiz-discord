package io.github.hansanto.quipoquiz.discord.framework.util

interface TimeoutManager {

    /**
     * Start a timeout to stop the task after a certain time no matter what.
     * @return `true` if the timeout was started, `false` otherwise.
     */
    suspend fun startAliveTimeout(): Boolean

    /**
     * Cancel the alive timeout.
     * @return `true` if the timeout was canceled, `false` otherwise.
     */
    suspend fun cancelAliveTimeout(): Boolean

    /**
     * Start a timeout to stop the task after a certain time of inactivity.
     * @return `true` if the timeout was started, `false` otherwise.
     */
    suspend fun startIdleTimeout(): Boolean

    /**
     * Cancel the idle timeout.
     * @return `true` if the timeout was canceled, `false` otherwise.
     */
    suspend fun cancelIdleTimeout(): Boolean
}
