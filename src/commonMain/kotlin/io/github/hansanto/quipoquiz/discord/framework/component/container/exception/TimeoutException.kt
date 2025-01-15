package io.github.hansanto.quipoquiz.discord.framework.component.container.exception

import kotlinx.coroutines.CancellationException

/**
 * Exception thrown when the component has reached the timeout.
 */
abstract class TimeoutReachedException(message: String? = null) : CancellationException(message)

/**
 * Exception thrown when the component has reached the alive timeout.
 */
class AliveTimeoutException(message: String? = null) : TimeoutReachedException(message)

/**
 * Exception thrown when the idle timeout is reached.
 */
class IdleTimeoutException(message: String? = null) : TimeoutReachedException(message)
