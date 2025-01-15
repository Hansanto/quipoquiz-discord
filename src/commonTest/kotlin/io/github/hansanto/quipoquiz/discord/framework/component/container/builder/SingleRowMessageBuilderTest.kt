package io.github.hansanto.quipoquiz.discord.framework.component.container.builder

import io.github.hansanto.quipoquiz.discord.framework.builder.component.CustomActionRowBuilder
import io.github.hansanto.quipoquiz.discord.framework.component.RowComponent
import io.github.hansanto.quipoquiz.util.randomString
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe

class SingleRowMessageBuilderTest : ShouldSpec({

    lateinit var builder: SingleRowMessageBuilder

    beforeTest {
        builder = SingleRowMessageBuilderImpl()
    }

    should("DISCORD_WIDTH_ROW is 5") {
        SingleRowMessageBuilder.DISCORD_WIDTH_ROW shouldBe 5
    }

    should("maxWidth should match with constructor parameters") {
        repeat(10) {
            val builder = SingleRowMessageBuilderImpl(it)
            builder.maxWidth shouldBe it
        }
    }

    should("addRowComponent adds if id is not present") {
        val component = createComponent()
        builder.addRowComponent(component) shouldBe true
        builder.sizeRowComponents() shouldBe 1
        builder.getRowComponents() shouldBe listOf(component)
    }

    should("addRowComponent doesn't add if id is present") {
        val component = createComponent()
        builder.addRowComponent(component) shouldBe true
        builder.addRowComponent(component) shouldBe false
        builder.sizeRowComponents() shouldBe 1
        builder.getRowComponents() shouldBe listOf(component)
    }

    should("addRowComponent adds next to each other") {
        val component1 = createComponent()
        val component2 = createComponent()
        builder.addRowComponent(component1) shouldBe true
        builder.addRowComponent(component2) shouldBe true
        builder.sizeRowComponents() shouldBe 2
        builder.getRowComponents() shouldBe listOf(component1, component2)
    }

    should("addRowComponent throws exception if row is full with 1 as width") {
        builder = SingleRowMessageBuilderImpl(123)
        repeat(123) {
            builder.addRowComponent(createComponent()) shouldBe true
        }
        builder.sizeRowComponents() shouldBe 123

        val component = createComponent()
        shouldThrow<RowOverflowException> {
            builder.addRowComponent(component)
        }
        builder.sizeRowComponents() shouldBe 123
    }

    should("addRowComponent throws exception if row is full with with more than 1 as width") {
        builder = SingleRowMessageBuilderImpl(17)
        builder.addRowComponent(createComponent(3)) shouldBe true
        builder.addRowComponent(createComponent(7)) shouldBe true

        shouldThrow<RowOverflowException> {
            builder.addRowComponent(createComponent(10))
        }

        builder.sizeRowComponents() shouldBe 2
    }

    should("addRowComponent throws exception if row is full with more than max width") {
        builder = SingleRowMessageBuilderImpl(8)
        shouldThrow<RowOverflowException> {
            builder.addRowComponent(createComponent(9))
        }
        builder.sizeRowComponents() shouldBe 0
    }

    should("addRowComponent with index adds in first position if no components") {
        val component = createComponent()
        builder.addRowComponent(component, 0) shouldBe true
        builder.sizeRowComponents() shouldBe 1
        builder.getRowComponents() shouldBe listOf(component)
    }

    should("addRowComponent with index doesn't add if id is present") {
        val component = createComponent()
        builder.addRowComponent(component, 0) shouldBe true
        repeat(builder.maxWidth) {
            builder.addRowComponent(component, it) shouldBe false
        }
        builder.sizeRowComponents() shouldBe 1
        builder.getRowComponents() shouldBe listOf(component)
    }

    should("addRowComponent with index adds in first position if index is 0") {
        val component = createComponent()
        val component2 = createComponent()
        builder.addRowComponent(component) shouldBe true
        builder.addRowComponent(component2, 0) shouldBe true
        builder.sizeRowComponents() shouldBe 2
        builder.getRowComponents() shouldBe listOf(component2, component)
    }

    should("addRowComponent with index adds in first position if index is negative") {
        val component = createComponent()
        val component2 = createComponent()
        builder.addRowComponent(component) shouldBe true
        builder.addRowComponent(component2, -1) shouldBe true
        builder.sizeRowComponents() shouldBe 2
        builder.getRowComponents() shouldBe listOf(component2, component)
    }

    should("addRowComponent with index adds in first position if index is greater than size") {
        val component = createComponent()
        val component2 = createComponent()
        builder.addRowComponent(component) shouldBe true
        builder.addRowComponent(component2, 10) shouldBe true
        builder.sizeRowComponents() shouldBe 2
        builder.getRowComponents() shouldBe listOf(component, component2)
    }

    should("addRowComponent with index adds second component with index below first but is placed after") {
        val component = createComponent()
        val component2 = createComponent()
        builder.addRowComponent(component, 10) shouldBe true
        builder.addRowComponent(component2, 0) shouldBe true
        builder.sizeRowComponents() shouldBe 2
        builder.getRowComponents() shouldBe listOf(component2, component)
    }

    should("addRowComponent with index throws exception if row is full with 1 as width") {
        builder = SingleRowMessageBuilderImpl(20)
        repeat(20) {
            builder.addRowComponent(createComponent()) shouldBe true
        }
        builder.sizeRowComponents() shouldBe 20

        val component6 = createComponent()
        shouldThrow<RowOverflowException> {
            builder.addRowComponent(component6, 2)
        }

        builder.sizeRowComponents() shouldBe 20
    }

    should("addRowComponent with index throws exception if row is full with more than 1 as width") {
        builder = SingleRowMessageBuilderImpl(50)
        builder.addRowComponent(createComponent(25)) shouldBe true
        builder.addRowComponent(createComponent(20)) shouldBe true

        shouldThrow<RowOverflowException> {
            builder.addRowComponent(createComponent(10), 1)
        }

        builder.sizeRowComponents() shouldBe 2
    }

    should("addRowComponent with index throws exception if row is full with 6 as width") {
        shouldThrow<RowOverflowException> {
            builder.addRowComponent(createComponent(6), 0)
        }
        builder.sizeRowComponents() shouldBe 0
    }

    should("removeRowComponent with id returns false if no components") {
        builder.removeRowComponent(randomString()) shouldBe false
        builder.sizeRowComponents() shouldBe 0
        builder.getRowComponents() shouldBe emptyList()
    }

    should("removeRowComponent with id removes component if id is present") {
        val component = createComponent()
        builder.addRowComponent(component)
        builder.removeRowComponent(component.id) shouldBe true
        builder.sizeRowComponents() shouldBe 0
        builder.getRowComponents() shouldBe emptyList()
    }

    should("removeRowComponent with id returns false if id is not present") {
        val component = createComponent()
        builder.addRowComponent(component)
        builder.removeRowComponent(randomString()) shouldBe false
        builder.sizeRowComponents() shouldBe 1
        builder.getRowComponents() shouldBe listOf(component)
    }

    should("removeRowComponent with id removes only component with id") {
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
        builder = SingleRowMessageBuilderImpl(5)
        val components = List(5) { createComponent() }
        components.forEach {
            builder.addRowComponent(it)
        }
        builder.removeRowComponents(components.map { it.id }) shouldBe true
        builder.sizeRowComponents() shouldBe 0
        builder.getRowComponents() shouldBe emptyList()
    }

    should("removeRowComponents with ids removes only components with ids") {
        builder = SingleRowMessageBuilderImpl(5)
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

    should("removeRowComponents removes all components") {
        val component1 = createComponent()
        val component2 = createComponent()
        builder.addRowComponent(component1)
        builder.addRowComponent(component2)
        builder.removeRowComponents()
        builder.sizeRowComponents() shouldBe 0
    }

    should("removeAndCancelRowComponents with ids does nothing if no components") {
        builder.removeAndCancelRowComponents(listOf("id")) shouldBe false
        builder.sizeRowComponents() shouldBe 0
    }

    should("removeAndCancelRowComponents with ids removes and cancels all components") {
        var canceled1 = false
        val component1 = createComponent {
            canceled1 = true
        }

        var canceled2 = false
        val component2 = createComponent {
            canceled2 = true
        }

        builder.addRowComponent(component1)
        builder.addRowComponent(component2)
        builder.removeAndCancelRowComponents(listOf(component1.id, component2.id)) shouldBe true
        builder.sizeRowComponents() shouldBe 0

        canceled1 shouldBe true
        canceled2 shouldBe true
    }

    should("removeAndCancelRowComponents with ids cancels only the removed components") {
        var canceled1 = false
        val component1 = createComponent {
            canceled1 = true
        }

        var canceled2 = false
        val component2 = createComponent { canceled2 = true }

        builder.addRowComponent(component1)
        builder.addRowComponent(component2)

        builder.removeAndCancelRowComponents(listOf(component1.id)) shouldBe true
        builder.sizeRowComponents() shouldBe 1
        builder.getRowComponents() shouldBe listOf(component2)

        canceled1 shouldBe true
        canceled2 shouldBe false
    }

    should("removeAndCancelRowComponents does nothing if no components") {
        builder.removeAndCancelRowComponents() shouldBe false
        builder.sizeRowComponents() shouldBe 0
    }

    should("removeAndCancelRowComponents removes and cancels all components") {
        var canceled1 = false
        val component1 = createComponent {
            canceled1 = true
        }

        var canceled2 = false
        val component2 = createComponent {
            canceled2 = true
        }

        builder.addRowComponent(component1)
        builder.addRowComponent(component2)
        builder.removeAndCancelRowComponents() shouldBe true
        builder.sizeRowComponents() shouldBe 0

        canceled1 shouldBe true
        canceled2 shouldBe true
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

        builder.removeAndCancelRowComponents() shouldBe true
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

    should("containsRowComponent returns true if id is present") {
        val component = createComponent()
        builder.addRowComponent(component)
        builder.containsRowComponent(component.id) shouldBe true
    }

    should("sizeRowComponents returns 0 if no components") {
        builder.sizeRowComponents() shouldBe 0
    }

    should("sizeRowComponents returns component number and not width") {
        builder = SingleRowMessageBuilderImpl(100)
        builder.addRowComponent(createComponent(1))
        builder.sizeRowComponents() shouldBe 1

        builder.addRowComponent(createComponent(99))
        builder.sizeRowComponents() shouldBe 2
    }

    should("sizeRowComponents returns component number") {
        repeat(builder.maxWidth) {
            val numberOfComponent = it + 1
            repeat(numberOfComponent) {
                builder.addRowComponent(createComponent())
            }
            builder.sizeRowComponents() shouldBe numberOfComponent
            builder.removeRowComponents()
        }
    }

    should("getRowComponents returns empty list if no components") {
        builder.getRowComponents() shouldBe emptyList()
    }

    should("getRowComponents returns list of components") {
        val component1 = createComponent()
        val component2 = createComponent()
        builder.addRowComponent(component1)
        builder.addRowComponent(component2)
        builder.getRowComponents() shouldBe listOf(component1, component2)
    }

    should("remainingWidth returns max width if no components") {
        builder = SingleRowMessageBuilderImpl(42)
        builder.remainingWidth() shouldBe 42
    }

    should("remainingWidth returns the max width minus the sum of components width") {
        builder = SingleRowMessageBuilderImpl(1000)
        builder.remainingWidth() shouldBe 1000

        builder.addRowComponent(createComponent(1))
        builder.remainingWidth() shouldBe 999

        builder.addRowComponent(createComponent(99))
        builder.remainingWidth() shouldBe 900

        builder.addRowComponent(createComponent(900))
        builder.remainingWidth() shouldBe 0
    }

    should("hasWidthFor returns true if row has enough width for component") {
        builder = SingleRowMessageBuilderImpl(100)
        (1..100).forEach {
            builder.hasWidthFor(createComponent(it)) shouldBe true
        }
    }

    should("hasWidthFor returns false if row doesn't have enough width for component") {
        builder = SingleRowMessageBuilderImpl(12)
        builder.hasWidthFor(createComponent(13)) shouldBe false
        builder.hasWidthFor(createComponent(100)) shouldBe false
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
