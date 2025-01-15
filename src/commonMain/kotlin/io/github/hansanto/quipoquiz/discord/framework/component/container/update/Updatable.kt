package io.github.hansanto.quipoquiz.discord.framework.component.container.update

import io.github.hansanto.quipoquiz.config.BotConfiguration
import kotlin.time.Duration

interface Updatable {

    /**
     * Start the auto update of the component.
     * @return `true` if the auto update was started, `false` otherwise.
     */
    suspend fun startAutoUpdate(refreshInterval: Duration = BotConfiguration.refreshInterval): Boolean

    /**
     * Stop the auto update of the component.
     * @return `true` if the auto update was stopped, `false` otherwise.
     */
    suspend fun stopAutoUpdate(): Boolean

    /**
     * Update the component.
     * @return `true` if something was updated, `false` otherwise.
     */
    suspend fun update(): Boolean
}
