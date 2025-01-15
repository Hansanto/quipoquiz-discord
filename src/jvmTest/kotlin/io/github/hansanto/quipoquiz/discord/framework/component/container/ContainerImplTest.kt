package io.github.hansanto.quipoquiz.discord.framework.component.container

import dev.kord.common.entity.Snowflake
import dev.kord.core.Kord
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import io.mockk.mockk
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.currentCoroutineContext
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.seconds

class ContainerImplTest : ShouldSpec({

    should("isOwner return true if owner list is null") {
        val container = ContainerImpl(
            kord = mockk<Kord>(),
            owners = null,
            authorizedUsers = null,
            coroutineScope = CoroutineScope(currentCoroutineContext())
        )

        repeat(10) {
            container.isOwner(Snowflake(it.toLong())) shouldBe true
        }
    }

    should("isOwner return true if id is in owner list with single owner") {
        val userId = Snowflake(0)
        val container = ContainerImpl(
            kord = mockk<Kord>(),
            owners = listOf(userId),
            authorizedUsers = null,
            coroutineScope = CoroutineScope(currentCoroutineContext())
        )
        container.isOwner(userId) shouldBe true
    }

    should("isOwner return true if id is in owner list with multiple owners") {
        val userIds = List(10) { Snowflake(it.toLong()) }
        val container = ContainerImpl(
            kord = mockk<Kord>(),
            owners = userIds,
            authorizedUsers = null,
            coroutineScope = CoroutineScope(currentCoroutineContext())
        )
        userIds.forEach {
            container.isOwner(it) shouldBe true
        }
    }

    should("isOwner return false if id is not in owner list with no owner") {
        val container = ContainerImpl(
            kord = mockk<Kord>(),
            owners = emptyList(),
            authorizedUsers = null,
            coroutineScope = CoroutineScope(currentCoroutineContext())
        )
        container.isOwner(Snowflake(0)) shouldBe false
    }

    should("isOwner return false if id is not in owner list with single owner") {
        val userId = Snowflake(0)
        val container = ContainerImpl(
            kord = mockk<Kord>(),
            owners = listOf(Snowflake(1)),
            authorizedUsers = null,
            coroutineScope = CoroutineScope(currentCoroutineContext())
        )
        container.isOwner(userId) shouldBe false
    }

    should("isOwner return false if id is not in owner list with multiple owners") {
        val userIds = List(10) { Snowflake(it.toLong()) }
        val container = ContainerImpl(
            kord = mockk<Kord>(),
            owners = userIds,
            authorizedUsers = null,
            coroutineScope = CoroutineScope(currentCoroutineContext())
        )
        container.isOwner(Snowflake(100)) shouldBe false
    }

    should("isAuthorized return true if authorizedUsers list is null") {
        val container = ContainerImpl(
            kord = mockk<Kord>(),
            owners = null,
            authorizedUsers = null,
            coroutineScope = CoroutineScope(currentCoroutineContext())
        )

        repeat(10) {
            container.isAuthorized(Snowflake(it.toLong())) shouldBe true
        }
    }

    should("isAuthorized return true if id is in authorizedUsers list with single user") {
        val userId = Snowflake(0)
        val container = ContainerImpl(
            kord = mockk<Kord>(),
            owners = null,
            authorizedUsers = listOf(userId),
            coroutineScope = CoroutineScope(currentCoroutineContext())
        )
        container.isAuthorized(userId) shouldBe true
    }

    should("isAuthorized return true if id is in authorizedUsers list with multiple users") {
        val userIds = List(10) { Snowflake(it.toLong()) }
        val container = ContainerImpl(
            kord = mockk<Kord>(),
            owners = null,
            authorizedUsers = userIds,
            coroutineScope = CoroutineScope(currentCoroutineContext())
        )
        userIds.forEach {
            container.isAuthorized(it) shouldBe true
        }
    }

    should("isAuthorized return false if id is not in authorizedUsers list with no user") {
        val container = ContainerImpl(
            kord = mockk<Kord>(),
            owners = emptyList(),
            authorizedUsers = emptyList(),
            coroutineScope = CoroutineScope(currentCoroutineContext())
        )
        container.isAuthorized(Snowflake(0)) shouldBe false
    }

    should("isAuthorized return false if id is not in authorizedUsers list with single user") {
        val container = ContainerImpl(
            kord = mockk<Kord>(),
            owners = emptyList(),
            authorizedUsers = listOf(Snowflake(0)),
            coroutineScope = CoroutineScope(currentCoroutineContext())
        )
        container.isAuthorized(Snowflake(1)) shouldBe false
    }

    should("isAuthorized return false if id is not in authorizedUsers list with multiple users") {
        val userIds = List(10) { Snowflake(it.toLong()) }
        val container = ContainerImpl(
            kord = mockk<Kord>(),
            owners = emptyList(),
            authorizedUsers = userIds,
            coroutineScope = CoroutineScope(currentCoroutineContext())
        )
        container.isAuthorized(Snowflake(100)) shouldBe false
    }

    should("isAuthorized return true if owner list is null") {
        val container = ContainerImpl(
            kord = mockk<Kord>(),
            owners = null,
            authorizedUsers = emptyList(),
            coroutineScope = CoroutineScope(currentCoroutineContext())
        )

        repeat(10) {
            container.isAuthorized(Snowflake(it.toLong())) shouldBe true
        }
    }

    should("isAuthorized return true if id is in owner list with single owner") {
        val userId = Snowflake(0)
        val container = ContainerImpl(
            kord = mockk<Kord>(),
            owners = listOf(userId),
            authorizedUsers = emptyList(),
            coroutineScope = CoroutineScope(currentCoroutineContext())
        )
        container.isAuthorized(userId) shouldBe true
    }

    should("isAuthorized return true if id is in owner list with multiple owners") {
        val userIds = List(10) { Snowflake(it.toLong()) }
        val container = ContainerImpl(
            kord = mockk<Kord>(),
            owners = userIds,
            authorizedUsers = emptyList(),
            coroutineScope = CoroutineScope(currentCoroutineContext())
        )
        userIds.forEach {
            container.isAuthorized(it) shouldBe true
        }
    }

    should("isAuthorized return false if id is not in owner list with no owner and no authorized user") {
        val container = ContainerImpl(
            kord = mockk<Kord>(),
            owners = emptyList(),
            authorizedUsers = emptyList(),
            coroutineScope = CoroutineScope(currentCoroutineContext())
        )
        container.isAuthorized(Snowflake(0)) shouldBe false
    }

    should("isAuthorized return false if id is not in owner list with single owner") {
        val userId = Snowflake(0)
        val container = ContainerImpl(
            kord = mockk<Kord>(),
            owners = listOf(Snowflake(1)),
            authorizedUsers = emptyList(),
            coroutineScope = CoroutineScope(currentCoroutineContext())
        )
        container.isAuthorized(userId) shouldBe false
    }

    should("isAuthorized return false if id is not in owner list with multiple owners") {
        val userIds = List(10) { Snowflake(it.toLong()) }
        val container = ContainerImpl(
            kord = mockk<Kord>(),
            owners = userIds,
            authorizedUsers = emptyList(),
            coroutineScope = CoroutineScope(currentCoroutineContext())
        )
        container.isAuthorized(Snowflake(100)) shouldBe false
    }

    should("timeout throw exception if max alive timeout is less than or equal to zero") {
        val coroutineScope = CoroutineScope(SupervisorJob() + currentCoroutineContext())
        val container = ContainerImpl(
            kord = mockk<Kord>(),
            owners = null,
            authorizedUsers = null,
            coroutineScope = coroutineScope
        )
        container.isActive() shouldBe true

        repeat(10) {
            shouldThrow<IllegalArgumentException> {
                container.timeout(
                    maxAliveTimeout = -(it).seconds,
                    maxIdleTimeout = 1.hours
                )
            }
        }
    }

    should("timeout throw exception if max idle timeout is less than or equal to zero") {
        val coroutineScope = CoroutineScope(SupervisorJob() + currentCoroutineContext())
        val container = ContainerImpl(
            kord = mockk<Kord>(),
            owners = null,
            authorizedUsers = null,
            coroutineScope = coroutineScope
        )
        container.isActive() shouldBe true

        repeat(10) {
            shouldThrow<IllegalArgumentException> {
                container.timeout(
                    maxAliveTimeout = 1.hours,
                    maxIdleTimeout = -(it).seconds
                )
            }
        }
    }
})
