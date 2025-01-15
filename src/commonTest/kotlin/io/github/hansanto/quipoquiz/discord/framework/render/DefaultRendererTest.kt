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

class DefaultRendererTest : ShouldSpec({

    lateinit var messageBuilder: MessageBuilder

    lateinit var renderer: DefaultRenderer

    beforeTest {
        messageBuilder = MessageBuilderImpl()
        renderer = DefaultRenderer(messageBuilder)
    }

    should("render do nothing if no components") {
        val builder = InteractionDeltaBuilder()
        renderer.render(builder)
        builder.components shouldBe mutableListOf()
        builder.embeds shouldBe mutableListOf()
    }

    should("render group components if present") {
        val builder = InteractionDeltaBuilder()
        messageBuilder.addGroupComponent(
            createGroupComponent {
                messageBuilder.addEmbedComponent(createEmbed { title = "Title" })
            }
        )
        renderer.render(builder)
        builder.components shouldBe mutableListOf()
        builder.embeds shouldBe mutableListOf(
            CustomEmbedBuilder().apply {
                title = "Title"
            }
        )
    }

    should("render embed components if present") {
        val builder = InteractionDeltaBuilder()
        messageBuilder.addEmbedComponent(createEmbed { title = "My title" })
        renderer.render(builder)
        builder.components shouldBe mutableListOf()
        builder.embeds shouldBe mutableListOf(
            CustomEmbedBuilder().apply {
                title = "My title"
            }
        )
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
    }

    should("render all components") {
        messageBuilder.addRowComponent(
            createRowComponent {
                interactionButton(ButtonStyle.Primary, "button1") {
                    label = "Button"
                }
            },
            1
        )

        messageBuilder.addGroupComponent(
            createGroupComponent {
                messageBuilder.addEmbedComponent(
                    createEmbed {
                        title = "Title"
                        description = "Hello"
                    }
                )
                messageBuilder.addRowComponent(
                    createRowComponent {
                        interactionButton(ButtonStyle.Primary, "button2") {
                            label = "Button 2"
                        }
                    },
                    1
                )
            }
        )

        messageBuilder.addEmbedComponent(
            createEmbed {
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
    }

    should("renderEmbeds do nothing if no embeds") {
        val builder = InteractionDeltaBuilder()
        renderer.renderEmbeds(builder)
        builder.embeds shouldBe mutableListOf()
        builder.components shouldBe null
    }

    should("renderEmbeds modify only embeds") {
        val builder = InteractionDeltaBuilder()
        messageBuilder.addEmbedComponent(createEmbed { title = "My title" })
        messageBuilder.addRowComponent(
            createRowComponent {
                interactionButton(ButtonStyle.Primary, "button1") {
                    label = "Button"
                }
            }
        )
        renderer.renderEmbeds(builder)
        builder.embeds shouldBe mutableListOf(
            CustomEmbedBuilder().apply {
                title = "My title"
            }
        )
        builder.components shouldBe null
    }

    should("renderRowComponents do nothing if no row components") {
        val builder = InteractionDeltaBuilder()
        renderer.renderRowComponents(builder)
        builder.components shouldBe mutableListOf()
        builder.embeds shouldBe null
    }

    should("renderRowComponents modify only row components") {
        val builder = InteractionDeltaBuilder()
        messageBuilder.addRowComponent(
            createRowComponent {
                interactionButton(ButtonStyle.Primary, "button1") {
                    label = "Button"
                }
            }
        )
        messageBuilder.addEmbedComponent(createEmbed { title = "My title" })
        renderer.renderRowComponents(builder)
        builder.components shouldBe mutableListOf(
            CustomActionRowBuilder().apply {
                interactionButton(ButtonStyle.Primary, "button1") {
                    label = "Button"
                }
            }
        )
        builder.embeds shouldBe null
    }
})

private inline fun createGroupComponent(crossinline action: suspend () -> Unit): GroupComponent =
    object : GroupComponent {
        override val messageBuilder: MessageBuilder
            get() = error("Not yet implemented")

        override val id: String
            get() = randomString()

        override suspend fun registerComponents() {
            action()
        }
    }

private inline fun createRowComponent(crossinline action: CustomActionRowBuilder.() -> Unit): RowComponent =
    object : RowComponent {

        override val width: Int
            get() = 0

        override val id: String
            get() = randomString()

        override suspend fun render(builder: CustomActionRowBuilder) {
            action(builder)
        }
    }

private inline fun createEmbed(crossinline action: CustomEmbedBuilder.() -> Unit): EmbedComponent =
    object : EmbedComponent {

        override val id: String
            get() = randomString()

        override suspend fun render(builder: CustomEmbedBuilder) {
            action(builder)
        }
    }
