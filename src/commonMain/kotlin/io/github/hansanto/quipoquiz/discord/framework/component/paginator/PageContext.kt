package io.github.hansanto.quipoquiz.discord.framework.component.paginator

import io.github.hansanto.quipoquiz.discord.framework.component.EmbedComponent
import io.github.hansanto.quipoquiz.discord.framework.component.RowComponent
import kotlinx.coroutines.CoroutineScope

/**
 * Context of the current page.
 * @param P Type of the page.
 */
class PageContext<P>(
    /**
     * Page to be rendered.
     */
    val page: P,
    /**
     * Page number.
     */
    val pageNumber: Int,
    /**
     * `true` if the page is the first one, `false` otherwise.
     */
    val isFirstPage: Boolean,
    /**
     * `true` if the page is the last one, `false` otherwise.
     */
    val isLastPage: Boolean,
    /**
     * Lifecycle of the page, is cancelled when a new page is loaded or the paginator is destroyed.
     */
    lifecycle: CoroutineScope
) : CoroutineScope by lifecycle {

    /**
     * Buttons to render or has been rendered in the page.
     */
    val buttonComponents: Collection<RowComponentWithPlacement>
        get() = _buttonComponents

    private val _buttonComponents = mutableListOf<RowComponentWithPlacement>()

    /**
     * Embeds to render or has been rendered in the page.
     */
    val embedComponents: Collection<EmbedComponent>
        get() = _embedComponents

    private val _embedComponents = mutableListOf<EmbedComponent>()

    /**
     * Add a button to the paginator.
     * @param button Button to be added.
     */
    fun addButton(button: RowComponentWithPlacement) {
        _buttonComponents += button
    }

    /**
     * Add a button to the paginator.
     * @param component Button to be added.
     * @param row Row to place the button.
     * @see addButton
     */
    fun addButton(component: RowComponent, row: Int? = null) {
        addButton(RowComponentWithPlacement(component, row))
    }

    /**
     * Add an embed to the paginator.
     * @param embed Embed to be added.
     */
    fun addEmbed(embed: EmbedComponent) {
        _embedComponents += embed
    }
}
