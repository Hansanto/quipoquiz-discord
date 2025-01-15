package io.github.hansanto.quipoquiz.discord.framework.component.container.builder

import io.github.hansanto.quipoquiz.discord.framework.component.GroupComponent
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Builder to register several [GroupComponent].
 */
interface GroupMessageBuilder {

    /**
     * Add a component to the message.
     * If the component id is already present, it will not be added.
     * @param component Component to add.
     * @return `true` if the component was added, `false` otherwise.
     */
    suspend fun addGroupComponent(component: GroupComponent): Boolean

    /**
     * Remove a component from the message.
     * @param id ID of the component to remove.
     * @return `true` if the component was removed, `false` otherwise.
     */
    suspend fun removeGroupComponent(id: String): Boolean

    /**
     * Check if a component is present.
     * @param id ID of the component to check.
     * @return `true` if the component is present, `false` otherwise.
     */
    suspend fun containsGroupComponent(id: String): Boolean

    /**
     * Get the number of components.
     * @return Number of components.
     */
    suspend fun sizeGroupComponents(): Int

    /**
     * Remove all components.
     */
    suspend fun removeGroupComponents()

    /**
     * Remove all components and cancel them.
     */
    suspend fun removeAndCancelGroupComponents()

    /**
     * Get all components.
     * @return List of components.
     */
    suspend fun getGroupComponents(): List<GroupComponent>
}

class GroupMessageBuilderImpl : GroupMessageBuilder {

    private val components = mutableMapOf<String, GroupComponent>()

    private val componentsMutex = Mutex()

    override suspend fun addGroupComponent(component: GroupComponent): Boolean {
        return componentsMutex.withLock {
            val id = component.id
            if (id in components) {
                return@withLock false
            }

            components[id] = component
            true
        }
    }

    override suspend fun removeGroupComponent(id: String): Boolean {
        return componentsMutex.withLock { components.remove(id) != null }
    }

    override suspend fun removeAndCancelGroupComponents() {
        componentsMutex.withLock {
            components.values.forEach { it.cancel() }
            components.clear()
        }
    }

    override suspend fun containsGroupComponent(id: String): Boolean {
        return componentsMutex.withLock { id in components }
    }

    override suspend fun sizeGroupComponents(): Int {
        return componentsMutex.withLock { components.size }
    }

    override suspend fun removeGroupComponents() {
        componentsMutex.withLock { components.clear() }
    }

    override suspend fun getGroupComponents(): List<GroupComponent> {
        return components.values.toList()
    }
}
