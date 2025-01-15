package io.github.hansanto.quipoquiz.discord.framework.component.container.builder

import dev.kord.common.entity.ButtonStyle
import dev.kord.rest.builder.component.ActionRowBuilder
import dev.kord.rest.builder.component.ButtonBuilder
import dev.kord.rest.builder.message.modify.InteractionResponseModifyBuilder
import io.github.hansanto.quipoquiz.discord.framework.builder.component.CustomActionRowBuilder
import io.github.hansanto.quipoquiz.discord.framework.builder.component.CustomMessageComponentBuilder
import io.github.hansanto.quipoquiz.discord.framework.builder.message.CustomEmbedBuilder
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe

class InteractionDeltaBuilderTest : ShouldSpec({

    lateinit var builder: InteractionDeltaBuilder

    beforeTest {
        builder = InteractionDeltaBuilder()
    }

    should("properties be null by default") {
        builder.embeds shouldBe null
        builder.components shouldBe null
    }

    should("constructor from InteractionCacheBuilder") {
        val interactionCacheBuilder = InteractionCacheBuilder(
            embeds = MutableList<CustomEmbedBuilder>(10) {
                CustomEmbedBuilder().apply {
                    title = "Title$it"
                    this.footer {
                        text = "Footer$it"
                    }
                }
            },
            components = MutableList<CustomMessageComponentBuilder>(10) {
                CustomActionRowBuilder().apply {
                    interactionButton(
                        style = ButtonStyle.Primary,
                        customId = "CustomId$it"
                    ) {
                        label = "Button$it"
                    }
                }
            }
        )

        val deltaBuilder = InteractionDeltaBuilder(interactionCacheBuilder)
        deltaBuilder.embeds shouldBe interactionCacheBuilder.embeds
        deltaBuilder.components shouldBe interactionCacheBuilder.components
    }

    should("embed add embed to the list") {
        addEmbed(builder)
        builder.embeds shouldBe listOf(
            CustomEmbedBuilder().apply {
                title = "Title1"
                this.footer {
                    text = "Footer1"
                }
            }
        )

        addEmbed(builder, "2")

        builder.embeds shouldBe listOf(
            CustomEmbedBuilder().apply {
                title = "Title1"
                this.footer {
                    text = "Footer1"
                }
            },
            CustomEmbedBuilder().apply {
                title = "Title2"
                this.footer {
                    text = "Footer2"
                }
            }
        )
    }

    should("actionRow add action row to the list") {
        addButton(builder)
        builder.components shouldBe listOf(
            CustomActionRowBuilder().apply {
                interactionButton(
                    style = ButtonStyle.Primary,
                    customId = "CustomId1"
                ) {
                    label = "Button1"
                }
            }
        )

        addButton(builder, "2")

        builder.components shouldBe listOf(
            CustomActionRowBuilder().apply {
                interactionButton(
                    style = ButtonStyle.Primary,
                    customId = "CustomId1"
                ) {
                    label = "Button1"
                }
            },
            CustomActionRowBuilder().apply {
                interactionButton(
                    style = ButtonStyle.Primary,
                    customId = "CustomId2"
                ) {
                    label = "Button2"
                }
            }
        )
    }

    should("hasUpdate return false if all properties are null") {
        builder.hasUpdate() shouldBe false
    }

    should("hasUpdate return true if embeds is not null") {
        addEmbed(builder)
        builder.hasUpdate() shouldBe true
    }

    should("hasUpdate return true if components is not null") {
        addButton(builder)
        builder.hasUpdate() shouldBe true
    }

    should("hasUpdate return true if embeds and components are not null") {
        addEmbed(builder)
        addButton(builder)
        builder.hasUpdate() shouldBe true
    }

    should("applyUpdate do nothing if all properties are null") {
        val modifyBuilder = InteractionResponseModifyBuilder()

        builder.applyUpdate(modifyBuilder)

        modifyBuilder.embeds shouldBe null
        modifyBuilder.components shouldBe null
        modifyBuilder.flags shouldBe null
        modifyBuilder.attachments shouldBe null
        modifyBuilder.allowedMentions shouldBe null
        modifyBuilder.suppressEmbeds shouldBe false
    }

    should("applyUpdate update embeds if embeds is not null") {
        val modifyBuilder = InteractionResponseModifyBuilder()
        addEmbed(builder)

        builder.applyUpdate(modifyBuilder)

        val embeds = modifyBuilder.embeds!!
        embeds.size shouldBe 1
        embeds[0].title shouldBe "Title1"
        embeds[0].footer!!.text shouldBe "Footer1"
        modifyBuilder.components shouldBe null
        modifyBuilder.flags shouldBe null
        modifyBuilder.attachments shouldBe null
        modifyBuilder.allowedMentions shouldBe null
        modifyBuilder.suppressEmbeds shouldBe false
    }

    should("applyUpdate update components if components is not null") {
        val modifyBuilder = InteractionResponseModifyBuilder()
        addButton(builder)

        builder.applyUpdate(modifyBuilder)

        modifyBuilder.embeds shouldBe null
        val components = modifyBuilder.components!!
        components.size shouldBe 1
        val firstRow = components[0] as ActionRowBuilder
        firstRow.components.size shouldBe 1
        val firstComponent = firstRow.components[0] as ButtonBuilder.InteractionButtonBuilder
        firstComponent.customId shouldBe "CustomId1"
        firstComponent.label shouldBe "Button1"
        firstComponent.style shouldBe ButtonStyle.Primary

        modifyBuilder.flags shouldBe null
        modifyBuilder.attachments shouldBe null
        modifyBuilder.allowedMentions shouldBe null
        modifyBuilder.suppressEmbeds shouldBe false
    }

    should("applyUpdate update embeds and components if they are not null") {
        val modifyBuilder = InteractionResponseModifyBuilder()
        builder.embed {
            title = "a"
            this.footer {
                text = "b"
            }
        }
        builder.actionRow {
            interactionButton(
                style = ButtonStyle.Danger,
                customId = "c"
            ) {
                label = "d"
            }
        }

        builder.applyUpdate(modifyBuilder)

        val embeds = modifyBuilder.embeds!!
        embeds.size shouldBe 1
        embeds[0].title shouldBe "a"
        embeds[0].footer!!.text shouldBe "b"

        val components = modifyBuilder.components!!
        components.size shouldBe 1
        val firstRow = components[0] as ActionRowBuilder
        firstRow.components.size shouldBe 1
        val firstComponent = firstRow.components[0] as ButtonBuilder.InteractionButtonBuilder
        firstComponent.customId shouldBe "c"
        firstComponent.label shouldBe "d"
        firstComponent.style shouldBe ButtonStyle.Danger

        modifyBuilder.flags shouldBe null
        modifyBuilder.attachments shouldBe null
        modifyBuilder.allowedMentions shouldBe null
        modifyBuilder.suppressEmbeds shouldBe false
    }

    should("equals return false if embeds and components are different") {
        val other = InteractionDeltaBuilder()
        addEmbed(builder)
        addButton(builder)

        other.embed {
            title = "B"
            this.footer {
                text = "C"
            }
        }
        other.actionRow {
            interactionButton(
                style = ButtonStyle.Danger,
                customId = "D"
            ) {
                label = "E"
            }
        }
        builder shouldNotBe other
    }

    should("equals return true if embeds and components are equal") {
        val other = InteractionDeltaBuilder()

        addEmbed(builder)
        addButton(builder)

        addEmbed(other)
        addButton(other)

        builder shouldBe other
    }

    should("hashCode return different values if embeds and components are different") {
        val other = InteractionDeltaBuilder()
        addEmbed(builder)
        addButton(builder)

        other.embed {
            title = "B"
            this.footer {
                text = "C"
            }
        }
        other.actionRow {
            interactionButton(
                style = ButtonStyle.Danger,
                customId = "D"
            ) {
                label = "E"
            }
        }
        builder.hashCode() shouldNotBe other.hashCode()
    }

    should("hashCode return same values if embeds and components are equal") {
        val other = InteractionDeltaBuilder()

        addEmbed(builder)
        addButton(builder)

        addEmbed(other)
        addButton(other)

        builder.hashCode() shouldBe other.hashCode()
    }
})

private fun addButton(builder: InteractionDeltaBuilder, id: String = "1") {
    builder.actionRow {
        interactionButton(
            style = ButtonStyle.Primary,
            customId = "CustomId$id"
        ) {
            label = "Button$id"
        }
    }
}

private fun addEmbed(builder: InteractionDeltaBuilder, id: String = "1") {
    builder.embed {
        title = "Title$id"
        this.footer {
            text = "Footer$id"
        }
    }
}
