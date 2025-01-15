package io.github.hansanto.quipoquiz.discord.framework.render

import dev.kord.common.entity.ButtonStyle
import io.github.hansanto.quipoquiz.discord.framework.builder.component.CustomActionRowBuilder
import io.github.hansanto.quipoquiz.discord.framework.builder.message.CustomEmbedBuilder
import io.github.hansanto.quipoquiz.discord.framework.component.EmbedComponent
import io.github.hansanto.quipoquiz.discord.framework.component.GroupComponent
import io.github.hansanto.quipoquiz.discord.framework.component.RowComponent
import io.github.hansanto.quipoquiz.discord.framework.component.container.builder.InteractionDeltaBuilder
import io.github.hansanto.quipoquiz.discord.framework.component.container.builder.MessageBuilder
import io.github.hansanto.quipoquiz.discord.framework.component.container.builder.MessageBuilderImpl
import io.github.hansanto.quipoquiz.util.randomString
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe

class DeltaRendererTest : ShouldSpec({

    lateinit var messageBuilder: MessageBuilder

    lateinit var srcRenderer: Renderer

    lateinit var renderer: DeltaRenderer

    beforeTest {
        messageBuilder = MessageBuilderImpl()
        srcRenderer = DefaultRenderer(messageBuilder)
        renderer = DeltaRenderer(srcRenderer)
    }

    should("render with no components sets to mutable list if different") {
        val builder = InteractionDeltaBuilder(
            components = null,
            embeds = null
        )
        renderer.render(builder)
        builder.components shouldBe mutableListOf()
        builder.embeds shouldBe mutableListOf()
    }

    should("render with no components sets to null if similar") {
        val builder = InteractionDeltaBuilder(
            components = mutableListOf(),
            embeds = mutableListOf()
        )
        renderer.render(builder)
        builder.components shouldBe null
        builder.embeds shouldBe null
    }

    should("render group components if present") {
        messageBuilder.addGroupComponent(
            createGroupComponent {
                messageBuilder.addEmbedComponent(createEmbed { title = "Title" })
            }
        )

        val builder = InteractionDeltaBuilder()
        renderer.render(builder)
        builder.components shouldBe mutableListOf()
        builder.embeds shouldBe mutableListOf(
            CustomEmbedBuilder().apply {
                title = "Title"
            }
        )

        val builder2 = builder.copy()
        renderer.render(builder2)
        builder2.components shouldBe null
        builder2.embeds shouldBe null
    }

    should("render embed components if present") {
        messageBuilder.addEmbedComponent(createEmbed { title = "My title" })

        val builder = InteractionDeltaBuilder()
        renderer.render(builder)
        builder.components shouldBe mutableListOf()
        builder.embeds shouldBe mutableListOf(
            CustomEmbedBuilder().apply {
                title = "My title"
            }
        )

        val builder2 = builder.copy()
        renderer.render(builder2)
        builder2.components shouldBe null
        builder2.embeds shouldBe null
    }

    should("render row components if present") {
        messageBuilder.addRowComponent(
            createRowComponent {
                interactionButton(ButtonStyle.Primary, "button1") {
                    label = "Button"
                }
            }
        )

        val builder = InteractionDeltaBuilder()
        renderer.render(builder)
        builder.components shouldBe mutableListOf(
            CustomActionRowBuilder().apply {
                interactionButton(ButtonStyle.Primary, "button1") {
                    label = "Button"
                }
            }
        )
        builder.embeds shouldBe mutableListOf()

        val builder2 = builder.copy()
        renderer.render(builder2)
        builder2.components shouldBe null
        builder2.embeds shouldBe null
    }

    should("render all components") {
        messageBuilder.addRowComponent(
            createRowComponent("btn1") {
                interactionButton(ButtonStyle.Primary, "button1") {
                    label = "Button"
                }
            },
            1
        )
        messageBuilder.addGroupComponent(
            createGroupComponent {
                messageBuilder.addEmbedComponent(
                    createEmbed("emd1") {
                        title = "Title"
                        description = "Hello"
                    }
                )
                messageBuilder.addRowComponent(
                    createRowComponent("btn2") {
                        interactionButton(ButtonStyle.Primary, "button2") {
                            label = "Button 2"
                        }
                    },
                    1
                )
            }
        )
        messageBuilder.addEmbedComponent(
            createEmbed("emd2") {
                title = "My title"
                url = "world"
            }
        )

        val builder = InteractionDeltaBuilder()
        renderer.render(builder)
        builder.components shouldBe mutableListOf(
            CustomActionRowBuilder().apply {
                interactionButton(ButtonStyle.Primary, "button1") {
                    label = "Button"
                }
                interactionButton(ButtonStyle.Primary, "button2") {
                    label = "Button 2"
                }
            }
        )
        builder.embeds shouldBe mutableListOf(
            CustomEmbedBuilder().apply {
                title = "Title"
                description = "Hello"
                url = "world"
            }
        )

        val builder2 = builder.copy()
        renderer.render(builder2)
        builder2.components shouldBe null
        builder2.embeds shouldBe null
    }

    should("renderEmbeds with no components sets to mutable list if different") {
        val builder = InteractionDeltaBuilder(
            components = null,
            embeds = null
        )
        renderer.renderEmbeds(builder)
        builder.components shouldBe null
        builder.embeds shouldBe mutableListOf()
    }

    should("renderEmbeds with no components sets to null if similar") {
        val builder = InteractionDeltaBuilder(
            components = mutableListOf(),
            embeds = mutableListOf()
        )
        renderer.renderEmbeds(builder)
        builder.components shouldBe mutableListOf()
        builder.embeds shouldBe null
    }

    should("renderEmbeds modify only embeds if different") {
        messageBuilder.addRowComponent(
            createRowComponent {
                interactionButton(ButtonStyle.Primary, "button1") {
                    label = "Button"
                }
            }
        )
        messageBuilder.addEmbedComponent(createEmbed { title = "Title" })

        val builder = InteractionDeltaBuilder(
            components = mutableListOf(),
            embeds = mutableListOf()
        )
        renderer.renderEmbeds(builder)
        builder.components shouldBe mutableListOf()
        builder.embeds shouldBe mutableListOf(
            CustomEmbedBuilder().apply {
                title = "Title"
            }
        )
    }

    should("renderRowComponents with no components sets to mutable list if different") {
        val builder = InteractionDeltaBuilder(
            components = null,
            embeds = null
        )
        renderer.renderRowComponents(builder)
        builder.components shouldBe mutableListOf()
        builder.embeds shouldBe null
    }

    should("renderRowComponents with no components sets to null if similar") {
        val builder = InteractionDeltaBuilder(
            components = mutableListOf(),
            embeds = mutableListOf()
        )
        renderer.renderRowComponents(builder)
        builder.components shouldBe null
        builder.embeds shouldBe mutableListOf()
    }

    should("renderRowComponents modify only components if different") {
        messageBuilder.addRowComponent(
            createRowComponent {
                interactionButton(ButtonStyle.Primary, "button1") {
                    label = "Button"
                }
            }
        )
        messageBuilder.addEmbedComponent(createEmbed { title = "Title" })

        val builder = InteractionDeltaBuilder(
            components = mutableListOf(),
            embeds = mutableListOf()
        )
        renderer.renderRowComponents(builder)
        builder.components shouldBe mutableListOf(
            CustomActionRowBuilder().apply {
                interactionButton(ButtonStyle.Primary, "button1") {
                    label = "Button"
                }
            }
        )
        builder.embeds shouldBe mutableListOf()
    }
})

private inline fun createGroupComponent(
    id: String = randomString(),
    crossinline action: suspend () -> Unit
): GroupComponent = object : GroupComponent {
    override val messageBuilder: MessageBuilder
        get() = error("Not yet implemented")

    override val id: String = id

    override suspend fun registerComponents() {
        action()
    }
}

private inline fun createRowComponent(
    id: String = randomString(),
    crossinline action: CustomActionRowBuilder.() -> Unit
): RowComponent = object : RowComponent {

    override val width: Int
        get() = 0

    override val id: String = id

    override suspend fun render(builder: CustomActionRowBuilder) {
        action(builder)
    }
}

private inline fun createEmbed(
    id: String = randomString(),
    crossinline action: CustomEmbedBuilder.() -> Unit
): EmbedComponent = object : EmbedComponent {

    override val id: String = id

    override suspend fun render(builder: CustomEmbedBuilder) {
        action(builder)
    }
}
