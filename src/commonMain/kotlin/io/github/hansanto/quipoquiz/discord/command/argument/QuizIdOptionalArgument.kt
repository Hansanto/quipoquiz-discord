package io.github.hansanto.quipoquiz.discord.command.argument

import de.comahe.i18n4k.messages.MessageBundleLocalizedString
import dev.kord.core.behavior.interaction.suggestString
import dev.kord.core.entity.interaction.InteractionCommand
import dev.kord.core.event.interaction.AutoCompleteInteractionCreateEvent
import dev.kord.rest.builder.interaction.BaseInputChatBuilder
import dev.kord.rest.builder.interaction.string
import io.github.hansanto.generated.i18n.Messages
import io.github.hansanto.quipoquiz.Language
import io.github.hansanto.quipoquiz.config.CommandConfiguration
import io.github.hansanto.quipoquiz.discord.framework.command.CommandArgument
import io.github.hansanto.quipoquiz.quipoquiz.QuipoQuizQuizId
import io.github.hansanto.quipoquiz.quipoquiz.QuipoQuizService
import io.github.hansanto.quipoquiz.withLocale

class QuizIdOptionalArgument(
    private val service: QuipoQuizService,
    private val languageOptionalArgument: LanguageOptionalArgument,
    private val categoryIdOptionalArgument: CategoryIdOptionalArgument
) : CommandArgument<QuipoQuizQuizId?> {

    override val autocomplete: Boolean
        get() = true

    override val bundleName: MessageBundleLocalizedString
        get() = Messages.argument_quiz_name

    override val bundleDescription: MessageBundleLocalizedString
        get() = Messages.argument_quiz_description

    override fun register(builder: BaseInputChatBuilder) {
        builder.string(
            name = rootName(),
            description = rootDescription()
        ) {
            Language.withLocale { i18nLocale, kordLocale ->
                name(kordLocale, bundleName(i18nLocale))
                description(kordLocale, bundleDescription(i18nLocale))
            }

            required = false
            autocomplete = true
        }
    }

    override fun from(command: InteractionCommand): QuipoQuizQuizId? {
        return command.strings[rootName()]?.let { QuipoQuizQuizId(it) }
    }

    override suspend fun autoComplete(event: AutoCompleteInteractionCreateEvent): Boolean {
        val interaction = event.interaction
        val userValue = interaction.focusedOption.value

        val language = languageOptionalArgument.from(interaction.command)
        val categoriesLanguage = service.getQuizData()[language] ?: return false

        val categoryId = categoryIdOptionalArgument.from(interaction.command)
        val category = categoryId?.let { userCategoryId ->
            categoriesLanguage.firstOrNull { category ->
                category.id == userCategoryId
            }
        }

        val quizzesCategory = when (category) {
            null -> categoriesLanguage.asSequence().flatMap { it.quizzes }
            else -> category.quizzes.asSequence()
        }

        val matchingQuizzes = quizzesCategory.filter {
            it.id.value.contains(userValue, ignoreCase = true) || it.title.contains(userValue, ignoreCase = true)
        }.sortedByDescending {
            it.id.value.startsWith(userValue, ignoreCase = true) || it.title.startsWith(userValue, ignoreCase = true)
        }.take(CommandConfiguration.maximumSizeAutoComplete)

        interaction.suggestString {
            matchingQuizzes.forEach {
                choice("${it.id}. ${it.title}", it.id.value)
            }
        }

        return true
    }
}
