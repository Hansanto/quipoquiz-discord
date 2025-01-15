package io.github.hansanto.quipoquiz.discord.framework.component.container.builder

import io.github.hansanto.quipoquiz.discord.framework.component.EmbedComponent
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Builder to construct only one embed message with multiple components.
 */
interface EmbedMessageBuilder {

    /**
     * Add a component to the message.
     * If the component id is already present, it will not be added.
     * @param component Component to add.
     * @return `true` if the component was added, `false` otherwise.
     */
    suspend fun addEmbedComponent(component: EmbedComponent): Boolean

    /**
     * Remove a component from the message.
     * @param id ID of the component to remove.
     * @return `true` if the component was removed, `false` otherwise.
     */
    suspend fun removeEmbedComponent(id: String): Boolean

    /**
     * Check if a component is present.
     * @param id ID of the component to check.
     * @return `true` if the component is present, `false` otherwise.
     */
    suspend fun containsEmbedComponent(id: String): Boolean

    /**
     * Get the number of components.
     * @return Number of components.
     */
    suspend fun sizeEmbedComponents(): Int

    /**
     * Remove all components.
     */
    suspend fun removeEmbedComponents()

    /**
     * Remove all components and cancel them.
     */
    suspend fun removeAndCancelEmbedComponents()

    /**
     * Remove and cancel a list of components.
     * @param ids List of component IDs to remove.
     * @return `true` if at least one component was removed, `false` otherwise.
     */
    suspend fun removeAndCancelEmbedComponents(ids: Iterable<String>): Boolean

    /**
     * Get all components.
     * @return List of components.
     */
    suspend fun getEmbedComponents(): List<EmbedComponent>
}

class SingleEmbedMessageBuilderImpl : EmbedMessageBuilder {

    private val components = mutableMapOf<String, EmbedComponent>()

    private val componentsMutex = Mutex()

    override suspend fun addEmbedComponent(component: EmbedComponent): Boolean {
        return componentsMutex.withLock {
            val id = component.id
            if (id in components) {
                return@withLock false
            }

            components[id] = component
            true
        }
    }

    override suspend fun removeEmbedComponent(id: String): Boolean {
        return componentsMutex.withLock { components.remove(id) != null }
    }

    override suspend fun containsEmbedComponent(id: String): Boolean {
        return componentsMutex.withLock { id in components }
    }

    override suspend fun sizeEmbedComponents(): Int {
        return componentsMutex.withLock { components.size }
    }

    override suspend fun removeEmbedComponents() {
        componentsMutex.withLock { components.clear() }
    }

    override suspend fun removeAndCancelEmbedComponents() {
        componentsMutex.withLock {
            components.values.forEach { it.cancel() }
            components.clear()
        }
    }

    override suspend fun removeAndCancelEmbedComponents(ids: Iterable<String>): Boolean {
        return componentsMutex.withLock {
            val removed = ids.mapNotNull { components.remove(it) }
            removed.forEach { it.cancel() }
            removed.isNotEmpty()
        }
    }

    override suspend fun getEmbedComponents(): List<EmbedComponent> {
        return components.values.toList()
    }
}
