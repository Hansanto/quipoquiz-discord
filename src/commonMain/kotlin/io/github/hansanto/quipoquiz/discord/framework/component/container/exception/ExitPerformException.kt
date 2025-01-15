package io.github.hansanto.quipoquiz.discord.framework.component.container.exception

import kotlinx.coroutines.CancellationException

class ExitPerformException(message: String? = null) : CancellationException(message)
