package io.github.hansanto.quipoquiz.discord.framework.component.container.builder

import io.github.hansanto.quipoquiz.discord.framework.builder.message.CustomEmbedBuilder
import io.github.hansanto.quipoquiz.discord.framework.component.EmbedComponent
import io.github.hansanto.quipoquiz.util.randomString
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe

class SingleEmbedMessageBuilderTest : ShouldSpec({

    lateinit var builder: EmbedMessageBuilder

    beforeTest {
        builder = SingleEmbedMessageBuilderImpl()
    }

    should("addEmbedComponent add if id is not present") {
        val component = createComponent()
        builder.addEmbedComponent(component)
        builder.sizeEmbedComponents() shouldBe 1
        builder.getEmbedComponents() shouldBe listOf(component)
    }

    should("addEmbedComponent doesn't add if id is present") {
        val component = createComponent()
        builder.addEmbedComponent(component)
        builder.addEmbedComponent(component)
        builder.sizeEmbedComponents() shouldBe 1
        builder.getEmbedComponents() shouldBe listOf(component)
    }

    should("addEmbedComponent next to each other") {
        val component1 = createComponent()
        val component2 = createComponent()
        builder.addEmbedComponent(component1)
        builder.addEmbedComponent(component2)
        builder.sizeEmbedComponents() shouldBe 2
        builder.getEmbedComponents() shouldBe listOf(component1, component2)
    }

    should("removeEmbedComponent returns false if no components") {
        builder.removeEmbedComponent(randomString()) shouldBe false
        builder.sizeEmbedComponents() shouldBe 0
        builder.getEmbedComponents() shouldBe emptyList()
    }

    should("removeEmbedComponent returns false if id is not present") {
        val component = createComponent()
        builder.addEmbedComponent(component)
        builder.removeEmbedComponent(randomString()) shouldBe false
        builder.sizeEmbedComponents() shouldBe 1
        builder.getEmbedComponents() shouldBe listOf(component)
    }

    should("removeEmbedComponent removes component if id is present") {
        val component = object : EmbedComponent {
            override val id: String = randomString()
            override suspend fun render(builder: CustomEmbedBuilder) {
                error("Not yet implemented")
            }
        }
        builder.addEmbedComponent(component)
        builder.removeEmbedComponent(component.id) shouldBe true
        builder.sizeEmbedComponents() shouldBe 0
        builder.getEmbedComponents() shouldBe emptyList()
    }

    should("removeAndCancelEmbedComponents does nothing if no components") {
        builder.removeAndCancelEmbedComponents()
        builder.sizeEmbedComponents() shouldBe 0
    }

    should("removeAndCancelEmbedComponents removes and cancels all components") {
        var canceled1 = false
        val component1 = createComponent {
            canceled1 = true
        }

        var canceled2 = false
        val component2 = createComponent {
            canceled2 = true
        }

        builder.addEmbedComponent(component1)
        builder.addEmbedComponent(component2)
        builder.removeAndCancelEmbedComponents()
        builder.sizeEmbedComponents() shouldBe 0

        canceled1 shouldBe true
        canceled2 shouldBe true
    }

    should("removeAndCancelEmbedComponents cancels only the removed components") {
        var canceled1 = false
        val component1 = createComponent {
            canceled1 = true
        }

        var canceled2 = false
        val component2 = createComponent { canceled2 = true }

        builder.addEmbedComponent(component1)

        builder.addEmbedComponent(component2)
        builder.removeEmbedComponent(component2.id)

        builder.removeAndCancelEmbedComponents()
        builder.sizeEmbedComponents() shouldBe 0

        canceled1 shouldBe true
        canceled2 shouldBe false
    }

    should("removeAndCancelEmbedComponents with ids does nothing if no components") {
        builder.removeAndCancelEmbedComponents(listOf("id")) shouldBe false
        builder.sizeEmbedComponents() shouldBe 0
    }

    should("removeAndCancelEmbedComponents with ids removes and cancels all components") {
        var canceled1 = false
        val component1 = createComponent {
            canceled1 = true
        }

        var canceled2 = false
        val component2 = createComponent {
            canceled2 = true
        }

        builder.addEmbedComponent(component1)
        builder.addEmbedComponent(component2)
        builder.removeAndCancelEmbedComponents(listOf(component1.id, component2.id)) shouldBe true
        builder.sizeEmbedComponents() shouldBe 0

        canceled1 shouldBe true
        canceled2 shouldBe true
    }

    should("removeAndCancelEmbedComponents with ids cancels only the removed components") {
        var canceled1 = false
        val component1 = createComponent {
            canceled1 = true
        }

        var canceled2 = false
        val component2 = createComponent { canceled2 = true }

        builder.addEmbedComponent(component1)
        builder.addEmbedComponent(component2)

        builder.removeAndCancelEmbedComponents(listOf(component1.id)) shouldBe true
        builder.sizeEmbedComponents() shouldBe 1
        builder.getEmbedComponents() shouldBe listOf(component2)

        canceled1 shouldBe true
        canceled2 shouldBe false
    }

    should("containsEmbedComponent returns false if no components") {
        builder.containsEmbedComponent(randomString()) shouldBe false
    }

    should("containsEmbedComponent returns false if id is not present") {
        val component = createComponent()
        builder.addEmbedComponent(component)
        builder.containsEmbedComponent(randomString()) shouldBe false
    }

    should("containsEmbedComponent returns true if id is present") {
        val component = object : EmbedComponent {
            override val id: String = randomString()
            override suspend fun render(builder: CustomEmbedBuilder) {
                error("Not yet implemented")
            }
        }
        builder.addEmbedComponent(component)
        builder.containsEmbedComponent(component.id) shouldBe true
    }

    should("sizeEmbedComponents returns 0 if no components") {
        builder.sizeEmbedComponents() shouldBe 0
    }

    should("sizeEmbedComponents returns 1 if one component") {
        val component = createComponent()
        builder.addEmbedComponent(component)
        builder.sizeEmbedComponents() shouldBe 1
    }

    should("sizeEmbedComponents returns 2 if two components") {
        val component1 = createComponent()
        val component2 = createComponent()
        builder.addEmbedComponent(component1)
        builder.addEmbedComponent(component2)
        builder.sizeEmbedComponents() shouldBe 2
    }

    should("removeEmbedComponents does nothing if no components") {
        builder.removeEmbedComponents()
        builder.sizeEmbedComponents() shouldBe 0
        builder.getEmbedComponents() shouldBe emptyList()
    }

    should("removeEmbedComponents removes all components") {
        val component1 = createComponent()
        val component2 = createComponent()
        builder.addEmbedComponent(component1)
        builder.addEmbedComponent(component2)
        builder.removeEmbedComponents()
        builder.sizeEmbedComponents() shouldBe 0
        builder.getEmbedComponents() shouldBe emptyList()
    }

    should("getEmbedComponents returns empty list if no components") {
        builder.getEmbedComponents() shouldBe emptyList()
    }

    should("getEmbedComponents returns list of components") {
        val component1 = createComponent()
        val component2 = createComponent()
        builder.addEmbedComponent(component1)
        builder.addEmbedComponent(component2)
        builder.getEmbedComponents() shouldBe listOf(component1, component2)
    }
}) {

    companion object {

        private inline fun createComponent(crossinline onCancel: () -> Unit = {}) = object : EmbedComponent {
            override val id: String = randomString()
            override suspend fun render(builder: CustomEmbedBuilder) {
                error("Not yet implemented")
            }

            override suspend fun cancel() {
                onCancel()
            }
        }
    }
}
