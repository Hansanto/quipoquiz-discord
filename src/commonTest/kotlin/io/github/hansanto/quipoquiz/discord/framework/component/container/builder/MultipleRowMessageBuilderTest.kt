package io.github.hansanto.quipoquiz.discord.framework.component.container.builder

import io.github.hansanto.quipoquiz.discord.framework.builder.component.CustomActionRowBuilder
import io.github.hansanto.quipoquiz.discord.framework.component.RowComponent
import io.github.hansanto.quipoquiz.discord.framework.component.container.builder.MultipleRowMessageBuilder.Companion.DISCORD_MAX_ROW
import io.github.hansanto.quipoquiz.util.randomString
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe

class MultipleRowMessageBuilderTest : ShouldSpec({

    lateinit var builder: MultipleRowMessageBuilder

    beforeTest {
        builder = MultipleRowMessageBuilderImpl()
    }

    should("DISCORD_MAX_ROW should be 5") {
        DISCORD_MAX_ROW shouldBe 5
    }

    should("maxNumberOfRows should match with constructor parameters") {
        val builder = MultipleRowMessageBuilderImpl(10)
        builder.maxNumberOfRows shouldBe 10

        repeat(10) {
            val component = createComponent()
            builder.addRowComponent(component, it) shouldBe true
        }
        builder.getRows().size shouldBe 10
    }

    should("maxWidth of row should be 5") {
        val tooLongComponent = createComponent(6)
        builder.addRowComponent(tooLongComponent) shouldBe false

        repeat(builder.maxNumberOfRows) {
            val component = createComponent(5)
            builder.addRowComponent(component) shouldBe true
        }
    }

    should("addRowComponent adds if id is not present") {
        val component = createComponent()
        builder.addRowComponent(component) shouldBe true
        builder.getRowComponents() shouldBe listOf(component)
    }

    should("addRowComponent doesn't add if id is present") {
        val component = createComponent()
        builder.addRowComponent(component) shouldBe true
        builder.addRowComponent(component) shouldBe false
        builder.getRowComponents() shouldBe listOf(component)
    }

    should("addRowComponent next to each other if row is not filled") {
        val component1 = createComponent()
        val component2 = createComponent()
        builder.addRowComponent(component1) shouldBe true
        builder.addRowComponent(component2) shouldBe true
        builder.getRowComponents() shouldBe listOf(component1, component2)
    }

    should("addRowComponent add in next row if first row is filled") {
        val component1 = createComponent(5)
        val component2 = createComponent()
        builder.addRowComponent(component1) shouldBe true
        builder.addRowComponent(component2) shouldBe true
        builder.getRowComponents() shouldBe listOf(component1, component2)
        builder.getRows() shouldBe listOf(listOf(component1), listOf(component2))
    }

    should("addRowComponent doesn't add if rows are full") {
        val added = List(5) {
            createComponent(5).also {
                builder.addRowComponent(it) shouldBe true
            }
        }
        val component = createComponent()
        builder.addRowComponent(component) shouldBe false
        builder.getRowComponents() shouldBe added
    }

    should("addRowComponent with row number adds if id is not present") {
        val component = createComponent()
        builder.addRowComponent(component, 0) shouldBe true
        builder.getRowComponents() shouldBe listOf(component)
    }

    should("addRowComponent with row number doesn't add if id is present") {
        val component = createComponent()
        builder.addRowComponent(component, 0) shouldBe true
        repeat(builder.maxNumberOfRows) {
            builder.addRowComponent(component, it) shouldBe false
        }

        builder.getRowComponents() shouldBe listOf(component)
    }

    should("addRowComponent with row number throws exception if row is out of bounds") {
        val component = createComponent()
        shouldThrow<IndexOutOfBoundsException> {
            builder.addRowComponent(component, -1)
        }
        shouldThrow<IndexOutOfBoundsException> {
            builder.addRowComponent(component, builder.maxNumberOfRows)
        }
    }

    should("addRowComponent with row number throws exception if row is full") {
        val component = createComponent(5)
        builder.addRowComponent(component, 0) shouldBe true
        shouldThrow<RowOverflowException> {
            builder.addRowComponent(createComponent(), 0)
        }
    }

    should("removeRowComponent returns false if no components") {
        builder.removeRowComponent(randomString()) shouldBe false
        builder.sizeRowComponents() shouldBe 0
    }

    should("removeRowComponent returns false if id is not present") {
        val component = createComponent()
        builder.addRowComponent(component) shouldBe true
        builder.removeRowComponent(randomString()) shouldBe false
        builder.sizeRowComponents() shouldBe 1
    }

    should("removeRowComponent returns true if id is present in any row") {
        (0 until DISCORD_MAX_ROW).forEach {
            val component = createComponent()
            builder.addRowComponent(component, it) shouldBe true
            builder.removeRowComponent(component.id) shouldBe true
            builder.sizeRowComponents() shouldBe 0
        }
    }

    should("removeRowComponent removes only component with id") {
        val component1 = createComponent()
        val component2 = createComponent()
        builder.addRowComponent(component1)
        builder.addRowComponent(component2)
        builder.removeRowComponent(component1.id) shouldBe true
        builder.sizeRowComponents() shouldBe 1
        builder.getRowComponents() shouldBe listOf(component2)
    }

    should("removeRowComponents with ids returns false if no components") {
        builder.removeRowComponents(emptyList()) shouldBe false
    }

    should("removeRowComponents with ids returns false if no components with ids") {
        val component = createComponent()
        builder.addRowComponent(component)
        builder.removeRowComponents(listOf(randomString())) shouldBe false
    }

    should("removeRowComponents with ids removes all components") {
        val components = List(5) { createComponent() }
        components.forEach {
            builder.addRowComponent(it)
        }
        builder.removeRowComponents(components.map { it.id }) shouldBe true
        builder.sizeRowComponents() shouldBe 0
        builder.getRowComponents() shouldBe emptyList()
    }

    should("removeRowComponents with ids removes only components with ids") {
        val components = List(5) { createComponent() }
        components.forEach {
            builder.addRowComponent(it)
        }
        builder.removeRowComponents(components.subList(0, 3).map { it.id }) shouldBe true
        builder.sizeRowComponents() shouldBe 2
        builder.getRowComponents() shouldBe components.subList(3, 5)
    }

    should("removeRowComponents does nothing if no components") {
        builder.removeRowComponents()
        builder.sizeRowComponents() shouldBe 0
    }

    should("removeRowComponents removes all components in all rows") {
        repeat(builder.maxNumberOfRows) {
            val component = createComponent()
            builder.addRowComponent(component, it)
        }
        builder.removeRowComponents()
        builder.sizeRowComponents() shouldBe 0
    }

    should("removeAndCancelRowComponents with ids does nothing if no components") {
        builder.removeAndCancelRowComponents(listOf("id")) shouldBe false
        builder.sizeRowComponents() shouldBe 0
    }

    should("removeAndCancelRowComponents with ids removes and cancels all components in all rows") {
        val componentsWithState = mutableMapOf<String, Boolean>()
        repeat(builder.maxNumberOfRows) {
            repeat(5) {
                lateinit var component: RowComponent
                component = createComponent {
                    componentsWithState[component.id] = true
                }
                builder.addRowComponent(component)
                componentsWithState[component.id] = false
            }
        }

        builder.removeAndCancelRowComponents(componentsWithState.keys) shouldBe true
        builder.sizeRowComponents() shouldBe 0
        componentsWithState.values.forEach { it shouldBe true }
    }

    should("removeAndCancelRowComponents with ids cancels only the removed components") {
        val componentsWillBeRemoved = mutableMapOf<String, Boolean>()
        val componentsWillNotBeRemoved = mutableMapOf<String, Boolean>()

        suspend fun addComponent(row: Int, components: MutableMap<String, Boolean>) {
            lateinit var component: RowComponent
            component = createComponent {
                components[component.id] = true
            }
            builder.addRowComponent(component, row)
            components[component.id] = false
        }

        repeat(builder.maxNumberOfRows) {
            addComponent(it, componentsWillBeRemoved)
            addComponent(it, componentsWillNotBeRemoved)
        }

        builder.removeAndCancelRowComponents(componentsWillBeRemoved.keys) shouldBe true
        builder.sizeRowComponents() shouldBe componentsWillNotBeRemoved.size
        builder.getRowComponents().map { it.id } shouldBe componentsWillNotBeRemoved.keys.toList()
        componentsWillBeRemoved.values.forEach { it shouldBe true }
        componentsWillNotBeRemoved.values.forEach { it shouldBe false }
    }

    should("removeAndCancelRowComponents does nothing if no components") {
        builder.removeAndCancelRowComponents() shouldBe false
        builder.sizeRowComponents() shouldBe 0
    }

    should("removeAndCancelRowComponents removes and cancels all components in all rows") {
        val componentsWithState = mutableMapOf<String, Boolean>()
        repeat(builder.maxNumberOfRows) {
            repeat(5) {
                lateinit var component: RowComponent
                component = createComponent {
                    componentsWithState[component.id] = true
                }
                builder.addRowComponent(component)
                componentsWithState[component.id] = false
            }
        }

        builder.removeAndCancelRowComponents() shouldBe true
        builder.sizeRowComponents() shouldBe 0
        componentsWithState.values.forEach { it shouldBe true }
    }

    should("removeAndCancelRowComponents cancels only the removed components") {
        var canceled1 = false
        val component1 = createComponent {
            canceled1 = true
        }

        var canceled2 = false
        val component2 = createComponent { canceled2 = true }

        builder.addRowComponent(component1)

        builder.addRowComponent(component2)
        builder.removeRowComponent(component2.id)

        builder.removeAndCancelRowComponents()
        builder.sizeRowComponents() shouldBe 0

        canceled1 shouldBe true
        canceled2 shouldBe false
    }

    should("containsRowComponent returns false if no components") {
        builder.containsRowComponent(randomString()) shouldBe false
    }

    should("containsRowComponent returns false if id is not present") {
        val component = createComponent()
        builder.addRowComponent(component)
        builder.containsRowComponent(randomString()) shouldBe false
    }

    should("containsRowComponent returns true if id is present in any row") {
        repeat(builder.maxNumberOfRows) {
            val component = createComponent()
            builder.addRowComponent(component, it)
            builder.containsRowComponent(component.id) shouldBe true
        }
    }

    should("sizeRowComponents returns 0 if no components") {
        builder.sizeRowComponents() shouldBe 0
    }

    should("sizeRowComponents returns component number and not width") {
        builder = MultipleRowMessageBuilderImpl(2)
        builder.addRowComponent(createComponent(3))
        builder.sizeRowComponents() shouldBe 1

        builder.addRowComponent(createComponent(3))
        builder.sizeRowComponents() shouldBe 2
    }

    should("sizeRowComponents returns component number") {
        repeat(builder.maxNumberOfRows) {
            repeat(5) {
                builder.addRowComponent(createComponent())
            }
            builder.sizeRowComponents() shouldBe 5 * (it + 1)
        }
    }

    should("getRowComponents returns empty list if no components") {
        builder.getRowComponents() shouldBe emptyList()
    }

    should("getRowComponents returns all components") {
        val components = List(builder.maxNumberOfRows) {
            List(5) { createComponent() }.onEach { component ->
                builder.addRowComponent(component, it)
            }
        }.flatten()
        builder.getRowComponents() shouldBe components
    }

    should("getRows returns empty list if no components") {
        builder.getRows() shouldBe emptyList()
    }

    should("getRows returns all components in rows") {
        val components = List(builder.maxNumberOfRows) {
            List(5) { createComponent() }.onEach { component ->
                builder.addRowComponent(component, it)
            }
        }
        builder.getRows() shouldBe components
    }
}) {

    companion object {

        private inline fun createComponent(width: Int = 1, crossinline onCancel: () -> Unit = {}) =
            object : RowComponent {
                override val width: Int = width
                override val id: String = randomString()
                override suspend fun render(builder: CustomActionRowBuilder) {
                    error("Not yet implemented")
                }

                override suspend fun cancel() {
                    onCancel()
                }
            }
    }
}
