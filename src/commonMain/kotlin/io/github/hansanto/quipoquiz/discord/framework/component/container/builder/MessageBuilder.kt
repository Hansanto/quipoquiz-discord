package io.github.hansanto.quipoquiz.discord.framework.component.container.builder

interface MessageBuilder :
    MultipleRowMessageBuilder,
    EmbedMessageBuilder,
    GroupMessageBuilder {

    /**
     * Remove all components from the builder and cancel them.
     */
    suspend fun removeAndCancelComponents() {
        getGroupComponents().forEach { it.cancel() }
        removeGroupComponents()

        getEmbedComponents().forEach { it.cancel() }
        removeEmbedComponents()

        getRowComponents().forEach { it.cancel() }
        removeRowComponents()
    }
}

class MessageBuilderImpl :
    MessageBuilder,
    MultipleRowMessageBuilder by MultipleRowMessageBuilderImpl(),
    EmbedMessageBuilder by SingleEmbedMessageBuilderImpl(),
    GroupMessageBuilder by GroupMessageBuilderImpl()
