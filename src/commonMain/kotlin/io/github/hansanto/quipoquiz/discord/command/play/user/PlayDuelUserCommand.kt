package io.github.hansanto.quipoquiz.discord.command.play.user

import de.comahe.i18n4k.messages.MessageBundleLocalizedString
import dev.kord.core.event.interaction.UserCommandInteractionCreateEvent
import io.github.hansanto.generated.i18n.Messages
import io.github.hansanto.quipoquiz.config.BotConfiguration
import io.github.hansanto.quipoquiz.config.CommandConfiguration
import io.github.hansanto.quipoquiz.discord.command.play.PlayUtils
import io.github.hansanto.quipoquiz.discord.framework.command.UserCommand
import io.github.hansanto.quipoquiz.discord.framework.extension.ephemeralError
import io.github.hansanto.quipoquiz.game.SimpleGame
import io.github.hansanto.quipoquiz.quipoquiz.QuipoQuizService
import io.github.hansanto.quipoquiz.quipoquiz.toQuestionOverviews

class PlayDuelUserCommand(
    private val service: QuipoQuizService
) : UserCommand {

    override val bundleName: MessageBundleLocalizedString
        get() = Messages.command_play_duel_name

    override suspend fun execute(event: UserCommandInteractionCreateEvent) {
        val interaction = event.interaction
        val language = BotConfiguration.defaultLanguage
        val targetUser = interaction.getTargetOrNull()

        if (!PlayUtils.rivalIsValid(interaction, targetUser, language)) return

        val categories = service.getQuizData()[language]
        if (categories == null) {
            interaction.ephemeralError(Messages.error_not_found_category_for_language(language.i18nLocale))
            return
        }

        val questions = categories
            .toQuestionOverviews()
            .shuffled()
            .take(CommandConfiguration.LimitArgument.defaultDuel)
            .toList()

        SimpleGame.fromInteraction(
            interaction = interaction,
            language = language,
            hideVote = true,
            questions = questions,
            timerVote = CommandConfiguration.TimeVoteArgument.defaultDuel,
            numberOfLife = null,
            owners = setOf(interaction.user.id),
            authorizedUsers = setOf(targetUser.id)
        ).start(interaction.deferPublicResponse())
    }
}
