package io.github.hansanto.quipoquiz.discord.framework.component.row

import dev.kord.core.event.interaction.ComponentInteractionCreateEvent
import io.github.hansanto.quipoquiz.discord.framework.builder.component.CustomActionRowBuilder
import io.github.hansanto.quipoquiz.discord.framework.component.RowComponent
import kotlinx.coroutines.Job
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Typealias for the action to perform when the component is selected.
 */
private typealias ActionComponent<T> = suspend T.() -> Unit

/**
 * Represents a navigation component of the paginator.
 */
abstract class AbstractRowComponent<T : ComponentInteractionCreateEvent> : RowComponent {

    /**
     * Listener job.
     */
    private var listenerJob: Job? = null

    /**
     * Mutex to synchronize the [listenerJob].
     */
    private val listenerMutex = Mutex()

    /**
     * The action to perform when the component is selected.
     */
    protected var action: ActionComponent<T>? = null

    override suspend fun cancel() {
        listenerMutex.withLock {
            listenerJob?.cancel()
            listenerJob = null
        }
    }

    /**
     * Add an action to perform when the component is selected.
     * @param action The action to perform.
     */
    fun action(action: ActionComponent<T>) {
        this.action = action
    }

    override suspend fun render(builder: CustomActionRowBuilder) {
        renderActionRow(builder)

        listenerMutex.withLock {
            val hasAction = this.action != null

            if (listenerJob?.isActive == true) {
                if (!hasAction) {
                    listenerJob?.cancel()
                }
            } else if (hasAction) {
                listenerJob = createListener()
            }
        }
    }

    /**
     * Render the action row.
     * @param builder The action row builder.
     */
    abstract suspend fun renderActionRow(builder: CustomActionRowBuilder)

    /**
     * Create a listener for the button interaction.
     * @return Listener job.
     */
    protected abstract fun createListener(): Job
}
