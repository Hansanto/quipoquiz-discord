package io.github.hansanto.quipoquiz.discord.command.play.chat

import de.comahe.i18n4k.messages.MessageBundleLocalizedString
import dev.kord.core.event.interaction.ChatInputCommandInteractionCreateEvent
import io.github.hansanto.generated.i18n.Messages
import io.github.hansanto.quipoquiz.Language
import io.github.hansanto.quipoquiz.config.CommandConfiguration
import io.github.hansanto.quipoquiz.discord.command.argument.CategoryIdOptionalArgument
import io.github.hansanto.quipoquiz.discord.command.argument.HideVoteOptionalArgument
import io.github.hansanto.quipoquiz.discord.command.argument.LanguageOptionalArgument
import io.github.hansanto.quipoquiz.discord.command.argument.LimitQuizOptionalArgument
import io.github.hansanto.quipoquiz.discord.command.argument.NumberOfLifeOptionalArgument
import io.github.hansanto.quipoquiz.discord.command.argument.QuizIdOptionalArgument
import io.github.hansanto.quipoquiz.discord.command.argument.RivalOptionalArgument
import io.github.hansanto.quipoquiz.discord.command.argument.TimeVoteOptionalArgument
import io.github.hansanto.quipoquiz.discord.command.play.PlayUtils
import io.github.hansanto.quipoquiz.discord.framework.command.ChatInputCommand
import io.github.hansanto.quipoquiz.discord.framework.command.CommandArgument
import io.github.hansanto.quipoquiz.discord.framework.extension.ephemeralError
import io.github.hansanto.quipoquiz.from
import io.github.hansanto.quipoquiz.game.SimpleGame
import io.github.hansanto.quipoquiz.quipoquiz.QuipoQuizService

class PlayClassicCommand(
    private val service: QuipoQuizService,
    private val rivalOptionalArgument: RivalOptionalArgument,
    private val languageOptionalArgument: LanguageOptionalArgument,
    private val numberOfLifeOptionalArgument: NumberOfLifeOptionalArgument,
    private val timerVoteArgument: TimeVoteOptionalArgument,
    private val limitQuizOptionalArgument: LimitQuizOptionalArgument,
    private val hideVoteOptionalArgument: HideVoteOptionalArgument,
    private val categoryIdOptionalArgument: CategoryIdOptionalArgument,
    private val quizIdOptionalArgument: QuizIdOptionalArgument
) : ChatInputCommand {

    override val bundleName: MessageBundleLocalizedString
        get() = Messages.command_play_classic_name

    override val bundleDescription: MessageBundleLocalizedString
        get() = Messages.command_play_classic_description

    override val arguments: Collection<CommandArgument<*>>
        get() = listOf(
            languageOptionalArgument,
            numberOfLifeOptionalArgument,
            timerVoteArgument,
            limitQuizOptionalArgument,
            hideVoteOptionalArgument,
            categoryIdOptionalArgument,
            quizIdOptionalArgument,
            rivalOptionalArgument
        )

    override suspend fun execute(event: ChatInputCommandInteractionCreateEvent) {
        val interaction = event.interaction
        val command = interaction.command
        val language = languageOptionalArgument.from(command) ?: Language.from(interaction.locale)
        val numberOfLife = numberOfLifeOptionalArgument.from(command)
        val timerVote = timerVoteArgument.from(command)
        val limitQuiz = limitQuizOptionalArgument.from(command) ?: CommandConfiguration.LimitArgument.defaultClassic
        val hideVote = hideVoteOptionalArgument.from(command) ?: CommandConfiguration.HideVoteArgument.default
        val categoryId = categoryIdOptionalArgument.from(command)
        val quizId = quizIdOptionalArgument.from(command)
        val rival = rivalOptionalArgument.from(command)

        if (rival != null && !PlayUtils.rivalIsValid(interaction, rival, language)) return

        val categories = service.getQuizData()[language]
        if (categories == null) {
            interaction.ephemeralError(Messages.error_not_found_category_for_language(language.i18nLocale))
            return
        }

        val questionOverviews = PlayUtils.getSelectedQuestions(
            interaction = interaction,
            language = language,
            categories = categories,
            categoryId = categoryId,
            quizId = quizId
        ) ?: return

        val questions = questionOverviews
            .shuffled()
            .take(limitQuiz)
            .toList()

        SimpleGame.fromInteraction(
            interaction = interaction,
            language = language,
            hideVote = hideVote,
            questions = questions,
            numberOfLife = numberOfLife,
            timerVote = timerVote,
            owners = setOf(interaction.user.id),
            authorizedUsers = rival?.let { setOf(it.id) }
        ).start(interaction.deferPublicResponse())
    }
}
