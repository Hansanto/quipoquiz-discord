package io.github.hansanto.quipoquiz.discord.framework.render

import io.github.hansanto.quipoquiz.discord.framework.component.container.builder.InteractionDeltaBuilder
import io.github.hansanto.quipoquiz.discord.framework.component.container.builder.MessageBuilder

class DefaultRenderer(override val messageBuilder: MessageBuilder) : Renderer {

    override suspend fun render(builder: InteractionDeltaBuilder) {
        renderGroupComponents()
        renderEmbeds(builder)
        renderRowComponents(builder)
    }

    private suspend fun renderGroupComponents() {
        messageBuilder.getGroupComponents().forEach {
            it.render(Unit)
        }
    }

    override suspend fun renderRowComponents(builder: InteractionDeltaBuilder) {
        val rows = messageBuilder.getRows()
        if (rows.isEmpty()) {
            builder.components = mutableListOf()
            return
        }

        rows.forEach { components ->
            builder.actionRow {
                components.forEach { component ->
                    component.render(this)
                }
            }
        }
    }

    override suspend fun renderEmbeds(builder: InteractionDeltaBuilder) {
        val components = messageBuilder.getEmbedComponents()
        if (components.isEmpty()) {
            builder.embeds = mutableListOf()
            return
        }

        // Only one embed is allowed
        builder.embed {
            components.forEach {
                it.render(this)
            }
        }
    }
}
