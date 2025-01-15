package io.github.hansanto.quipoquiz.discord.framework.builder.message

import dev.kord.common.Color
import dev.kord.rest.builder.message.EmbedBuilder
import dev.kord.rest.builder.message.EmbedBuilder.Companion.ZERO_WIDTH_SPACE
import io.github.hansanto.quipoquiz.discord.framework.builder.ConvertableKordBuilder
import io.github.hansanto.quipoquiz.discord.framework.builder.convert
import io.github.hansanto.quipoquiz.extension.truncate
import kotlinx.datetime.Instant

enum class EmbedFooterAddPosition {
    LEFT,
    RIGHT
}

class CustomEmbedBuilder : ConvertableKordBuilder<EmbedBuilder> {

    /**
     * @see EmbedBuilder.title
     */
    var title: String? = null
        set(value) {
            field = value?.truncate(EmbedBuilder.Limits.title)
        }

    /**
     * @see EmbedBuilder.description
     */
    var description: String? = null
        set(value) {
            field = value?.truncate(EmbedBuilder.Limits.description)
        }

    /**
     * @see EmbedBuilder.url
     */
    var url: String? = null

    /**
     * @see EmbedBuilder.timestamp
     */
    var timestamp: Instant? = null

    /**
     * @see EmbedBuilder.color
     */
    var color: Color? = null

    /**
     * @see EmbedBuilder.image
     */
    var image: String? = null

    /**
     * @see EmbedBuilder.footer
     */
    var footer: Footer? = null

    /**
     * @see EmbedBuilder.thumbnail
     */
    var thumbnail: Thumbnail? = null

    /**
     * @see EmbedBuilder.author
     */
    var author: Author? = null

    /**
     * @see EmbedBuilder.fields
     */
    var fields: MutableList<Field> = mutableListOf()

    /**
     * Adds or updates the [footer] as configured by the [builder].
     */
    inline fun footer(builder: Footer.() -> Unit) {
        footer = (footer ?: Footer()).apply(builder)
    }

    /**
     * Adds or updates the [thumbnail] as configured by the [builder].
     */
    inline fun thumbnail(builder: Thumbnail.() -> Unit) {
        thumbnail = (thumbnail ?: Thumbnail()).apply(builder)
    }

    /**
     * Adds or updates the [author] as configured by the [builder].
     */
    inline fun author(builder: Author.() -> Unit) {
        author = (author ?: Author()).apply(builder)
    }

    /**
     * Adds a new [Field] configured by the [builder].
     */
    inline fun field(builder: Field.() -> Unit) {
        fields.add(Field().apply(builder))
    }

    override fun convert(): EmbedBuilder {
        return EmbedBuilder().also {
            it.title = title
            it.description = description
            it.url = url
            it.timestamp = timestamp
            it.color = color
            it.image = image
            it.footer = footer?.convert()
            it.thumbnail = thumbnail?.convert()
            it.author = author?.convert()
            it.fields = fields.convert()
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is CustomEmbedBuilder) return false

        if (title != other.title) return false
        if (description != other.description) return false
        if (url != other.url) return false
        if (timestamp != other.timestamp) return false
        if (color != other.color) return false
        if (image != other.image) return false
        if (footer != other.footer) return false
        if (thumbnail != other.thumbnail) return false
        if (author != other.author) return false
        if (fields != other.fields) return false

        return true
    }

    override fun hashCode(): Int {
        var result = title.hashCode()
        result = 31 * result + description.hashCode()
        result = 31 * result + url.hashCode()
        result = 31 * result + timestamp.hashCode()
        result = 31 * result + color.hashCode()
        result = 31 * result + image.hashCode()
        result = 31 * result + footer.hashCode()
        result = 31 * result + thumbnail.hashCode()
        result = 31 * result + author.hashCode()
        result = 31 * result + fields.hashCode()
        return result
    }

    override fun toString(): String {
        return "CustomEmbedBuilder(" +
            "title=$title, " +
            "description=$description, " +
            "url=$url, " +
            "timestamp=$timestamp, " +
            "color=$color, " +
            "image=$image, " +
            "footer=$footer, " +
            "thumbnail=$thumbnail, " +
            "author=$author, " +
            "fields=$fields" +
            ")"
    }

    class Thumbnail : ConvertableKordBuilder<EmbedBuilder.Thumbnail> {

        /**
         * @see EmbedBuilder.Thumbnail.url
         */
        var url: String? = null

        override fun convert(): EmbedBuilder.Thumbnail {
            return EmbedBuilder.Thumbnail().also {
                url?.let { url -> it.url = url }
            }
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is Thumbnail) return false

            if (url != other.url) return false

            return true
        }

        override fun hashCode(): Int {
            return url.hashCode()
        }

        override fun toString(): String {
            return "Thumbnail(url='$url')"
        }
    }

    class Footer : ConvertableKordBuilder<EmbedBuilder.Footer> {

        /**
         * @see EmbedBuilder.Footer.text
         */
        var text: String = ""
            set(value) {
                field = value.truncate(EmbedBuilder.Footer.Limits.text)
            }

        /**
         * @see EmbedBuilder.Footer.icon
         */
        var icon: String? = null

        /**
         * Add a text to the footer of the embed.
         * If the footer already has a text, the new text will be added to the current text.
         * Otherwise, the new text will be set as the text of the footer.
         * @receiver EmbedBuilder.
         * @param newText Text to add.
         * @param addPosition Position to add the new text.
         * @param separator Separator between the current text and the new text.
         */
        fun addText(
            newText: String,
            addPosition: EmbedFooterAddPosition = EmbedFooterAddPosition.RIGHT,
            separator: String = " • "
        ) {
            val currentText = this.text
            if (currentText.isNotBlank()) {
                this.text = when (addPosition) {
                    EmbedFooterAddPosition.LEFT -> "$newText$separator$currentText"
                    EmbedFooterAddPosition.RIGHT -> "${currentText}$separator$newText"
                }
            } else {
                this.text = newText
            }
        }

        override fun convert(): EmbedBuilder.Footer {
            return EmbedBuilder.Footer().also {
                it.text = text
                it.icon = icon
            }
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is Footer) return false

            if (text != other.text) return false
            if (icon != other.icon) return false

            return true
        }

        override fun hashCode(): Int {
            var result = text.hashCode()
            result = 31 * result + icon.hashCode()
            return result
        }

        override fun toString(): String {
            return "Footer(text='$text', icon='$icon')"
        }
    }

    class Author : ConvertableKordBuilder<EmbedBuilder.Author> {

        /**
         * @see EmbedBuilder.Author.name
         */
        var name: String? = null
            set(value) {
                field = value?.truncate(EmbedBuilder.Author.Limits.name)
            }

        /**
         * @see EmbedBuilder.Author.url
         */
        var url: String? = null

        /**
         * @see EmbedBuilder.Author.icon
         */
        var icon: String? = null

        override fun convert(): EmbedBuilder.Author {
            return EmbedBuilder.Author().also {
                it.name = name
                it.url = url
                it.icon = icon
            }
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is Author) return false

            if (name != other.name) return false
            if (url != other.url) return false
            if (icon != other.icon) return false

            return true
        }

        override fun hashCode(): Int {
            var result = name.hashCode()
            result = 31 * result + url.hashCode()
            result = 31 * result + icon.hashCode()
            return result
        }

        override fun toString(): String {
            return "Author(name='$name', url='$url', icon='$icon')"
        }
    }

    class Field : ConvertableKordBuilder<EmbedBuilder.Field> {

        /**
         * @see EmbedBuilder.Field.value
         */
        var value: String = ZERO_WIDTH_SPACE
            set(value) {
                field = value.truncate(EmbedBuilder.Field.Limits.value)
            }

        /**
         * @see EmbedBuilder.Field.name
         */
        var name: String = ZERO_WIDTH_SPACE
            set(value) {
                field = value.truncate(EmbedBuilder.Field.Limits.name)
            }

        /**
         * @see EmbedBuilder.Field.inline
         */
        var inline: Boolean = false

        override fun convert(): EmbedBuilder.Field {
            return EmbedBuilder.Field().also {
                it.value = value
                it.name = name
                it.inline = inline
            }
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is Field) return false

            if (value != other.value) return false
            if (name != other.name) return false
            if (inline != other.inline) return false

            return true
        }

        override fun hashCode(): Int {
            var result = value.hashCode()
            result = 31 * result + name.hashCode()
            result = 31 * result + inline.hashCode()
            return result
        }

        override fun toString(): String {
            return "Field(value='$value', name='$name', inline=$inline)"
        }
    }
}
