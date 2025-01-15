package io.github.hansanto.quipoquiz

import de.comahe.i18n4k.i18n4kInitCldrPluralRules
import dev.kord.core.Kord
import dev.kord.core.event.gateway.ReadyEvent
import dev.kord.gateway.Intent
import io.github.hansanto.quipoquiz.config.BotConfiguration
import io.github.hansanto.quipoquiz.config.QuipoQuizConfiguration
import io.github.hansanto.quipoquiz.discord.command.argument.CategoryIdOptionalArgument
import io.github.hansanto.quipoquiz.discord.command.argument.HideVoteOptionalArgument
import io.github.hansanto.quipoquiz.discord.command.argument.LanguageOptionalArgument
import io.github.hansanto.quipoquiz.discord.command.argument.LimitQuizOptionalArgument
import io.github.hansanto.quipoquiz.discord.command.argument.NumberOfLifeOptionalArgument
import io.github.hansanto.quipoquiz.discord.command.argument.QuizIdOptionalArgument
import io.github.hansanto.quipoquiz.discord.command.argument.RivalOptionalArgument
import io.github.hansanto.quipoquiz.discord.command.argument.TimeVoteOptionalArgument
import io.github.hansanto.quipoquiz.discord.command.category.CategoryCommand
import io.github.hansanto.quipoquiz.discord.command.ping.PingCommand
import io.github.hansanto.quipoquiz.discord.command.play.chat.PlayClassicCommand
import io.github.hansanto.quipoquiz.discord.command.play.chat.PlayEndlessCommand
import io.github.hansanto.quipoquiz.discord.command.play.user.PlayDuelUserCommand
import io.github.hansanto.quipoquiz.discord.framework.command.CommandRegistryImpl
import io.github.hansanto.quipoquiz.discord.framework.extension.onSingleEvent
import io.github.hansanto.quipoquiz.quipoquiz.QuipoQuizRepositoryImpl
import io.github.hansanto.quipoquiz.quipoquiz.QuipoQuizService
import io.github.hansanto.quipoquiz.quipoquiz.QuipoQuizServiceImpl
import io.github.oshai.kotlinlogging.KotlinLogging
import okio.use

private val logger = KotlinLogging.logger { }

suspend fun main() {
    // Initialize the plural rules for i18n4k
    i18n4kInitCldrPluralRules()

    val kord = Kord(BotConfiguration.token)
    kord.onSingleEvent<ReadyEvent> {
        logger.info { "Bot is connected" }
    }

    QuipoQuizConfiguration.createClient().use { client ->
        val repository = QuipoQuizRepositoryImpl(client)
        val service = QuipoQuizServiceImpl(repository)
        // Load and save in cache the quizzes
        service.getQuizData()

        registerCommands(kord, service)
        kord.login {
            intents {
                +Intent.Guilds
                +Intent.GuildMessages
            }
        }
    }
}

/**
 * Register the commands in the bot.
 * @param kord Kord instance to interact with Discord.
 * @param service Service to interact with the quizzes.
 */
private suspend fun registerCommands(kord: Kord, service: QuipoQuizService) {
    CommandRegistryImpl(kord).apply {
        val languageOptionalArgument = LanguageOptionalArgument()
        val timerVoteArgument = TimeVoteOptionalArgument()
        val limitQuizOptionalArgument = LimitQuizOptionalArgument()
        val numberOfLifeOptionalArgument = NumberOfLifeOptionalArgument()
        val hideVoteOptionalArgument = HideVoteOptionalArgument()
        val rivalOptionalArgument = RivalOptionalArgument()
        val categoryIdOptionalArgument = CategoryIdOptionalArgument(service, languageOptionalArgument)
        val quizIdOptionalArgument =
            QuizIdOptionalArgument(service, languageOptionalArgument, categoryIdOptionalArgument)

        addCommand(PingCommand(kord))
        addCommand(
            CategoryCommand(
                kord = kord,
                service = service,
                languageOptionalArgument = languageOptionalArgument
            )
        )
        addCommand(
            PlayEndlessCommand(
                service = service,
                languageOptionalArgument = languageOptionalArgument,
                numberOfLifeOptionalArgument = numberOfLifeOptionalArgument,
                hideVoteOptionalArgument = hideVoteOptionalArgument
            )
        )
        addCommand(
            PlayClassicCommand(
                service = service,
                languageOptionalArgument = languageOptionalArgument,
                numberOfLifeOptionalArgument = numberOfLifeOptionalArgument,
                timerVoteArgument = timerVoteArgument,
                limitQuizOptionalArgument = limitQuizOptionalArgument,
                hideVoteOptionalArgument = hideVoteOptionalArgument,
                categoryIdOptionalArgument = categoryIdOptionalArgument,
                quizIdOptionalArgument = quizIdOptionalArgument,
                rivalOptionalArgument = rivalOptionalArgument
            )
        )
        addCommand(PlayDuelUserCommand(service))
        registerCommands()
    }
}
