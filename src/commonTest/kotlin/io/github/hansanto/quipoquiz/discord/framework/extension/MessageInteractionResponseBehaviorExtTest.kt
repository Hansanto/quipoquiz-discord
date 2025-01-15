package io.github.hansanto.quipoquiz.discord.framework.extension

import dev.kord.common.entity.Snowflake
import dev.kord.core.Kord
import dev.kord.core.behavior.interaction.response.EphemeralMessageInteractionResponseBehavior
import dev.kord.core.behavior.interaction.response.PublicMessageInteractionResponseBehavior
import dev.kord.core.supplier.EntitySupplier
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe

class MessageInteractionResponseBehaviorExtTest : ShouldSpec({

    should("MessageInteractionResponseBehavior isPublic should return true if it is public message") {
        val messageInteractionResponseBehavior = object : PublicMessageInteractionResponseBehavior {
            override val applicationId: Snowflake
                get() = error("Not yet implemented")
            override val token: String
                get() = error("Not yet implemented")
            override val kord: Kord
                get() = error("Not yet implemented")
            override val supplier: EntitySupplier
                get() = error("Not yet implemented")
        }
        messageInteractionResponseBehavior.isPublic shouldBe true
    }

    should("MessageInteractionResponseBehavior isPublic should return false if it is ephemeral message") {
        val messageInteractionResponseBehavior = object : EphemeralMessageInteractionResponseBehavior {
            override val applicationId: Snowflake
                get() = error("Not yet implemented")
            override val token: String
                get() = error("Not yet implemented")
            override val kord: Kord
                get() = error("Not yet implemented")
            override val supplier: EntitySupplier
                get() = error("Not yet implemented")
        }
        messageInteractionResponseBehavior.isPublic shouldBe false
    }
})
