package io.github.hansanto.quipoquiz.quipoquiz

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe

class QuipoQuizSiteIdTest : ShouldSpec({

    should("FR returns 1 as site id") {
        QuipoQuizSiteId.FR.id shouldBe "1"
    }

    should("EN returns 2 as site id") {
        QuipoQuizSiteId.EN.id shouldBe "2"
    }
})
