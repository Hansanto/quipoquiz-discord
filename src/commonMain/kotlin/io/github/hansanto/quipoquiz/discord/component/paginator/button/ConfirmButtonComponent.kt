package io.github.hansanto.quipoquiz.discord.component.paginator.button

import dev.kord.common.entity.ButtonStyle
import dev.kord.core.Kord
import dev.kord.core.event.interaction.ButtonInteractionCreateEvent
import io.github.hansanto.quipoquiz.config.EmojiConfiguration
import io.github.hansanto.quipoquiz.discord.component.question.QuestionComponent
import io.github.hansanto.quipoquiz.discord.framework.builder.component.CustomActionRowBuilder
import io.github.hansanto.quipoquiz.discord.framework.component.paginator.PageContext
import io.github.hansanto.quipoquiz.discord.framework.component.paginator.PaginatorComponent
import io.github.hansanto.quipoquiz.discord.framework.component.paginator.PreProcessButtonInteraction
import io.github.hansanto.quipoquiz.discord.framework.component.paginator.addButton
import io.github.hansanto.quipoquiz.discord.framework.component.row.ClickableButtonComponent
import io.github.hansanto.quipoquiz.util.createId
import kotlinx.coroutines.CoroutineScope

class ConfirmButtonComponent(
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
            style = ButtonStyle.Primary,
            customId = id
        ) {
            emoji = EmojiConfiguration.validate
        }
    }
}

/**
 * Add a confirmation button to the paginator.
 * @receiver Paginator where the button will be added.
 * @param kord Kord.
 * @param preProcess Pre-process action when the button is clicked.
 * @param row Number of the row, if null, the button will be added to the first place available.
 * @param addIf Condition to add the button.
 * @param onClick Action to perform when the button is clicked.
 */
inline fun PaginatorComponent<QuestionComponent>.addConfirmButton(
    kord: Kord,
    preProcess: PreProcessButtonInteraction,
    row: Int? = null,
    crossinline addIf: suspend PageContext<QuestionComponent>.() -> Boolean = { true },
    crossinline onClick: suspend PageContext<QuestionComponent>.(ButtonInteractionCreateEvent) -> Unit
) {
    addButton(row = row) {
        if (!addIf()) return@addButton null

        val page = page

        ConfirmButtonComponent(
            id = createId(page.id, "confirm"),
            disabled = page.revealed,
            kord = kord,
            executionEventScope = messageBuilder,
            launchInEventScope = this
        ).apply {
            action {
                if (page.revealed) return@action
                if (!preProcess.perform(interaction)) return@action

                onClick(this)
            }
        }
    }
}
