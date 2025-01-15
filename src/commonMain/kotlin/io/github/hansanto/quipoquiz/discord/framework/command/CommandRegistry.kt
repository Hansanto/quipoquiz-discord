package io.github.hansanto.quipoquiz.discord.framework.command

import dev.kord.common.entity.ApplicationCommandType
import dev.kord.common.entity.Snowflake
import dev.kord.core.Kord
import dev.kord.core.entity.application.ApplicationCommand
import dev.kord.core.event.interaction.ApplicationCommandInteractionCreateEvent
import dev.kord.core.event.interaction.AutoCompleteInteractionCreateEvent
import dev.kord.core.event.interaction.ChatInputCommandInteractionCreateEvent
import dev.kord.core.event.interaction.MessageCommandInteractionCreateEvent
import dev.kord.core.event.interaction.UserCommandInteractionCreateEvent
import dev.kord.rest.builder.interaction.MultiApplicationCommandBuilder
import dev.kord.rest.builder.interaction.input
import dev.kord.rest.builder.interaction.message
import dev.kord.rest.builder.interaction.user
import io.github.hansanto.quipoquiz.Language
import io.github.hansanto.quipoquiz.config.BotConfiguration
import io.github.hansanto.quipoquiz.discord.framework.extension.onEvent
import io.github.hansanto.quipoquiz.withLocale
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.flow.toList

private val logger = KotlinLogging.logger { }

interface CommandRegistry {

    /**
     * Update the commands in the bot.
     * If the bot is in the development guild, it will update the commands in the development guild.
     * Otherwise, it will update the global commands.
     * This method will create, delete or update the commands in the bot.
     */
    suspend fun registerCommands()

    /**
     * Add a command to the registry.
     * @param command The command to add.
     */
    fun addCommand(command: Command<*>)
}

class CommandRegistryImpl(private val kord: Kord) : CommandRegistry {

    private val commands: MutableSet<Command<*>> = mutableSetOf()

    /**
     * `true` if the commands are initialized, `false` otherwise.
     */
    private var isInitialized = false

    override suspend fun registerCommands() {
        require(!isInitialized) { "Commands are already initialized." }
        isInitialized = true

        val (chatInputCommands, userCommands, messageCommands) = splitCommands()

        logger.info { "Registering commands..." }
        val allRegisteredCommands = sendCommands(chatInputCommands, userCommands, messageCommands)
        logger.info {
            "Registered ${allRegisteredCommands.size} commands [${allRegisteredCommands.joinToString { it.name }}]"
        }

        val (chatInputRegisteredCommands, userRegisteredCommands, messageRegisteredCommands) = associateCommandWithId(
            allRegisteredCommands,
            chatInputCommands,
            userCommands,
            messageCommands
        )

        listenInteractionEvent<ChatInputCommandInteractionCreateEvent>(chatInputRegisteredCommands)
        listenInteractionEvent<UserCommandInteractionCreateEvent>(userRegisteredCommands)
        listenInteractionEvent<MessageCommandInteractionCreateEvent>(messageRegisteredCommands)

        listenAutoCompleteEvent(chatInputCommands)
    }

    /**
     * Split the commands into chat input, user and message commands.
     * @return A triple containing the chat input, user and message commands.
     */
    private fun splitCommands(): Triple<
        Collection<ChatInputCommand>,
        Collection<UserCommand>,
        Collection<MessageCommand>
        > {
        val chatInputCommands = mutableListOf<ChatInputCommand>()
        val userCommands = mutableListOf<UserCommand>()
        val messageCommands = mutableListOf<MessageCommand>()

        for (command in this.commands) {
            when (command) {
                is ChatInputCommand -> chatInputCommands.add(command)
                is UserCommand -> userCommands.add(command)
                is MessageCommand -> messageCommands.add(command)
                else -> error("Command type ${command::class.simpleName} not supported.")
            }
        }

        return Triple(chatInputCommands, userCommands, messageCommands)
    }

    /**
     * Associate the commands with their ids.
     * @param commands The registered commands.
     * @param chatInputCommands The chat input commands.
     * @param userCommands The user commands.
     * @param messageCommands The message commands.
     * @return A triple containing the chat input, user and message commands with their ids.
     */
    private fun associateCommandWithId(
        commands: Collection<ApplicationCommand>,
        chatInputCommands: Collection<ChatInputCommand>,
        userCommands: Collection<UserCommand>,
        messageCommands: Collection<MessageCommand>
    ): Triple<Map<Snowflake, ChatInputCommand>, Map<Snowflake, UserCommand>, Map<Snowflake, MessageCommand>> {
        val chatInputCommandWithName = chatInputCommands.associateBy { it.rootName() }
        val userCommandWithName = userCommands.associateBy { it.rootName() }
        val messageCommandWithName = messageCommands.associateBy { it.rootName() }

        val chatInputCommandWithId = mutableMapOf<Snowflake, ChatInputCommand>()
        val userCommandWithId = mutableMapOf<Snowflake, UserCommand>()
        val messageCommandWithId = mutableMapOf<Snowflake, MessageCommand>()

        for (command in commands) {
            when (command.type) {
                ApplicationCommandType.ChatInput -> chatInputCommandWithId[command.id] =
                    chatInputCommandWithName.getValue(command.name)

                ApplicationCommandType.User -> userCommandWithId[command.id] =
                    userCommandWithName.getValue(command.name)

                ApplicationCommandType.Message -> messageCommandWithId[command.id] =
                    messageCommandWithName.getValue(command.name)

                else -> logger.error { "Command type ${command.type} not supported." }
            }
        }
        return Triple(chatInputCommandWithId, userCommandWithId, messageCommandWithId)
    }

    /**
     * Send the request to register the commands.
     * @param chatInputCommands The chat input commands to register.
     * @param userCommands The user commands to register.
     * @param messageCommands The message commands to register.
     * @return The list of registered commands.
     */
    private suspend fun sendCommands(
        chatInputCommands: Collection<ChatInputCommand>,
        userCommands: Collection<UserCommand>,
        messageCommands: Collection<MessageCommand>
    ): List<ApplicationCommand> {
        val devGuildId = BotConfiguration.devGuildId
        return when (devGuildId) {
            null -> kord.createGlobalApplicationCommands {
                registerCommands(chatInputCommands, userCommands, messageCommands)
            }

            else -> kord.createGuildApplicationCommands(devGuildId) {
                registerCommands(chatInputCommands, userCommands, messageCommands)
            }
        }.toList()
    }

    /**
     * Register the commands in the builder.
     * @param chatInputCommands The chat input commands to register.
     * @param userCommands The user commands to register.
     * @param messageCommands The message commands to register.
     * @receiver The builder to register the commands.
     */
    fun MultiApplicationCommandBuilder.registerCommands(
        chatInputCommands: Collection<ChatInputCommand>,
        userCommands: Collection<UserCommand>,
        messageCommands: Collection<MessageCommand>
    ) {
        chatInputCommands.forEach {
            input(it.rootName(), it.rootDescription()) {
                Language.withLocale { i18nLocale, kordLocale ->
                    name(kordLocale, it.bundleName(i18nLocale))
                    description(kordLocale, it.bundleDescription(i18nLocale))
                }

                it.arguments.forEach { argument ->
                    argument.register(this)
                }
            }
        }

        userCommands.forEach {
            user(it.rootName()) {
                Language.withLocale { i18nLocale, kordLocale ->
                    name(kordLocale, it.bundleName(i18nLocale))
                }
            }
        }

        messageCommands.forEach {
            message(it.rootName()) {
                Language.withLocale { i18nLocale, kordLocale ->
                    name(kordLocale, it.bundleName(i18nLocale))
                }
            }
        }
    }

    /**
     * Listen to the [E] event for the commands.
     * @param commands The commands to handle the event.
     */
    private inline fun <reified E : ApplicationCommandInteractionCreateEvent> listenInteractionEvent(
        commands: Map<Snowflake, Command<E>>
    ) {
        if (commands.isEmpty()) return

        kord.onEvent<E> {
            val command = commands[interaction.invokedCommandId]
            if (command == null) {
                logger.warn { "Handler for command ${interaction.invokedCommandId} not found." }
                return@onEvent
            }

            command.execute(this)
        }
    }

    /**
     * Listen to the [AutoCompleteInteractionCreateEvent] for the arguments that should be autocompleted.
     * @param commands The commands containing the arguments to autocomplete.
     */
    private fun listenAutoCompleteEvent(commands: Collection<ChatInputCommand>) {
        val arguments = commands.flatMap { it.arguments }.distinct().filter { it.autocomplete }
        if (arguments.isEmpty()) return

        kord.onEvent<AutoCompleteInteractionCreateEvent> {
            val argName = interaction
                .command
                .options
                .asSequence()
                .singleOrNull { it.value.focused }
                ?.key ?: return@onEvent

            arguments.any applySuggestion@{ argument ->
                when {
                    argName == argument.rootName() -> argument.autoComplete(this)
                    else -> false
                }
            }
        }
    }

    override fun addCommand(command: Command<*>) {
        this.commands.add(command)
    }
}
