package io.github.hansanto.quipoquiz.discord.framework.component.container.builder

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe

class RowMessageBuilderTest : ShouldSpec({

    should("firstRow should be 0") {
        RowMessageBuilder.FIRST_ROW shouldBe 0
    }

    should("secondRow should be 1") {
        RowMessageBuilder.SECOND_ROW shouldBe 1
    }

    should("thirdRow should be 2") {
        RowMessageBuilder.THIRD_ROW shouldBe 2
    }

    should("fourthRow should be 3") {
        RowMessageBuilder.FOURTH_ROW shouldBe 3
    }
})
