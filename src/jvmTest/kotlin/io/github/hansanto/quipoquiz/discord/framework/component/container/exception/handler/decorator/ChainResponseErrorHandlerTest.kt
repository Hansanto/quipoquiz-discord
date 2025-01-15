package io.github.hansanto.quipoquiz.discord.framework.component.container.exception.handler.decorator

import dev.kord.core.behavior.interaction.response.DeferredMessageInteractionResponseBehavior
import dev.kord.core.behavior.interaction.response.MessageInteractionResponseBehavior
import io.github.hansanto.quipoquiz.discord.framework.component.container.exception.handler.ChainCreationResponseErrorHandler
import io.github.hansanto.quipoquiz.discord.framework.component.container.exception.handler.CreationResponseErrorHandler
import io.github.hansanto.quipoquiz.discord.framework.component.container.exception.handler.chainWith
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.mockk

class ChainResponseErrorHandlerTest : ShouldSpec({

    should("chainWith another handler") {
        val first = mockk<CreationResponseErrorHandler>()
        val second = mockk<CreationResponseErrorHandler>()
        val chain = first.chainWith(second)

        chain.first shouldBe first
        chain.second shouldBe second
    }

    should("handle DeferredMessageInteractionResponseBehavior using first and second handlers") {
        val first = mockk<CreationResponseErrorHandler> {
            coJustRun { handle(any<DeferredMessageInteractionResponseBehavior>(), any()) }
        }
        val second = mockk<CreationResponseErrorHandler> {
            coJustRun { handle(any<DeferredMessageInteractionResponseBehavior>(), any()) }
        }
        val chain = ChainCreationResponseErrorHandler(first, second)

        val response = mockk<DeferredMessageInteractionResponseBehavior>()
        val exception = Exception()

        chain.handle(response, exception)

        coVerify(exactly = 1) { first.handle(response, exception) }
        coVerify(exactly = 1) { second.handle(response, exception) }
    }

    should("handle MessageInteractionResponseBehavior using first and second handlers") {
        val first = mockk<CreationResponseErrorHandler> {
            coJustRun { handle(any<MessageInteractionResponseBehavior>(), any()) }
        }
        val second = mockk<CreationResponseErrorHandler> {
            coJustRun { handle(any<MessageInteractionResponseBehavior>(), any()) }
        }
        val chain = ChainCreationResponseErrorHandler(first, second)

        val response = mockk<MessageInteractionResponseBehavior>()
        val exception = Exception()

        chain.handle(response, exception)

        coVerify(exactly = 1) { first.handle(response, exception) }
        coVerify(exactly = 1) { second.handle(response, exception) }
    }
})
