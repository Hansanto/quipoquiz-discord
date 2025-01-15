package io.github.hansanto.quipoquiz.discord.framework.component.container.exception

import kotlinx.coroutines.CancellationException

class MessageDeletedException(message: String? = null) : CancellationException(message)
