package io.github.hansanto.quipoquiz.discord.framework.component.paginator

import io.github.hansanto.quipoquiz.discord.component.paginator.PageContentComponent
import io.github.hansanto.quipoquiz.discord.framework.component.EmbedComponent
import io.github.hansanto.quipoquiz.discord.framework.component.GroupComponent
import io.github.hansanto.quipoquiz.discord.framework.component.RowComponent
import io.github.hansanto.quipoquiz.discord.framework.component.container.Container
import io.github.hansanto.quipoquiz.extension.createChildrenScope
import io.github.hansanto.quipoquiz.util.createId
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.cancel
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.collections.plusAssign

/**
 * Minimal location of an interactive component in the message.
 * @property component The interactive component.
 * @property row Row number where the component is rendered.
 */
data class RowComponentWithPlacement(
    val component: RowComponent,
    val row: Int?
)

/**
 * Actions to be executed when a page is loaded.
 */
private typealias PageLoadHandler<P> = suspend PageContext<P>.() -> Unit

/**
 * Add a button to the paginator when a new page is loaded.
 * @receiver Paginator where the button will be added.
 * @param row Number of the row where the button will be rendered.
 * @param componentBuilder Builder of the button.
 */
inline fun <P : EmbedComponent> PaginatorComponent<P>.addButton(
    row: Int? = null,
    crossinline componentBuilder: suspend PageContext<P>.() -> RowComponent?
) {
    onPageLoad {
        val component = componentBuilder() ?: return@onPageLoad
        addButton(component, row)
    }
}

/**
 * Add an embed component to the paginator when a new page is loaded.
 * If the component is null, it will not be added.
 * @receiver Paginator where the embed will be added.
 * @param componentBuilder Builder of the embed.
 * @return The embed component.
 */
inline fun <P : EmbedComponent> PaginatorComponent<P>.addEmbed(
    crossinline componentBuilder: suspend PageContext<P>.() -> EmbedComponent?
) {
    onPageLoad {
        val component = componentBuilder() ?: return@onPageLoad
        addEmbed(component)
    }
}

private val logger = KotlinLogging.logger {}

class PaginatorComponent<P : EmbedComponent>(
    override val id: String,
    /**
     * Message builder.
     */
    override val messageBuilder: Container,
    /**
     * Pages to be rendered.
     */
    val pages: List<P>,
    /**
     * Current page number.
     */
    private var currentPage: Int = 0
) : GroupComponent {

    /**
     * Context of the current page loaded and rendered.
     */
    private var pageContext: PageContext<P>? = null

    /**
     * Handlers to be called when a page is loaded.
     */
    private val onPageLoad = mutableListOf<PageLoadHandler<P>>()

    /**
     * Mutex to avoid concurrent access to the paginator.
     */
    private val mutex = Mutex()

    init {
        require(pages.isNotEmpty()) { "Pages must not be empty" }

        onPageLoad {
            addEmbed(
                PageContentComponent(
                    id = createId(page.id, "content"),
                    pageContext = this,
                    numberOfPages = pages.size
                )
            )
        }
    }

    override suspend fun registerComponents() {
        mutex.withLock {
            val currentPageIndex = this.currentPage

            pageContext?.let { previousPageContext ->
                previousPageContext.cancel()
                removeButtons(previousPageContext)
                removeEmbeds(previousPageContext)
            }

            val page = getPageFromIndexOrLimit(currentPageIndex)
            createPageContext(page, currentPageIndex).also { context ->
                pageContext = context
                onPageLoad.forEach { it(context) }

                addButtons(context)
                addEmbed(context)
            }
        }
    }

    /**
     * Retrieve the page from the index.
     * If the index is under 0, the first page is returned.
     * If the index is over the last page, the last page is returned.
     * @param index Current page number.
     * @return The current page or the first/last page if the index is out of bounds.
     */
    private fun getPageFromIndexOrLimit(index: Int): P {
        val indexPage = pages.getOrNull(index)
        if (indexPage != null) {
            return indexPage
        }

        // This step must be never reached and is here to avoid the stop of a game and log the case.
        logger.warn { "The page [$index] is out of bounds. Must be between 0 and ${pages.lastIndex}" }
        return when {
            index < 0 -> pages.first()
            else -> pages.last()
        }
    }

    override suspend fun cancel() {
        this.pageContext?.let {
            it.cancel()
            it.embedComponents.forEach { it.cancel() }
            it.buttonComponents.forEach { it.component.cancel() }
        }
    }

    /**
     * Create the context of the current page.
     * That contains page information and its lifecycle.
     * @param page Page.
     * @param currentPage Current page number.
     * @return Page context.
     */
    private fun createPageContext(page: P, currentPage: Int): PageContext<P> = PageContext(
        page = page,
        pageNumber = currentPage,
        isFirstPage = currentPage == 0,
        isLastPage = currentPage == pages.lastIndex,
        lifecycle = messageBuilder.createChildrenScope()
    )

    /**
     * Add the embed to the message.
     * @param pageContext Context of the new page.
     */
    private suspend fun addEmbed(newPageContext: PageContext<P>) {
        newPageContext.embedComponents.onEach {
            messageBuilder.addEmbedComponent(it)
        }
    }

    /**
     * Remove the last embeds from the message.
     * @param oldPageContext Context of the previous page.
     */
    private suspend fun removeEmbeds(oldPageContext: PageContext<P>) {
        val components = oldPageContext.embedComponents
        if (components.isEmpty()) {
            return
        }

        messageBuilder.removeAndCancelEmbedComponents(components.map { it.id })
    }

    /**
     * Add the buttons to the message.
     * @param newPageContext Context of the new page.
     */
    private suspend fun addButtons(newPageContext: PageContext<P>) {
        newPageContext.buttonComponents.forEach { (component, row) ->
            when (row) {
                null -> messageBuilder.addRowComponent(component)
                else -> messageBuilder.addRowComponent(component, row)
            }
        }
    }

    /**
     * Remove the last buttons from the message.
     * @param oldPageContext Context of the previous page.
     */
    private suspend fun removeButtons(oldPageContext: PageContext<P>) {
        val components = oldPageContext.buttonComponents
        if (components.isEmpty()) {
            return
        }

        messageBuilder.removeAndCancelRowComponents(components.map { it.component.id })
    }

    /**
     * Add a handler to be called when a page is loaded.
     * @param handler Handler to be called.
     */
    fun onPageLoad(handler: PageLoadHandler<P>) {
        onPageLoad += handler
    }

    /**
     * Set the current page.
     * This will not update the message.
     * @param page Page number.
     */
    suspend fun setCurrentPage(page: Int) {
        require(page in pages.indices) { "The page number is out of bounds." }
        mutex.withLock {
            currentPage = page
        }
    }
}
