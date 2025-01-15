package io.github.hansanto.quipoquiz.discord.component.paginator

import io.github.hansanto.quipoquiz.discord.framework.builder.message.CustomEmbedBuilder
import io.github.hansanto.quipoquiz.discord.framework.builder.message.EmbedFooterAddPosition
import io.github.hansanto.quipoquiz.discord.framework.component.EmbedComponent
import io.github.hansanto.quipoquiz.discord.framework.component.paginator.PageContext

class PageContentComponent<P : EmbedComponent>(
    override val id: String,
    private val pageContext: PageContext<P>,
    private val numberOfPages: Int
) : EmbedComponent {

    override suspend fun render(builder: CustomEmbedBuilder) {
        builder.apply {
            pageContext.page.render(this)
            val pageNumberText = getPageNumberDisplay(pageContext.pageNumber)

            footer {
                addText(pageNumberText, addPosition = EmbedFooterAddPosition.LEFT)
            }
        }
    }

    /**
     * Get the text to display the current page number.
     * @param currentPage Current page number.
     * @return Page number display.
     */
    private fun getPageNumberDisplay(currentPage: Int) = "${currentPage + 1} / $numberOfPages"
}
