package io.github.hansanto.quipoquiz.discord.framework.exception

import kotlinx.coroutines.CancellationException

open class DiscordException(val reason: String) : CancellationException(reason)
