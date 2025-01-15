package io.github.hansanto.quipoquiz.extension

import dev.kord.common.Color
import io.github.hansanto.quipoquiz.util.randomString
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe

class StringExtTest : ShouldSpec({

    should("removeHtmlTag does nothing on empty string") {
        "".removeHtmlTag() shouldBe ""
    }

    should("removeHtmlTag removes tag") {
        "<html>".removeHtmlTag() shouldBe ""
    }

    should("removeHtmlTag removes tag with content") {
        "<a>content</a>".removeHtmlTag() shouldBe "content"
    }

    should("removeHtmlTag removes multiple tags") {
        "<html>content</html><html>content</html>".removeHtmlTag() shouldBe "contentcontent"
    }

    should("removeHtmlTag removes nested tags") {
        "<html>content<p>inner</p></html>".removeHtmlTag() shouldBe "contentinner"
    }

    should("removeHtmlTag removes multiple lines tags") {
        """
            <html>
                <p>content</p>
            </html>
        """.trimIndent().removeHtmlTag().trim() shouldBe "content"
    }

    should("hexColorOrNull returns null for invalid hex color") {
        "invalid".hexColorOrNull() shouldBe null
        "#invalid".hexColorOrNull() shouldBe null
        "1".hexColorOrNull() shouldBe null
        "12".hexColorOrNull() shouldBe null
        "12345".hexColorOrNull() shouldBe null
        "#1".hexColorOrNull() shouldBe null
        "#12".hexColorOrNull() shouldBe null
        "#t12".hexColorOrNull() shouldBe null
        "#1x2".hexColorOrNull() shouldBe null
        "#12y".hexColorOrNull() shouldBe null
        "#12345".hexColorOrNull() shouldBe null
        "#1234567".hexColorOrNull() shouldBe null
        "#oo1234".hexColorOrNull() shouldBe null
        "#1234oo".hexColorOrNull() shouldBe null
        "#12oo34".hexColorOrNull() shouldBe null
    }

    should("hexColorOrNull returns color with 3 char hex") {
        "123".hexColorOrNull() shouldBe Color(0x11, 0x22, 0x33)
        "#123".hexColorOrNull() shouldBe Color(0x11, 0x22, 0x33)
        "#abc".hexColorOrNull() shouldBe Color(0xaa, 0xbb, 0xcc)
        "#ABC".hexColorOrNull() shouldBe Color(0xaa, 0xbb, 0xcc)
        "#fff".hexColorOrNull() shouldBe Color(0xff, 0xff, 0xff)
        "#FFF".hexColorOrNull() shouldBe Color(0xff, 0xff, 0xff)
        "#000".hexColorOrNull() shouldBe Color(0x00, 0x00, 0x00)
        "000".hexColorOrNull() shouldBe Color(0x00, 0x00, 0x00)
    }

    should("hexColorOrNull returns color with 6 char hex") {
        "123456".hexColorOrNull() shouldBe Color(0x12, 0x34, 0x56)
        "#123456".hexColorOrNull() shouldBe Color(0x12, 0x34, 0x56)
        "#abcdef".hexColorOrNull() shouldBe Color(0xab, 0xcd, 0xef)
        "#ABCDEF".hexColorOrNull() shouldBe Color(0xab, 0xcd, 0xef)
        "#ffffff".hexColorOrNull() shouldBe Color(0xff, 0xff, 0xff)
        "#FFFFFF".hexColorOrNull() shouldBe Color(0xff, 0xff, 0xff)
        "#000000".hexColorOrNull() shouldBe Color(0x00, 0x00, 0x00)
        "000000".hexColorOrNull() shouldBe Color(0x00, 0x00, 0x00)
    }

    should("truncate returns original string if length is less than maxLength") {
        repeat(10) {
            val str = randomString(size = it)
            str.truncate(10) shouldBe str
        }
    }

    should("truncate returns truncated string if length is greater than maxLength") {
        "".truncate(0) shouldBe ""
        "1".truncate(0) shouldBe ""
        "12".truncate(1) shouldBe "."
        "123".truncate(1) shouldBe "."
        "1234".truncate(1) shouldBe "."
        "12345".truncate(3) shouldBe "1.."
        "123456".truncate(3) shouldBe "1.."
        "1234567".truncate(5) shouldBe "123.."
        "12345678".truncate(6) shouldBe "1234.."
    }

    should("truncate returns truncated string with custom symbol") {
        "".truncate(0, "abc") shouldBe ""
        "123".truncate(3, "") shouldBe "123"
        "1234".truncate(3, "") shouldBe "123"
        "12345".truncate(3, "abc") shouldBe "abc"
        "123456".truncate(5, "abc") shouldBe "12abc"
    }
})
