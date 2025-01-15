package io.github.hansanto.quipoquiz.discord.command.play.chat

import de.comahe.i18n4k.messages.MessageBundleLocalizedString
import dev.kord.core.event.interaction.ChatInputCommandInteractionCreateEvent
import io.github.hansanto.generated.i18n.Messages
import io.github.hansanto.quipoquiz.Language
import io.github.hansanto.quipoquiz.config.CommandConfiguration
import io.github.hansanto.quipoquiz.discord.command.argument.HideVoteOptionalArgument
import io.github.hansanto.quipoquiz.discord.command.argument.LanguageOptionalArgument
import io.github.hansanto.quipoquiz.discord.command.argument.NumberOfLifeOptionalArgument
import io.github.hansanto.quipoquiz.discord.framework.command.ChatInputCommand
import io.github.hansanto.quipoquiz.discord.framework.command.CommandArgument
import io.github.hansanto.quipoquiz.discord.framework.extension.ephemeralError
import io.github.hansanto.quipoquiz.from
import io.github.hansanto.quipoquiz.game.SimpleGame
import io.github.hansanto.quipoquiz.quipoquiz.QuipoQuizService
import io.github.hansanto.quipoquiz.quipoquiz.toQuestionOverviews

class PlayEndlessCommand(
    private val service: QuipoQuizService,
    private val languageOptionalArgument: LanguageOptionalArgument,
    private val numberOfLifeOptionalArgument: NumberOfLifeOptionalArgument,
    private val hideVoteOptionalArgument: HideVoteOptionalArgument
) : ChatInputCommand {

    override val bundleName: MessageBundleLocalizedString
        get() = Messages.command_play_endless_name

    override val bundleDescription: MessageBundleLocalizedString
        get() = Messages.command_play_endless_description

    override val arguments: Collection<CommandArgument<*>>
        get() = listOf(
            languageOptionalArgument,
            numberOfLifeOptionalArgument,
            hideVoteOptionalArgument
        )

    override suspend fun execute(event: ChatInputCommandInteractionCreateEvent) {
        val interaction = event.interaction
        val command = interaction.command
        val language = languageOptionalArgument.from(command) ?: Language.from(interaction.locale)
        val hideVote = hideVoteOptionalArgument.from(command) ?: CommandConfiguration.HideVoteArgument.default
        val numberOfLife = numberOfLifeOptionalArgument.from(command)

        val categories = service.getQuizData()[language]
        if (categories == null) {
            interaction.ephemeralError(Messages.error_not_found_category_for_language(language.i18nLocale))
            return
        }

        val questions = categories
            .toQuestionOverviews()
            .shuffled()
            .toList()

        SimpleGame.fromInteraction(
            interaction = interaction,
            language = language,
            hideVote = hideVote,
            questions = questions,
            numberOfLife = numberOfLife,
            timerVote = null,
            owners = setOf(interaction.user.id),
            authorizedUsers = null
        ).start(interaction.deferPublicResponse())
    }
}
