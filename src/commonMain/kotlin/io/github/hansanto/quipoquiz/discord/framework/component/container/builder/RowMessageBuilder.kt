package io.github.hansanto.quipoquiz.discord.framework.component.container.builder

import io.github.hansanto.quipoquiz.discord.framework.component.RowComponent
import io.github.hansanto.quipoquiz.discord.framework.component.container.builder.MultipleRowMessageBuilder.Companion.DISCORD_MAX_ROW
import io.github.hansanto.quipoquiz.discord.framework.component.container.builder.SingleRowMessageBuilder.Companion.DISCORD_WIDTH_ROW
import io.github.hansanto.quipoquiz.extension.forEachAnyMatch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class RowOverflowException : Exception("Row is full")

/**
 * Builder to construct action row (button, select menu, etc.) with multiple components.
 */
interface RowMessageBuilder {

    companion object {

        /**
         * Index of the first row.
         */
        const val FIRST_ROW = 0

        /**
         * Index of the second row.
         */
        const val SECOND_ROW = 1

        /**
         * Index of the third row.
         */
        const val THIRD_ROW = 2

        /**
         * Index of the fourth row.
         */
        const val FOURTH_ROW = 3
    }

    /**
     * Add a component to the row.
     * If the component id is already present, it will not be added.
     * @param component Component to add.
     * @return `true` if the component was added, `false` otherwise.
     */
    suspend fun addRowComponent(component: RowComponent): Boolean

    /**
     * Remove a component from the row.
     * @param id ID of the component to remove.
     * @return `true` if the component was removed, `false` otherwise.
     */
    suspend fun removeRowComponent(id: String): Boolean

    /**
     * Remove multiple components from the row.
     * @param ids IDs of the components to remove.
     * @return `true` if at least one component was removed, `false` otherwise.
     */
    suspend fun removeRowComponents(ids: Iterable<String>): Boolean

    /**
     * Remove multiple components from the row.
     * For each component removed, the component will be cancelled.
     * If a component is not present, it will be ignored.
     * @param ids IDs of the components to remove.
     * @return `true` if at least one component was removed, `false` otherwise.
     */
    suspend fun removeAndCancelRowComponents(ids: Iterable<String>): Boolean

    /**
     * Check if a component is present.
     * @param id ID of the component to check.
     * @return `true` if the component is present, `false` otherwise.
     */
    suspend fun containsRowComponent(id: String): Boolean

    /**
     * Get the number of components.
     * @return Number of components.
     */
    suspend fun sizeRowComponents(): Int

    /**
     * Remove all components.
     */
    suspend fun removeRowComponents()

    /**
     * Remove all components and cancel them.
     */
    suspend fun removeAndCancelRowComponents(): Boolean

    /**
     * Get all components.
     * @return List of components.
     */
    suspend fun getRowComponents(): List<RowComponent>
}

/**
 * Builder to construct a single action row (button, select menu, etc.) with multiple components.
 */
interface SingleRowMessageBuilder : RowMessageBuilder {

    companion object {
        /**
         * Width of a row.
         */
        const val DISCORD_WIDTH_ROW = 5
    }

    /**
     * Maximum width of the row.
     */
    val maxWidth: Int

    /**
     * Add a component to the row at a specific index.
     * If the component id is already present, it will not be added.
     * @param component Component to add.
     * @param index Index where the component should be added.
     * @return `true` if the component was added, `false` otherwise.
     */
    suspend fun addRowComponent(component: RowComponent, index: Int): Boolean

    /**
     * Get the remaining width of the row.
     * @return Remaining width.
     */
    suspend fun remainingWidth(): Int

    /**
     * Check if the row has enough width for a component.
     * @param component Component to check.
     * @return `true` if the row has enough width, `false` otherwise.
     */
    suspend fun hasWidthFor(component: RowComponent): Boolean {
        return remainingWidth() >= component.width
    }
}

class SingleRowMessageBuilderImpl(override val maxWidth: Int = DISCORD_WIDTH_ROW) : SingleRowMessageBuilder {

    private val components = ArrayList<RowComponent>(maxWidth)

    private val componentsMutex = Mutex()

    override suspend fun addRowComponent(component: RowComponent): Boolean {
        return componentsMutex.withLock {
            if (unsafeContainsRowComponent(component.id)) {
                return false
            }
            unsafeAssertHasWidthFor(component)

            this.components.add(component)
            true
        }
    }

    override suspend fun addRowComponent(component: RowComponent, index: Int): Boolean {
        return componentsMutex.withLock {
            if (unsafeContainsRowComponent(component.id)) {
                return@withLock false
            }
            unsafeAssertHasWidthFor(component)

            when {
                index < 0 -> this.components.add(0, component)
                index >= components.size -> this.components.add(component)
                else -> this.components.add(index, component)
            }

            true
        }
    }

    override suspend fun removeRowComponent(id: String): Boolean {
        return componentsMutex.withLock {
            components.removeAll { it.id == id }
        }
    }

    override suspend fun removeRowComponents(ids: Iterable<String>): Boolean {
        return componentsMutex.withLock {
            components.removeAll { it.id in ids }
        }
    }

    override suspend fun removeAndCancelRowComponents(ids: Iterable<String>): Boolean {
        return componentsMutex.withLock {
            val componentsPresent = components.filter { it.id in ids }.onEach { it.cancel() }
            components.removeAll(componentsPresent)
        }
    }

    override suspend fun removeRowComponents() {
        componentsMutex.withLock {
            components.clear()
        }
    }

    override suspend fun removeAndCancelRowComponents(): Boolean {
        componentsMutex.withLock {
            val hasComponents = components.isNotEmpty()
            components.forEach { it.cancel() }
            components.clear()
            return hasComponents
        }
    }

    override suspend fun containsRowComponent(id: String): Boolean {
        return componentsMutex.withLock {
            unsafeContainsRowComponent(id)
        }
    }

    private fun unsafeContainsRowComponent(id: String): Boolean {
        return components.any { it.id == id }
    }

    override suspend fun sizeRowComponents(): Int {
        return componentsMutex.withLock {
            components.size
        }
    }

    override suspend fun getRowComponents(): List<RowComponent> {
        return components.toList()
    }

    override suspend fun remainingWidth(): Int {
        return componentsMutex.withLock {
            unsafeRemainingWidth()
        }
    }

    /**
     * Get the remaining width of the row.
     * @return Remaining width.
     */
    private fun unsafeRemainingWidth() = maxWidth - components.sumOf { it.width }

    /**
     * Check if the component can be added to the row.
     * If the component does not fit, an [RowOverflowException] will be thrown.
     * @receiver Row builder.
     * @param component Component that will be added.
     */
    private fun unsafeAssertHasWidthFor(component: RowComponent) {
        if (!unsafeHasWidthFor(component)) {
            throw RowOverflowException()
        }
    }

    /**
     * Check if the row has enough width for a component.
     * @param component Component to check.
     * @return `true` if the row has enough width, `false` otherwise.
     */
    private fun unsafeHasWidthFor(component: RowComponent): Boolean {
        return unsafeRemainingWidth() >= component.width
    }
}

/**
 * Builder to construct multiple action rows (button, select menu, etc.) with multiple components.
 */
interface MultipleRowMessageBuilder : RowMessageBuilder {

    companion object {
        /**
         * Width of a row.
         */
        const val DISCORD_MAX_ROW = 5

        /**
         * Last row index.
         */
        const val LAST_ROW_INDEX = DISCORD_MAX_ROW - 1
    }

    /**
     * Number of rows.
     */
    val maxNumberOfRows: Int

    /**
     * Add a component to the row at a specific index.
     * If the component id is already present, it will not be added.
     * @param component Component to add.
     * @param row Index where the component should be added.
     * @return `true` if the component was added, `false` otherwise.
     */
    suspend fun addRowComponent(component: RowComponent, row: Int): Boolean

    /**
     * Get all rows with their components.
     * If a row is empty, it will not be included.
     * @return List of rows.
     */
    suspend fun getRows(): List<List<RowComponent>>
}

class MultipleRowMessageBuilderImpl(override val maxNumberOfRows: Int = DISCORD_MAX_ROW) : MultipleRowMessageBuilder {

    private val rows = Array(maxNumberOfRows) { SingleRowMessageBuilderImpl() }

    private val rowsMutex = Mutex()

    override suspend fun addRowComponent(component: RowComponent): Boolean {
        return rowsMutex.withLock {
            if (unsafeContainsRowComponent(component.id)) {
                return false
            }
            rows.firstOrNull { it.hasWidthFor(component) }?.addRowComponent(component) == true
        }
    }

    override suspend fun addRowComponent(component: RowComponent, row: Int): Boolean {
        if (row !in 0 until maxNumberOfRows) {
            throw IndexOutOfBoundsException("Only rows from 0 to ${maxNumberOfRows - 1} are allowed")
        }
        return rowsMutex.withLock {
            if (unsafeContainsRowComponent(component.id)) {
                return false
            }
            rows[row].addRowComponent(component)
        }
    }

    override suspend fun removeRowComponent(id: String): Boolean {
        return rowsMutex.withLock {
            rows.any { it.removeRowComponent(id) }
        }
    }

    override suspend fun removeRowComponents(ids: Iterable<String>): Boolean {
        return rowsMutex.withLock {
            rows.forEachAnyMatch { it.removeRowComponents(ids) }
        }
    }

    override suspend fun removeAndCancelRowComponents(ids: Iterable<String>): Boolean {
        return rowsMutex.withLock {
            rows.forEachAnyMatch { it.removeAndCancelRowComponents(ids) }
        }
    }

    override suspend fun removeRowComponents() {
        rowsMutex.withLock {
            rows.forEach { it.removeRowComponents() }
        }
    }

    override suspend fun removeAndCancelRowComponents(): Boolean {
        return rowsMutex.withLock {
            rows.forEachAnyMatch { it.removeAndCancelRowComponents() }
        }
    }

    override suspend fun containsRowComponent(id: String): Boolean {
        return rowsMutex.withLock {
            unsafeContainsRowComponent(id)
        }
    }

    private suspend fun unsafeContainsRowComponent(id: String): Boolean {
        return rows.any { it.containsRowComponent(id) }
    }

    override suspend fun sizeRowComponents(): Int {
        return rowsMutex.withLock {
            rows.sumOf { it.sizeRowComponents() }
        }
    }

    override suspend fun getRowComponents(): List<RowComponent> {
        return rowsMutex.withLock {
            rows.flatMap { it.getRowComponents() }
        }
    }

    override suspend fun getRows(): List<List<RowComponent>> {
        return rowsMutex.withLock {
            rows.filter { it.sizeRowComponents() > 0 }.map { it.getRowComponents() }
        }
    }
}
