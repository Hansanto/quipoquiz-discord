package io.github.hansanto.quipoquiz.discord.component.category

import dev.kord.core.Kord
import io.github.hansanto.generated.i18n.Messages
import io.github.hansanto.quipoquiz.Language
import io.github.hansanto.quipoquiz.discord.framework.builder.component.CustomActionRowBuilder
import io.github.hansanto.quipoquiz.discord.framework.builder.component.option
import io.github.hansanto.quipoquiz.discord.framework.component.row.SelectMenuComponent
import io.github.hansanto.quipoquiz.quipoquiz.QuipoQuizCategory
import io.github.hansanto.quipoquiz.quipoquiz.QuipoQuizCategoryId
import io.github.hansanto.quipoquiz.quipoquiz.QuipoQuizService
import kotlinx.coroutines.CoroutineScope

/**
 * Component to select a category to get information about it.
 */
class CategorySelectorComponent(
    override val id: String,
    /**
     * QuipoQuiz service.
     */
    val service: QuipoQuizService,
    /**
     * Language selected.
     */
    val language: Language,
    /**
     * Category selected to display its name in the placeholder.
     */
    var selectedCategoryId: QuipoQuizCategoryId? = null,
    kord: Kord,
    executionEventScope: CoroutineScope,
    launchInEventScope: CoroutineScope
) : SelectMenuComponent(
    kord = kord,
    executionEventScope = executionEventScope,
    launchInEventScope = launchInEventScope
) {

    override suspend fun renderActionRow(builder: CustomActionRowBuilder) {
        val categories = service.getQuizData()[language] ?: emptyList()
        builder.stringSelect(id) {
            placeholder = getPlaceholder(categories)
            for (category in categories) {
                option(category.name, category.id.value)
            }
        }
    }

    /**
     * Get the placeholder for the category selector.
     * @param categories List of categories.
     * @return Placeholder text.
     */
    private fun getPlaceholder(categories: List<QuipoQuizCategory>): String = selectedCategoryId?.let { selected ->
        categories.find { it.id == selected }?.name
    } ?: Messages.category_select(language.i18nLocale)
}
