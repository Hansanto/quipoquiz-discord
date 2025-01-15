package io.github.hansanto.quipoquiz.discord.framework.component.container

import dev.kord.common.entity.Snowflake
import dev.kord.core.Kord
import dev.kord.core.behavior.interaction.response.DeferredMessageInteractionResponseBehavior
import dev.kord.core.behavior.interaction.response.MessageInteractionResponseBehavior
import dev.kord.core.behavior.interaction.response.edit
import dev.kord.core.behavior.interaction.response.respond
import dev.kord.core.entity.interaction.response.MessageInteractionResponse
import dev.kord.core.event.interaction.ButtonInteractionCreateEvent
import dev.kord.core.event.interaction.ComponentInteractionCreateEvent
import dev.kord.core.live.live
import dev.kord.rest.builder.message.modify.InteractionResponseModifyBuilder
import io.github.hansanto.quipoquiz.discord.framework.component.container.builder.InteractionCacheBuilder
import io.github.hansanto.quipoquiz.discord.framework.component.container.builder.InteractionDeltaBuilder
import io.github.hansanto.quipoquiz.discord.framework.component.container.builder.MessageBuilder
import io.github.hansanto.quipoquiz.discord.framework.component.container.builder.MessageBuilderImpl
import io.github.hansanto.quipoquiz.discord.framework.component.container.exception.MessageDeletedException
import io.github.hansanto.quipoquiz.discord.framework.component.container.exception.handler.CreationResponseErrorHandler
import io.github.hansanto.quipoquiz.discord.framework.component.container.update.Updatable
import io.github.hansanto.quipoquiz.discord.framework.extension.isPublic
import io.github.hansanto.quipoquiz.discord.framework.extension.toMessageInteractionResponseBehavior
import io.github.hansanto.quipoquiz.discord.framework.render.DefaultRenderer
import io.github.hansanto.quipoquiz.discord.framework.render.DeltaRenderer
import io.github.hansanto.quipoquiz.discord.framework.render.View
import io.github.hansanto.quipoquiz.discord.framework.util.Cancellable
import io.github.hansanto.quipoquiz.discord.framework.util.CancellationHandler
import io.github.hansanto.quipoquiz.discord.framework.util.TimeoutManager
import io.github.hansanto.quipoquiz.extension.createChildrenScope
import io.github.hansanto.quipoquiz.extension.waitUntilUnlocked
import io.github.hansanto.quipoquiz.util.Identifiable
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.job
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.datetime.Clock
import kotlin.time.Duration

private val logger = KotlinLogging.logger { }

/**
 * Container for interactive and non-interactive components to render in an interaction response.
 * The container manages the lifecycle of the components and can be cancelled after a timeout.
 */
interface Container : MessageBuilder, View, Updatable, Identifiable, Cancellable, CoroutineScope {

    /**
     * Unique identifier of the owners of the container.
     * If `null`, all users are considered as owners.
     */
    val owners: Collection<Snowflake>?

    /**
     * Users who can interact with the container (optional).
     * If `null`, all users can interact with the container.
     */
    val authorizedUsers: Collection<Snowflake>?

    /**
     * `true` if the interaction is public, `false` otherwise.
     */
    val isPublic: Boolean

    /**
     * Apply timeout configuration in order to cancel the container after a certain time.
     * @param maxAliveTimeout Duration to wait before cancelling the container.
     * @param maxIdleTimeout Duration to wait before cancelling the container after the last update.
     */
    suspend fun timeout(maxAliveTimeout: Duration, maxIdleTimeout: Duration)

    /**
     * Create a new container from the current container.
     * @return New container.
     */
    fun create(): Container

    /**
     * Check if the user is an owner of the container.
     * If the owners are not defined, all users are considered as owners.
     * @param userId User identifier.
     * @return `true` if the user is an owner, `false` otherwise.
     */
    fun isOwner(userId: Snowflake): Boolean {
        val owners = owners ?: return true
        return userId in owners
    }

    /**
     * Check if the user is authorized to interact with the container.
     * If the authorized users are not defined, all users are considered as authorized.
     * If the user is an owner, the user is considered as authorized.
     * @param userId User identifier.
     * @return `true` if the user is authorized, `false` otherwise.
     */
    fun isAuthorized(userId: Snowflake): Boolean {
        val authorizedUsers = authorizedUsers ?: return true
        return userId in authorizedUsers || isOwner(userId)
    }
}

class ContainerImpl(
    val kord: Kord,
    override val owners: Collection<Snowflake>?,
    override val authorizedUsers: Collection<Snowflake>?,
    private val coroutineScope: CoroutineScope = kord.createChildrenScope(),
    var errorHandler: CreationResponseErrorHandler? = null,
    private var lastInteraction: MessageInteractionResponseBehavior? = null,
    private val interactionCacheBuilder: InteractionCacheBuilder = InteractionCacheBuilder()
) : Container,
    MessageBuilder by MessageBuilderImpl(),
    CoroutineScope by coroutineScope {

    override val id: String = "container-${Snowflake(Clock.System.now())}"

    override var isPublic: Boolean = false

    private val renderer = DeltaRenderer(DefaultRenderer(this))

    private var updateJob: Job? = null

    private var timeout: TimeoutManager? = null

    private val cancellationHandlers = mutableListOf<CancellationHandler>()

    private val mutex = Mutex()

    override suspend fun startAutoUpdate(refreshInterval: Duration): Boolean {
        mutex.withLock {
            if (updateJob?.isActive == true) {
                return false
            }
            updateJob = createUpdateJob(refreshInterval)
            return true
        }
    }

    /**
     * Create a job to update the components at a given interval.
     * @param refreshInterval Interval to update the components.
     * @return Job to update the components.
     */
    private fun createUpdateJob(refreshInterval: Duration): Job = launch {
        while (isActive) {
            if (mutex.tryLock()) {
                try {
                    unsafeUpdateComponents()
                } finally {
                    mutex.unlock()
                }
            } else {
                mutex.waitUntilUnlocked()
            }
            delay(refreshInterval)
        }
    }

    override suspend fun stopAutoUpdate(): Boolean {
        return mutex.withLock {
            unsafeStopAutoUpdate()
        }
    }

    /**
     * Stop the auto-update job without locking the mutex.
     * @see stopAutoUpdate
     */
    private suspend fun unsafeStopAutoUpdate(): Boolean {
        val updateJob = this.updateJob
        return if (updateJob?.isActive == true) {
            updateJob.cancelAndJoin()
            this.updateJob = null
            true
        } else {
            false
        }
    }

    override suspend fun timeout(maxAliveTimeout: Duration, maxIdleTimeout: Duration) {
        require(maxAliveTimeout > Duration.ZERO) { "maxAliveTimeout must be greater than 0" }
        require(maxIdleTimeout > Duration.ZERO) { "maxIdleTimeout must be greater than 0" }

        mutex.withLock {
            require(timeout == null) { "Timeout configuration is already set" }

            ContainerTimeoutManager(
                kord = kord,
                container = this,
                maxAliveTimeout = maxAliveTimeout,
                maxIdleTimeout = maxIdleTimeout
            ).also {
                this.timeout = it
                it.startAliveTimeout()
            }
        }
    }

    override fun create(): Container {
        return ContainerImpl(
            kord = kord,
            owners = owners,
            authorizedUsers = authorizedUsers,
            errorHandler = errorHandler,
            lastInteraction = lastInteraction,
            interactionCacheBuilder = interactionCacheBuilder
        )
    }

    override suspend fun render(event: ComponentInteractionCreateEvent) {
        mutex.withLock {
            if (isPublic) {
                event.interaction.deferPublicMessageUpdate()
            } else {
                event.interaction.deferEphemeralMessageUpdate()
            }.also {
                this.unsafeRender(it)
                lastInteraction = it
            }
        }
    }

    override suspend fun render(event: MessageInteractionResponseBehavior) {
        mutex.withLock {
            unsafeRender(event)
        }
    }

    override suspend fun render(event: DeferredMessageInteractionResponseBehavior) {
        mutex.withLock {
            require(lastInteraction == null) { "The first interaction response is already set" }

            isPublic = event.isPublic
            renderDelta(
                render = { renderer.render(it) },
                applyDelta = {
                    val interactionResponse = event.respond { merge(it, this) }
                    cancelOnMessageDelete(interactionResponse)
                },
                doOnError = { errorHandler?.handle(event, it) },
                // Always update the interaction when the first response is created
                shouldUpdate = { true }
            )
            lastInteraction = event.toMessageInteractionResponseBehavior()
        }
    }

    private suspend fun unsafeRender(event: MessageInteractionResponseBehavior) {
        isPublic = event.isPublic
        renderDelta(
            render = { renderer.render(it) },
            applyDelta = { event.edit { merge(it, this) } },
            doOnError = { errorHandler?.handle(event, it) },
            shouldUpdate = { it.hasUpdate() }
        )
        lastInteraction = event
    }

    override suspend fun acknowledge(event: ButtonInteractionCreateEvent) {
        mutex.withLock {
            lastInteraction = if (isPublic) {
                event.interaction.deferPublicMessageUpdate()
            } else {
                event.interaction.deferEphemeralMessageUpdate()
            }
        }
    }

    override suspend fun update(): Boolean {
        return mutex.withLock {
            unsafeUpdate {
                renderer.render(it)
            }
        }
    }

    private suspend fun unsafeUpdateComponents(): Boolean {
        return unsafeUpdate {
            renderer.renderEmbeds(it)
            renderer.renderRowComponents(it)
        }
    }

    /**
     * Update by comparing the current components with the new components.
     * If at least one component is updated, the components modified are integrated into the [lastInteraction].
     * Otherwise, the interaction is not updated.
     *
     * @param render Function to render the components.
     * @return `true` if the components are updated, `false` otherwise.
     */
    private suspend inline fun unsafeUpdate(render: (InteractionDeltaBuilder) -> Unit): Boolean {
        val lastInteraction = lastInteraction
        if (lastInteraction == null) {
            return false
        }

        return renderDelta(
            render = render,
            applyDelta = {
                lastInteraction.edit {
                    merge(it, this)
                }
            },
            doOnError = { errorHandler?.handle(lastInteraction, it) },
            shouldUpdate = {
                it.hasUpdate()
            }
        )
    }

    /**
     * Render the components and apply the delta to the interaction.
     * If the delta is empty, the method [applyDelta] is not called.
     *
     * @param render Function to render the components.
     * @param applyDelta Function to apply the delta to the interaction.
     * @param doOnError Function to display an error message if it's not possible to render the components.
     * @param shouldUpdate Function to check if the interaction should be updated according to the delta.
     * @return `true` if the interaction is updated, `false` otherwise.
     */
    private inline fun renderDelta(
        render: (InteractionDeltaBuilder) -> Unit,
        applyDelta: (InteractionDeltaBuilder) -> Unit,
        doOnError: (Exception) -> Unit,
        shouldUpdate: (InteractionDeltaBuilder) -> Boolean
    ): Boolean {
        val interactionCacheBuilder = interactionCacheBuilder
        val tmpBuilder = InteractionDeltaBuilder(interactionCacheBuilder)

        try {
            render(tmpBuilder)
        } catch (exception: Exception) {
            logger.error(exception) { "An error occurred during the render in temporary builder" }
            doOnError(exception)
            return true
        }

        if (!shouldUpdate(tmpBuilder)) {
            return false
        }

        applyDelta(tmpBuilder)
        tmpBuilder.embeds?.let { interactionCacheBuilder.embeds = it }
        tmpBuilder.components?.let { interactionCacheBuilder.components = it }
        return true
    }

    /**
     * Merge the [builder] to the [kordBuilder].
     * Restart the timeout after merging.
     * @param builder Builder to merge.
     * @param kordBuilder Kord builder to merge.
     */
    private suspend fun merge(builder: InteractionDeltaBuilder, kordBuilder: InteractionResponseModifyBuilder) {
        timeout?.cancelIdleTimeout()
        builder.applyUpdate(kordBuilder)
        timeout?.startIdleTimeout()
    }

    override suspend fun cancel(cause: CancellationException?): Boolean {
        // Use mutex to avoid multiple cancellations at the same time
        // and consequently multiple calls to the cancellation handlers
        mutex.withLock {
            if (!isActive()) {
                return false
            }

            coroutineScope.cancel(cause)

            timeout?.let {
                it.cancelAliveTimeout()
                it.cancelIdleTimeout()
            }
            unsafeStopAutoUpdate()
        }

        cancellationHandlers.forEach { it(cause) }
        return true
    }

    override fun onCancel(block: CancellationHandler) {
        cancellationHandlers.add(block)
    }

    override fun isActive(): Boolean {
        return isActive
    }

    /**
     * Cancel the container when the message is deleted.
     * @param response Message response to cancel.
     */
    private suspend fun cancelOnMessageDelete(response: MessageInteractionResponse) {
        val liveMessage = response.message.live(this.createChildrenScope())

        // When the live message is cancelled, that means the message was deleted (or the container was cancelled)
        liveMessage.coroutineContext.job.invokeOnCompletion {
            // No need to cancel the container if it is already cancelled
            if (!this@ContainerImpl.isActive) return@invokeOnCompletion

            response.kord.launch {
                this@ContainerImpl.cancel(MessageDeletedException())
            }
        }
    }
}
