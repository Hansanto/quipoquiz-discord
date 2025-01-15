package io.github.hansanto.quipoquiz.discord.framework.extension

import dev.kord.common.entity.Snowflake
import dev.kord.core.Kord
import dev.kord.core.behavior.interaction.response.DeferredEphemeralMessageInteractionResponseBehavior
import dev.kord.core.behavior.interaction.response.DeferredPublicMessageInteractionResponseBehavior
import dev.kord.core.supplier.EntitySupplier
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe

class DeferredMessageInteractionResponseBehaviorExtTest : ShouldSpec({

    should("DeferredMessageInteractionResponseBehavior isPublic should return true if it is public message") {
        val deferredMessageInteractionResponseBehavior = object : DeferredPublicMessageInteractionResponseBehavior {
            override val applicationId: Snowflake
                get() = error("Not yet implemented")
            override val token: String
                get() = error("Not yet implemented")
            override val kord: Kord
                get() = error("Not yet implemented")
            override val supplier: EntitySupplier
                get() = error("Not yet implemented")
        }
        deferredMessageInteractionResponseBehavior.isPublic shouldBe true
    }

    should("DeferredMessageInteractionResponseBehavior isPublic should return false if it is ephemeral message") {
        val deferredMessageInteractionResponseBehavior = object : DeferredEphemeralMessageInteractionResponseBehavior {
            override val applicationId: Snowflake
                get() = error("Not yet implemented")
            override val token: String
                get() = error("Not yet implemented")
            override val kord: Kord
                get() = error("Not yet implemented")
            override val supplier: EntitySupplier
                get() = error("Not yet implemented")
        }
        deferredMessageInteractionResponseBehavior.isPublic shouldBe false
    }
})
