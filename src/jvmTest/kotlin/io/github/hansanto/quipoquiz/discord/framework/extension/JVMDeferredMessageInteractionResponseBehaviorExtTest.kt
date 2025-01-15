package io.github.hansanto.quipoquiz.discord.framework.extension

import dev.kord.common.entity.Snowflake
import dev.kord.core.Kord
import dev.kord.core.behavior.interaction.response.DeferredEphemeralMessageInteractionResponseBehavior
import dev.kord.core.behavior.interaction.response.DeferredPublicMessageInteractionResponseBehavior
import dev.kord.core.behavior.interaction.response.EphemeralMessageInteractionResponseBehavior
import dev.kord.core.behavior.interaction.response.PublicMessageInteractionResponseBehavior
import dev.kord.core.supplier.EntitySupplier
import io.github.hansanto.quipoquiz.util.randomSnowflake
import io.github.hansanto.quipoquiz.util.randomString
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import io.mockk.mockk

class JVMDeferredMessageInteractionResponseBehaviorExtTest : ShouldSpec({

    should("toMessageInteractionResponseBehavior return public message if isPublic is true") {
        val deferredMessageInteractionResponseBehavior = object : DeferredPublicMessageInteractionResponseBehavior {
            override val applicationId: Snowflake = randomSnowflake()
            override val token: String = randomString()
            override val kord: Kord = mockk()
            override val supplier: EntitySupplier = mockk()
        }

        val result =
            deferredMessageInteractionResponseBehavior.toMessageInteractionResponseBehavior()

        (result is PublicMessageInteractionResponseBehavior) shouldBe true
        result.applicationId shouldBe deferredMessageInteractionResponseBehavior.applicationId
        result.kord shouldBe deferredMessageInteractionResponseBehavior.kord
        result.supplier shouldBe deferredMessageInteractionResponseBehavior.supplier
        result.token shouldBe deferredMessageInteractionResponseBehavior.token
    }

    should("toMessageInteractionResponseBehavior return ephemeral message if isPublic is true") {
        val deferredMessageInteractionResponseBehavior = object : DeferredEphemeralMessageInteractionResponseBehavior {
            override val applicationId: Snowflake = randomSnowflake()
            override val token: String = randomString()
            override val kord: Kord = mockk()
            override val supplier: EntitySupplier = mockk()
        }

        val result =
            deferredMessageInteractionResponseBehavior.toMessageInteractionResponseBehavior()

        (result is EphemeralMessageInteractionResponseBehavior) shouldBe true
        result.applicationId shouldBe deferredMessageInteractionResponseBehavior.applicationId
        result.kord shouldBe deferredMessageInteractionResponseBehavior.kord
        result.supplier shouldBe deferredMessageInteractionResponseBehavior.supplier
        result.token shouldBe deferredMessageInteractionResponseBehavior.token
    }
})
