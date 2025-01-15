package io.github.hansanto.quipoquiz.discord.component.paginator.button

import dev.kord.common.entity.ButtonStyle
import dev.kord.core.Kord
import io.github.hansanto.quipoquiz.config.EmojiConfiguration
import io.github.hansanto.quipoquiz.discord.framework.builder.component.CustomActionRowBuilder
import io.github.hansanto.quipoquiz.discord.framework.component.EmbedComponent
import io.github.hansanto.quipoquiz.discord.framework.component.paginator.PageContext
import io.github.hansanto.quipoquiz.discord.framework.component.paginator.PaginatorComponent
import io.github.hansanto.quipoquiz.discord.framework.component.paginator.PreProcessButtonInteraction
import io.github.hansanto.quipoquiz.discord.framework.component.paginator.addButton
import io.github.hansanto.quipoquiz.discord.framework.component.row.ClickableButtonComponent
import io.github.hansanto.quipoquiz.util.createId
import kotlinx.coroutines.CoroutineScope

/**
 * Represents the previous button component of the paginator.
 */
class PreviousButtonComponent(
    override val id: String,
    disabled: Boolean,
    kord: Kord,
    executionEventScope: CoroutineScope,
    launchInEventScope: CoroutineScope
) : ClickableButtonComponent(
    disabled = disabled,
    kord = kord,
    executionEventScope = executionEventScope,
    launchInEventScope = launchInEventScope
) {

    override suspend fun renderActionRow(builder: CustomActionRowBuilder) {
        builder.interactionButton(
            style = ButtonStyle.Secondary,
            customId = id
        ) {
            emoji = EmojiConfiguration.previous
            disabled = this@PreviousButtonComponent.disabled
        }
    }
}

/**
 * Add a previous button to the paginator.
 * @receiver Paginator where the button will be added.
 * @param kord Kord.
 * @param preProcess Pre-process action when the button is clicked.
 * @param row Number of the row, if null, the button will be added to the first place available.
 * @param disabled Condition to disable the button.
 * @param addIf Condition to add the button.
 */
inline fun <P : EmbedComponent> PaginatorComponent<P>.addPreviousButton(
    kord: Kord,
    preProcess: PreProcessButtonInteraction,
    row: Int? = null,
    crossinline disabled: PageContext<P>.() -> Boolean = { false },
    crossinline addIf: PageContext<P>.() -> Boolean = { true }
) {
    addButton(row = row) {
        if (!addIf()) return@addButton null

        val paginatorId = this@addPreviousButton.id
        PreviousButtonComponent(
            kord = kord,
            id = createId(paginatorId, "previous"),
            disabled = isFirstPage || disabled(this),
            executionEventScope = messageBuilder,
            launchInEventScope = this
        ).apply {
            action {
                if (isFirstPage) return@action
                if (!preProcess.perform(interaction)) return@action
                this@addPreviousButton.setCurrentPage(pageNumber - 1)
                messageBuilder.render(this)
            }
        }
    }
}
