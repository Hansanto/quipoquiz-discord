package io.github.hansanto.quipoquiz.discord.framework.builder.message

import dev.kord.rest.builder.message.EmbedBuilder
import io.github.hansanto.quipoquiz.util.randomBoolean
import io.github.hansanto.quipoquiz.util.randomColor
import io.github.hansanto.quipoquiz.util.randomInstant
import io.github.hansanto.quipoquiz.util.randomString
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe

class CustomEmbedBuilderTest : ShouldSpec({

    should("set title to null if value is null") {
        val customEmbedBuilder = CustomEmbedBuilder().apply {
            title = null
        }
        customEmbedBuilder.title shouldBe null
    }

    should("set title is not truncated if value is less than limit") {
        val title = randomString(size = EmbedBuilder.Limits.title)
        val customEmbedBuilder = CustomEmbedBuilder().apply {
            this.title = title
        }
        customEmbedBuilder.title shouldBe title
    }

    should("set title is truncated if value is more than limit") {
        val title = "a".repeat(EmbedBuilder.Limits.title - 5) + "123456789"
        val customEmbedBuilder = CustomEmbedBuilder().apply {
            this.title = title
        }
        customEmbedBuilder.title.let {
            it!!.length shouldBe EmbedBuilder.Limits.title
            it.endsWith("123..") shouldBe true
        }
    }

    should("set description to null if value is null") {
        val customEmbedBuilder = CustomEmbedBuilder().apply {
            description = null
        }
        customEmbedBuilder.description shouldBe null
    }

    should("set description is not truncated if value is less than limit") {
        val description = randomString(size = EmbedBuilder.Limits.description)
        val customEmbedBuilder = CustomEmbedBuilder().apply {
            this.description = description
        }
        customEmbedBuilder.description shouldBe description
    }

    should("set description is truncated if value is more than limit") {
        val description = "a".repeat(EmbedBuilder.Limits.description - 5) + "123456789"
        val customEmbedBuilder = CustomEmbedBuilder().apply {
            this.description = description
        }
        customEmbedBuilder.description.let {
            it!!.length shouldBe EmbedBuilder.Limits.description
            it.endsWith("123..") shouldBe true
        }
    }

    should("equals return true if instances are empty") {
        val customEmbedBuilder1 = CustomEmbedBuilder()
        val customEmbedBuilder2 = CustomEmbedBuilder()
        customEmbedBuilder1 shouldBe customEmbedBuilder2
    }

    should("equals return true if instances are not empty") {
        val src = randomEmbedBuilder()
        val customEmbedBuilder1 = copy(src)
        val customEmbedBuilder2 = copy(src)
        customEmbedBuilder1 shouldBe customEmbedBuilder2
    }

    should("equals return false if title is different") {
        checkIfNotEqual {
            title = randomString()
        }
    }

    should("equals return false if description is different") {
        checkIfNotEqual {
            description = randomString()
        }
    }

    should("equals return false if url is different") {
        checkIfNotEqual {
            url = randomString()
        }
    }

    should("equals return false if timestamp is different") {
        checkIfNotEqual {
            timestamp = randomInstant()
        }
    }

    should("equals return false if color is different") {
        checkIfNotEqual {
            color = randomColor()
        }
    }

    should("equals return false if image is different") {
        checkIfNotEqual {
            image = randomString()
        }
    }

    should("equals return false if footer is different") {
        checkIfNotEqual {
            footer = CustomEmbedBuilder.Footer().apply {
                text = randomString()
                icon = randomString()
            }
        }
    }

    should("equals return false if thumbnail is different") {
        checkIfNotEqual {
            thumbnail = CustomEmbedBuilder.Thumbnail().apply {
                url = randomString()
            }
        }
    }

    should("equals return false if author is different") {
        checkIfNotEqual {
            author = CustomEmbedBuilder.Author().apply {
                name = randomString()
                url = randomString()
                icon = randomString()
            }
        }
    }

    should("equals return false if fields is different") {
        checkIfNotEqual {
            fields.add(
                CustomEmbedBuilder.Field().apply {
                    name = randomString()
                    value = randomString()
                    inline = randomBoolean()
                }
            )
        }
    }

    should("hashcode return true if instances are empty") {
        val customEmbedBuilder1 = CustomEmbedBuilder()
        val customEmbedBuilder2 = CustomEmbedBuilder()
        customEmbedBuilder1.hashCode() shouldBe customEmbedBuilder2.hashCode()
    }

    should("hashcode return true if instances are not empty") {
        val src = randomEmbedBuilder()
        val customEmbedBuilder1 = copy(src)
        val customEmbedBuilder2 = copy(src)
        customEmbedBuilder1.hashCode() shouldBe customEmbedBuilder2.hashCode()
    }

    should("hashcode return false if title is different") {
        checkIfHashcodeNotEqual {
            title = randomString()
        }
    }

    should("hashcode return false if description is different") {
        checkIfHashcodeNotEqual {
            description = randomString()
        }
    }

    should("hashcode return false if url is different") {
        checkIfHashcodeNotEqual {
            url = randomString()
        }
    }

    should("hashcode return false if timestamp is different") {
        checkIfHashcodeNotEqual {
            timestamp = randomInstant()
        }
    }

    should("hashcode return false if color is different") {
        checkIfHashcodeNotEqual {
            color = randomColor()
        }
    }

    should("hashcode return false if image is different") {
        checkIfHashcodeNotEqual {
            image = randomString()
        }
    }

    should("hashcode return false if footer is different") {
        checkIfHashcodeNotEqual {
            footer = CustomEmbedBuilder.Footer().apply {
                text = randomString()
                icon = randomString()
            }
        }
    }

    should("hashcode return false if thumbnail is different") {
        checkIfHashcodeNotEqual {
            thumbnail = CustomEmbedBuilder.Thumbnail().apply {
                url = randomString()
            }
        }
    }

    should("hashcode return false if author is different") {
        checkIfHashcodeNotEqual {
            author = CustomEmbedBuilder.Author().apply {
                name = randomString()
                url = randomString()
                icon = randomString()
            }
        }
    }

    should("hashcode return false if fields is different") {
        checkIfHashcodeNotEqual {
            fields.add(
                CustomEmbedBuilder.Field().apply {
                    name = randomString()
                    value = randomString()
                    inline = randomBoolean()
                }
            )
        }
    }

    should("embed builder can be converted to kord object") {
        val title = randomString()
        val description = randomString()
        val url = randomString()
        val timestamp = randomInstant()
        val color = randomColor()
        val image = randomString()
        val footer = CustomEmbedBuilder.Footer().apply {
            text = randomString()
            icon = randomString()
        }
        val thumbnail = CustomEmbedBuilder.Thumbnail().apply {
            this.url = randomString()
        }
        val author = CustomEmbedBuilder.Author().apply {
            this.name = randomString()
            this.url = randomString()
            this.icon = randomString()
        }
        val fields = List(5) {
            CustomEmbedBuilder.Field().apply {
                this.name = randomString()
                this.value = randomString()
                this.inline = randomBoolean()
            }
        }
        val customEmbedBuilder = CustomEmbedBuilder().apply {
            this.title = title
            this.description = description
            this.url = url
            this.timestamp = timestamp
            this.color = color
            this.image = image
            this.footer = footer
            this.thumbnail = thumbnail
            this.author = author
            this.fields = fields.toMutableList()
        }
        val converted = customEmbedBuilder.convert()
        converted.title shouldBe title
        converted.description shouldBe description
        converted.url shouldBe url
        converted.timestamp shouldBe timestamp
        converted.color shouldBe color
        converted.image shouldBe image
        converted.footer?.text shouldBe footer.text
        converted.footer?.icon shouldBe footer.icon
        converted.thumbnail?.url shouldBe thumbnail.url
        converted.author?.name shouldBe author.name
        converted.author?.url shouldBe author.url
        converted.author?.icon shouldBe author.icon
        converted.fields.size shouldBe fields.size
        converted.fields.forEachIndexed { index, field ->
            field.name shouldBe fields[index].name
            field.value shouldBe fields[index].value
            field.inline shouldBe fields[index].inline
        }
    }

    should("footer builder create a footer object") {
        val text = randomString()
        val icon = randomString()
        val embedBuilder = CustomEmbedBuilder().apply {
            this.footer {
                this.text = text
                this.icon = icon
            }
        }
        embedBuilder.footer?.text shouldBe text
        embedBuilder.footer?.icon shouldBe icon
    }

    should("footer builder update a footer object") {
        val text = randomString()
        val icon = randomString()
        val icon2 = randomString()
        val embedBuilder = CustomEmbedBuilder().apply {
            this.footer {
                this.text = text
                this.icon = icon
            }
            this.footer {
                this.icon = icon2
            }
        }
        embedBuilder.footer?.text shouldBe text
        embedBuilder.footer?.icon shouldBe icon2
    }

    should("footer set text is not truncated if value is less than limit") {
        val text = randomString(size = EmbedBuilder.Footer.Limits.text)
        val footer = CustomEmbedBuilder.Footer().apply {
            this.text = text
        }
        footer.text shouldBe text
    }

    should("footer set text is truncated if value is more than limit") {
        val text = "a".repeat(EmbedBuilder.Footer.Limits.text - 5) + "123456789"
        val footer = CustomEmbedBuilder.Footer().apply {
            this.text = text
        }
        footer.text.let {
            it.length shouldBe EmbedBuilder.Footer.Limits.text
            it.endsWith("123..") shouldBe true
        }
    }

    should("thumbnail builder create a thumbnail object") {
        val url = randomString()
        val embedBuilder = CustomEmbedBuilder().apply {
            this.thumbnail {
                this.url = url
            }
        }
        embedBuilder.thumbnail?.url shouldBe url
    }

    should("thumbnail builder update a thumbnail object") {
        val url = randomString()
        val url2 = randomString()
        val embedBuilder = CustomEmbedBuilder().apply {
            this.thumbnail {
                this.url = url
            }
            this.thumbnail {
                this.url = url2
            }
        }
        embedBuilder.thumbnail?.url shouldBe url2
    }

    should("field builder create a field object") {
        val name = randomString()
        val value = randomString()
        val inline = randomBoolean()

        val name2 = randomString()
        val value2 = randomString()
        val inline2 = randomBoolean()
        val embedBuilder = CustomEmbedBuilder().apply {
            this.field {
                this.name = name
                this.value = value
                this.inline = inline
            }
            this.field {
                this.name = name2
                this.value = value2
                this.inline = inline2
            }
        }
        embedBuilder.fields.size shouldBe 2
        embedBuilder.fields[0].name shouldBe name
        embedBuilder.fields[0].value shouldBe value
        embedBuilder.fields[0].inline shouldBe inline
        embedBuilder.fields[1].name shouldBe name2
        embedBuilder.fields[1].value shouldBe value2
        embedBuilder.fields[1].inline shouldBe inline2
    }

    should("author builder create a author object") {
        val name = randomString()
        val url = randomString()
        val icon = randomString()
        val embedBuilder = CustomEmbedBuilder().apply {
            this.author {
                this.name = name
                this.url = url
                this.icon = icon
            }
        }
        embedBuilder.author?.name shouldBe name
        embedBuilder.author?.url shouldBe url
        embedBuilder.author?.icon shouldBe icon
    }

    should("author builder update a author object") {
        val name = randomString()
        val url = randomString()
        val icon = randomString()
        val icon2 = randomString()
        val embedBuilder = CustomEmbedBuilder().apply {
            this.author {
                this.name = name
                this.url = url
                this.icon = icon
            }
            this.author {
                this.icon = icon2
            }
        }
        embedBuilder.author?.name shouldBe name
        embedBuilder.author?.url shouldBe url
        embedBuilder.author?.icon shouldBe icon2
    }

    should("author set name to null if value is null") {
        val author = CustomEmbedBuilder.Author().apply {
            name = null
        }
        author.name shouldBe null
    }

    should("author set name is not truncated if value is less than limit") {
        val name = randomString(size = EmbedBuilder.Author.Limits.name)
        val author = CustomEmbedBuilder.Author().apply {
            this.name = name
        }
        author.name shouldBe name
    }

    should("author set name is truncated if value is more than limit") {
        val name = "a".repeat(EmbedBuilder.Author.Limits.name - 5) + "123456789"
        val author = CustomEmbedBuilder.Author().apply {
            this.name = name
        }
        author.name.let {
            it!!.length shouldBe EmbedBuilder.Author.Limits.name
            it.endsWith("123..") shouldBe true
        }
    }

    should("thumbnail can be converted to kord object") {
        val url = randomString()
        val thumbnail = CustomEmbedBuilder.Thumbnail().apply {
            this.url = url
        }
        val converted = thumbnail.convert()
        converted.url shouldBe url
    }

    should("thumbnail equals return true if instances are empty") {
        val thumbnail1 = CustomEmbedBuilder.Thumbnail()
        val thumbnail2 = CustomEmbedBuilder.Thumbnail()
        thumbnail1 shouldBe thumbnail2
    }

    should("thumbnail equals return true if instances are not empty") {
        val url = randomString()
        val thumbnail1 = CustomEmbedBuilder.Thumbnail().apply {
            this.url = url
        }
        val thumbnail2 = CustomEmbedBuilder.Thumbnail().apply {
            this.url = url
        }
        thumbnail1 shouldBe thumbnail2
    }

    should("thumbnail equals return false if url is different") {
        val thumbnail1 = CustomEmbedBuilder.Thumbnail().apply {
            this.url = randomString()
        }
        val thumbnail2 = CustomEmbedBuilder.Thumbnail().apply {
            this.url = randomString()
        }
        thumbnail1 shouldNotBe thumbnail2
    }

    should("thumbnail hashcode return true if instances are empty") {
        val thumbnail1 = CustomEmbedBuilder.Thumbnail()
        val thumbnail2 = CustomEmbedBuilder.Thumbnail()
        thumbnail1.hashCode() shouldBe thumbnail2.hashCode()
    }

    should("thumbnail hashcode return true if instances are not empty") {
        val url = randomString()
        val thumbnail1 = CustomEmbedBuilder.Thumbnail().apply {
            this.url = url
        }
        val thumbnail2 = CustomEmbedBuilder.Thumbnail().apply {
            this.url = url
        }
        thumbnail1.hashCode() shouldBe thumbnail2.hashCode()
    }

    should("thumbnail hashcode return false if url is different") {
        val thumbnail1 = CustomEmbedBuilder.Thumbnail().apply {
            this.url = randomString()
        }
        val thumbnail2 = CustomEmbedBuilder.Thumbnail().apply {
            this.url = randomString()
        }
        thumbnail1.hashCode() shouldNotBe thumbnail2.hashCode()
    }

    should("footer can be converted to kord object") {
        val text = randomString()
        val icon = randomString()
        val footer = CustomEmbedBuilder.Footer().apply {
            this.text = text
            this.icon = icon
        }
        val converted = footer.convert()
        converted.text shouldBe text
        converted.icon shouldBe icon
    }

    should("footer addText set text if text is empty") {
        val text = randomString()
        val footer = CustomEmbedBuilder.Footer().apply {
            addText(text)
        }
        footer.text shouldBe text
    }

    should("footer addText add text to the right if text is not empty") {
        val text = randomString()
        val text2 = randomString()
        val footer = CustomEmbedBuilder.Footer().apply {
            this.text = text
            addText(text2)
        }
        footer.text shouldBe "$text • $text2"
    }

    should("footer addText add text to the left if text is not empty") {
        val text = randomString()
        val text2 = randomString()
        val footer = CustomEmbedBuilder.Footer().apply {
            this.text = text
            addText(text2, EmbedFooterAddPosition.LEFT)
        }
        footer.text shouldBe "$text2 • $text"
    }

    should("footer addText add text with custom separator if text is not empty") {
        val text = randomString()
        val text2 = randomString()
        val separator = randomString()
        val footer = CustomEmbedBuilder.Footer().apply {
            this.text = text
            addText(text2, separator = separator)
        }
        footer.text shouldBe "$text$separator$text2"
    }

    should("footer equals return true if instances are empty") {
        val footer1 = CustomEmbedBuilder.Footer()
        val footer2 = CustomEmbedBuilder.Footer()
        footer1 shouldBe footer2
    }

    should("footer equals return true if instances are not empty") {
        val text = randomString()
        val icon = randomString()
        val footer1 = CustomEmbedBuilder.Footer().apply {
            this.text = text
            this.icon = icon
        }
        val footer2 = CustomEmbedBuilder.Footer().apply {
            this.text = text
            this.icon = icon
        }
        footer1 shouldBe footer2
    }

    should("footer equals return false if text is different") {
        val footer1 = CustomEmbedBuilder.Footer().apply {
            this.text = randomString()
        }
        val footer2 = CustomEmbedBuilder.Footer().apply {
            this.text = randomString()
        }
        footer1 shouldNotBe footer2
    }

    should("footer equals return false if icon is different") {
        val footer1 = CustomEmbedBuilder.Footer().apply {
            this.icon = randomString()
        }
        val footer2 = CustomEmbedBuilder.Footer().apply {
            this.icon = randomString()
        }
        footer1 shouldNotBe footer2
    }

    should("footer hashcode return true if instances are empty") {
        val footer1 = CustomEmbedBuilder.Footer()
        val footer2 = CustomEmbedBuilder.Footer()
        footer1.hashCode() shouldBe footer2.hashCode()
    }

    should("footer hashcode return true if instances are not empty") {
        val text = randomString()
        val icon = randomString()
        val footer1 = CustomEmbedBuilder.Footer().apply {
            this.text = text
            this.icon = icon
        }
        val footer2 = CustomEmbedBuilder.Footer().apply {
            this.text = text
            this.icon = icon
        }
        footer1.hashCode() shouldBe footer2.hashCode()
    }

    should("footer hashcode return false if text is different") {
        val footer1 = CustomEmbedBuilder.Footer().apply {
            this.text = randomString()
        }
        val footer2 = CustomEmbedBuilder.Footer().apply {
            this.text = randomString()
        }
        footer1.hashCode() shouldNotBe footer2.hashCode()
    }

    should("author can be converted to kord object") {
        val name = randomString()
        val url = randomString()
        val icon = randomString()
        val author = CustomEmbedBuilder.Author().apply {
            this.name = name
            this.url = url
            this.icon = icon
        }
        val converted = author.convert()
        converted.name shouldBe name
        converted.url shouldBe url
        converted.icon shouldBe icon
    }

    should("author equals return true if instances are empty") {
        val author1 = CustomEmbedBuilder.Author()
        val author2 = CustomEmbedBuilder.Author()
        author1 shouldBe author2
    }

    should("author equals return true if instances are not empty") {
        val name = randomString()
        val url = randomString()
        val icon = randomString()
        val author1 = CustomEmbedBuilder.Author().apply {
            this.name = name
            this.url = url
            this.icon = icon
        }
        val author2 = CustomEmbedBuilder.Author().apply {
            this.name = name
            this.url = url
            this.icon = icon
        }
        author1 shouldBe author2
    }

    should("author equals return false if name is different") {
        val author1 = CustomEmbedBuilder.Author().apply {
            this.name = randomString()
        }
        val author2 = CustomEmbedBuilder.Author().apply {
            this.name = randomString()
        }
        author1 shouldNotBe author2
    }

    should("author equals return false if url is different") {
        val author1 = CustomEmbedBuilder.Author().apply {
            this.url = randomString()
        }
        val author2 = CustomEmbedBuilder.Author().apply {
            this.url = randomString()
        }
        author1 shouldNotBe author2
    }

    should("author equals return false if icon is different") {
        val author1 = CustomEmbedBuilder.Author().apply {
            this.icon = randomString()
        }
        val author2 = CustomEmbedBuilder.Author().apply {
            this.icon = randomString()
        }
        author1 shouldNotBe author2
    }

    should("author hashcode return true if instances are empty") {
        val author1 = CustomEmbedBuilder.Author()
        val author2 = CustomEmbedBuilder.Author()
        author1.hashCode() shouldBe author2.hashCode()
    }

    should("author hashcode return true if instances are not empty") {
        val name = randomString()
        val url = randomString()
        val icon = randomString()
        val author1 = CustomEmbedBuilder.Author().apply {
            this.name = name
            this.url = url
            this.icon = icon
        }
        val author2 = CustomEmbedBuilder.Author().apply {
            this.name = name
            this.url = url
            this.icon = icon
        }
        author1.hashCode() shouldBe author2.hashCode()
    }

    should("author hashcode return false if name is different") {
        val author1 = CustomEmbedBuilder.Author().apply {
            this.name = randomString()
        }
        val author2 = CustomEmbedBuilder.Author().apply {
            this.name = randomString()
        }
        author1.hashCode() shouldNotBe author2.hashCode()
    }

    should("author hashcode return false if url is different") {
        val author1 = CustomEmbedBuilder.Author().apply {
            this.url = randomString()
        }
        val author2 = CustomEmbedBuilder.Author().apply {
            this.url = randomString()
        }
        author1.hashCode() shouldNotBe author2.hashCode()
    }

    should("author hashcode return false if icon is different") {
        val author1 = CustomEmbedBuilder.Author().apply {
            this.icon = randomString()
        }
        val author2 = CustomEmbedBuilder.Author().apply {
            this.icon = randomString()
        }
        author1.hashCode() shouldNotBe author2.hashCode()
    }

    should("field can be converted to kord object") {
        val name = randomString()
        val value = randomString()
        val inline = randomBoolean()
        val field = CustomEmbedBuilder.Field().apply {
            this.name = name
            this.value = value
            this.inline = inline
        }
        val converted = field.convert()
        converted.name shouldBe name
        converted.value shouldBe value
        converted.inline shouldBe inline
    }

    should("field equals return true if instances are empty") {
        val field1 = CustomEmbedBuilder.Field()
        val field2 = CustomEmbedBuilder.Field()
        field1 shouldBe field2
    }

    should("field equals return true if instances are not empty") {
        val name = randomString()
        val value = randomString()
        val inline = randomBoolean()
        val field1 = CustomEmbedBuilder.Field().apply {
            this.name = name
            this.value = value
            this.inline = inline
        }
        val field2 = CustomEmbedBuilder.Field().apply {
            this.name = name
            this.value = value
            this.inline = inline
        }
        field1 shouldBe field2
    }

    should("field equals return false if name is different") {
        val field1 = CustomEmbedBuilder.Field().apply {
            this.name = randomString()
        }
        val field2 = CustomEmbedBuilder.Field().apply {
            this.name = randomString()
        }
        field1 shouldNotBe field2
    }

    should("field equals return false if value is different") {
        val field1 = CustomEmbedBuilder.Field().apply {
            this.value = randomString()
        }
        val field2 = CustomEmbedBuilder.Field().apply {
            this.value = randomString()
        }
        field1 shouldNotBe field2
    }

    should("field equals return false if inline is different") {
        val field1 = CustomEmbedBuilder.Field().apply {
            this.inline = true
        }
        val field2 = CustomEmbedBuilder.Field().apply {
            this.inline = false
        }
        field1 shouldNotBe field2
    }

    should("field hashcode return true if instances are empty") {
        val field1 = CustomEmbedBuilder.Field()
        val field2 = CustomEmbedBuilder.Field()
        field1.hashCode() shouldBe field2.hashCode()
    }

    should("field hashcode return true if instances are not empty") {
        val name = randomString()
        val value = randomString()
        val inline = randomBoolean()
        val field1 = CustomEmbedBuilder.Field().apply {
            this.name = name
            this.value = value
            this.inline = inline
        }
        val field2 = CustomEmbedBuilder.Field().apply {
            this.name = name
            this.value = value
            this.inline = inline
        }
        field1.hashCode() shouldBe field2.hashCode()
    }

    should("field hashcode return false if name is different") {
        val field1 = CustomEmbedBuilder.Field().apply {
            this.name = randomString()
        }
        val field2 = CustomEmbedBuilder.Field().apply {
            this.name = randomString()
        }
        field1.hashCode() shouldNotBe field2.hashCode()
    }

    should("field hashcode return false if value is different") {
        val field1 = CustomEmbedBuilder.Field().apply {
            this.value = randomString()
        }
        val field2 = CustomEmbedBuilder.Field().apply {
            this.value = randomString()
        }
        field1.hashCode() shouldNotBe field2.hashCode()
    }

    should("field hashcode return false if inline is different") {
        val field1 = CustomEmbedBuilder.Field().apply {
            this.inline = true
        }
        val field2 = CustomEmbedBuilder.Field().apply {
            this.inline = false
        }
        field1.hashCode() shouldNotBe field2.hashCode()
    }

    should("field set value is not truncated if value is less than limit") {
        val value = randomString(size = EmbedBuilder.Field.Limits.value)
        val field = CustomEmbedBuilder.Field().apply {
            this.value = value
        }
        field.value shouldBe value
    }

    should("field set value is truncated if value is more than limit") {
        val value = "a".repeat(EmbedBuilder.Field.Limits.value - 5) + "123456789"
        val field = CustomEmbedBuilder.Field().apply {
            this.value = value
        }
        field.value.let {
            it.length shouldBe EmbedBuilder.Field.Limits.value
            it.endsWith("123..") shouldBe true
        }
    }

    should("field set name is not truncated if value is less than limit") {
        val name = randomString(size = EmbedBuilder.Field.Limits.name)
        val field = CustomEmbedBuilder.Field().apply {
            this.name = name
        }
        field.name shouldBe name
    }

    should("field set name is truncated if value is more than limit") {
        val name = "a".repeat(EmbedBuilder.Field.Limits.name - 5) + "123456789"
        val field = CustomEmbedBuilder.Field().apply {
            this.name = name
        }
        field.name.let {
            it.length shouldBe EmbedBuilder.Field.Limits.name
            it.endsWith("123..") shouldBe true
        }
    }
})

private inline fun checkIfNotEqual(modified: CustomEmbedBuilder.() -> Unit) {
    val src = randomEmbedBuilder()
    val customEmbedBuilder1 = copy(src)
    val customEmbedBuilder2 = copy(src).apply(modified)
    customEmbedBuilder1 shouldNotBe customEmbedBuilder2
}

private inline fun checkIfHashcodeNotEqual(modified: CustomEmbedBuilder.() -> Unit) {
    val src = randomEmbedBuilder()
    val customEmbedBuilder1 = copy(src)
    val customEmbedBuilder2 = copy(src).apply(modified)
    customEmbedBuilder1.hashCode() shouldNotBe customEmbedBuilder2.hashCode()
}

private fun randomEmbedBuilder(): CustomEmbedBuilder {
    val title = randomString()
    val description = randomString()
    val url = randomString()
    val timestamp = randomInstant()
    val color = randomColor()
    val image = randomString()
    val footer = CustomEmbedBuilder.Footer().apply {
        text = randomString()
        icon = randomString()
    }
    val thumbnail = CustomEmbedBuilder.Thumbnail().apply {
        this.url = randomString()
    }
    val author = CustomEmbedBuilder.Author().apply {
        this.name = randomString()
        this.url = randomString()
        this.icon = randomString()
    }
    val fields = List(5) {
        CustomEmbedBuilder.Field().apply {
            this.name = randomString()
            this.value = randomString()
            this.inline = randomBoolean()
        }
    }
    return CustomEmbedBuilder().apply {
        this.title = title
        this.description = description
        this.url = url
        this.timestamp = timestamp
        this.color = color
        this.image = image
        this.footer = footer
        this.thumbnail = thumbnail
        this.author = author
        this.fields = fields.toMutableList()
    }
}

private fun copy(src: CustomEmbedBuilder): CustomEmbedBuilder {
    return CustomEmbedBuilder().apply {
        title = src.title
        description = src.description
        url = src.url
        timestamp = src.timestamp
        color = src.color
        image = src.image
        footer = src.footer?.let {
            CustomEmbedBuilder.Footer().apply {
                text = it.text
                icon = it.icon
            }
        }
        thumbnail = src.thumbnail?.let {
            CustomEmbedBuilder.Thumbnail().apply {
                url = it.url
            }
        }
        author = src.author?.let {
            CustomEmbedBuilder.Author().apply {
                name = it.name
                url = it.url
                icon = it.icon
            }
        }
        fields = src.fields.map {
            CustomEmbedBuilder.Field().apply {
                name = it.name
                value = it.value
                inline = it.inline
            }
        }.toMutableList()
    }
}
