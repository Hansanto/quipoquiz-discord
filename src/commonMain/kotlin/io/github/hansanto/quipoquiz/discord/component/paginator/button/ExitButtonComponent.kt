package io.github.hansanto.quipoquiz.discord.component.paginator.button

import dev.kord.common.entity.ButtonStyle
import dev.kord.core.Kord
import io.github.hansanto.quipoquiz.config.EmojiConfiguration
import io.github.hansanto.quipoquiz.discord.component.question.QuestionComponent
import io.github.hansanto.quipoquiz.discord.framework.builder.component.CustomActionRowBuilder
import io.github.hansanto.quipoquiz.discord.framework.component.container.exception.ExitPerformException
import io.github.hansanto.quipoquiz.discord.framework.component.paginator.PageContext
import io.github.hansanto.quipoquiz.discord.framework.component.paginator.PaginatorComponent
import io.github.hansanto.quipoquiz.discord.framework.component.paginator.PreProcessButtonInteraction
import io.github.hansanto.quipoquiz.discord.framework.component.paginator.addButton
import io.github.hansanto.quipoquiz.discord.framework.component.row.ClickableButtonComponent
import io.github.hansanto.quipoquiz.util.createId
import kotlinx.coroutines.CoroutineScope

class ExitButtonComponent(
    override val id: String,
    disabled: Boolean,
    val style: ButtonStyle,
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
            style = style,
            customId = id
        ) {
            emoji = EmojiConfiguration.exit
        }
    }
}

/**
 * Add an exit button to the paginator.
 * @receiver Paginator where the button will be added.
 * @param kord Kord.
 * @param preProcess Pre-process action when the button is clicked.
 * @param row Number of the row, if null, the button will be added to the first place available.
 * @param addIf Condition to add the button.
 */
inline fun PaginatorComponent<QuestionComponent>.addExitButton(
    kord: Kord,
    preProcess: PreProcessButtonInteraction,
    row: Int? = null,
    crossinline addIf: PageContext<QuestionComponent>.() -> Boolean = { true }
) {
    addButton(row = row) {
        if (!addIf()) return@addButton null
        val isFinished = pages.all { it.revealed }

        ExitButtonComponent(
            kord = kord,
            id = createId(page.id, "exit"),
            style = if (isFinished) ButtonStyle.Success else ButtonStyle.Danger,
            disabled = !messageBuilder.isActive(),
            executionEventScope = kord,
            launchInEventScope = this
        ).apply {
            action {
                if (!messageBuilder.isActive()) return@action
                if (!preProcess.perform(interaction)) return@action

                messageBuilder.acknowledge(this)
                messageBuilder.cancel(ExitPerformException())
            }
        }
    }
}
