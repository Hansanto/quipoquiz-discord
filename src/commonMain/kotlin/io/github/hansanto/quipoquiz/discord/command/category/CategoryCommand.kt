package io.github.hansanto.quipoquiz.discord.command.category

import de.comahe.i18n4k.messages.MessageBundleLocalizedString
import dev.kord.core.Kord
import dev.kord.core.event.interaction.ChatInputCommandInteractionCreateEvent
import io.github.hansanto.generated.i18n.Messages
import io.github.hansanto.quipoquiz.Language
import io.github.hansanto.quipoquiz.config.CommandConfiguration
import io.github.hansanto.quipoquiz.discord.command.argument.LanguageOptionalArgument
import io.github.hansanto.quipoquiz.discord.component.category.CategoryInfoComponent
import io.github.hansanto.quipoquiz.discord.component.category.CategorySelectorComponent
import io.github.hansanto.quipoquiz.discord.component.paginator.button.addNextButton
import io.github.hansanto.quipoquiz.discord.component.paginator.button.addPreviousButton
import io.github.hansanto.quipoquiz.discord.framework.command.ChatInputCommand
import io.github.hansanto.quipoquiz.discord.framework.command.CommandArgument
import io.github.hansanto.quipoquiz.discord.framework.component.container.Container
import io.github.hansanto.quipoquiz.discord.framework.component.container.ContainerImpl
import io.github.hansanto.quipoquiz.discord.framework.component.container.builder.MultipleRowMessageBuilder.Companion.LAST_ROW_INDEX
import io.github.hansanto.quipoquiz.discord.framework.component.container.exception.handler.ContainerShutdownCreationResponseErrorHandler
import io.github.hansanto.quipoquiz.discord.framework.component.container.exception.handler.DisplayReasonCreationResponseErrorHandler
import io.github.hansanto.quipoquiz.discord.framework.component.container.exception.handler.chainWith
import io.github.hansanto.quipoquiz.discord.framework.component.paginator.OwnerUserCheckButtonInteraction
import io.github.hansanto.quipoquiz.discord.framework.component.paginator.PaginatorComponent
import io.github.hansanto.quipoquiz.discord.framework.extension.ephemeralError
import io.github.hansanto.quipoquiz.discord.framework.extension.respondError
import io.github.hansanto.quipoquiz.from
import io.github.hansanto.quipoquiz.quipoquiz.QuipoQuizCategory
import io.github.hansanto.quipoquiz.quipoquiz.QuipoQuizCategoryId
import io.github.hansanto.quipoquiz.quipoquiz.QuipoQuizService
import io.github.hansanto.quipoquiz.util.createId
import kotlin.math.ceil
import kotlin.time.Duration.Companion.minutes

class CategoryCommand(
    private val kord: Kord,
    private val service: QuipoQuizService,
    private val languageOptionalArgument: LanguageOptionalArgument
) : ChatInputCommand {

    override val bundleName: MessageBundleLocalizedString
        get() = Messages.command_category_name

    override val bundleDescription: MessageBundleLocalizedString
        get() = Messages.command_category_description

    override val arguments: Collection<CommandArgument<*>>
        get() = listOf(languageOptionalArgument)

    override suspend fun execute(event: ChatInputCommandInteractionCreateEvent) {
        val interaction = event.interaction
        val command = interaction.command

        val language = languageOptionalArgument.from(command) ?: Language.from(interaction.locale)

        val categories = service.getQuizData()[language]
        if (categories == null) {
            interaction
                .deferEphemeralResponse()
                .respondError(Messages.error_not_found_category_for_language(language.i18nLocale))
            return
        }

        val container = ContainerImpl(
            kord = kord,
            owners = setOf(interaction.user.id),
            authorizedUsers = emptyList()
        ).apply {
            errorHandler = DisplayReasonCreationResponseErrorHandler(language)
                .chainWith(
                    ContainerShutdownCreationResponseErrorHandler(
                        kord = kord,
                        container = this,
                        language = language
                    )
                )

            timeout(
                maxAliveTimeout = 30.minutes,
                maxIdleTimeout = 5.minutes
            )
            startAutoUpdate()
        }

        val selector = CategorySelectorComponent(
            id = createId(container.id, "category_selector"),
            service = service,
            language = language,
            kord = kord,
            executionEventScope = container,
            launchInEventScope = container
        ).apply { registerChangeEventAction(this, container) }

        container.addRowComponent(selector, LAST_ROW_INDEX)
        container.render(interaction.deferEphemeralResponse())
    }

    /**
     * Handle the selection of a category.
     * @param selector Category selector.
     * @param container Container to render the next components.
     */
    private fun registerChangeEventAction(selector: CategorySelectorComponent, container: Container) {
        selector.action {
            val categoryId = QuipoQuizCategoryId(interaction.values.first())
            if (categoryId == selector.selectedCategoryId) {
                return@action
            }

            val language = selector.language
            val categories = service.getQuizData()[language]
            if (categories == null) {
                interaction.ephemeralError(Messages.error_not_found_category_for_language(language.i18nLocale))
                return@action
            }

            val categoryQuiz = categories.find { it.id == categoryId }
            if (categoryQuiz == null) {
                interaction.ephemeralError(Messages.error_not_found_category(language.i18nLocale))
                return@action
            }

            selector.selectedCategoryId = categoryId

            val paginatorId = createId(container.id, "paginator")
            val paginator = PaginatorComponent(
                id = paginatorId,
                pages = createCategoryPages(paginatorId, categoryQuiz, language),
                messageBuilder = container
            ).apply {
                val checkIsOwner = OwnerUserCheckButtonInteraction(messageBuilder)
                addPreviousButton(
                    kord = kord,
                    preProcess = checkIsOwner
                )
                addNextButton(
                    kord = kord,
                    preProcess = checkIsOwner
                )
            }

            container.removeAndCancelComponents()
            container.addGroupComponent(paginator)
            container.addRowComponent(selector, LAST_ROW_INDEX)
            container.render(this)
        }
    }

    /**
     * Create the components for the category information pages.
     * @param parentId Parent identifier.
     * @param category Category with quizzes.
     * @param language Language to display the information.
     * @return List of components.
     */
    private fun createCategoryPages(
        parentId: String,
        category: QuipoQuizCategory,
        language: Language
    ): List<CategoryInfoComponent> {
        val pageSize = CommandConfiguration.CategoryCommand.pageSize
        val quizzes = category.quizzes
        val numberOfQuiz = quizzes.size
        val numberOfPages = ceil(numberOfQuiz.toDouble() / pageSize).toInt()

        return List(numberOfPages) { index ->
            val start = index * pageSize
            val end = start + pageSize

            CategoryInfoComponent(
                id = createId(parentId, "page_$index"),
                category = category,
                quizzesSubList = quizzes.subList(start, end.coerceAtMost(numberOfQuiz)),
                language = language
            )
        }
    }
}
