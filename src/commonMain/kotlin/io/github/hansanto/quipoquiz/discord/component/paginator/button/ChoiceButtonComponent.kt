package io.github.hansanto.quipoquiz.discord.component.paginator.button

import dev.kord.core.Kord
import dev.kord.core.entity.User
import io.github.hansanto.quipoquiz.discord.component.question.QuestionComponent
import io.github.hansanto.quipoquiz.discord.framework.builder.component.CustomActionRowBuilder
import io.github.hansanto.quipoquiz.discord.framework.component.paginator.PageContext
import io.github.hansanto.quipoquiz.discord.framework.component.paginator.PaginatorComponent
import io.github.hansanto.quipoquiz.discord.framework.component.paginator.PreProcessButtonInteraction
import io.github.hansanto.quipoquiz.discord.framework.component.row.ClickableButtonComponent
import io.github.hansanto.quipoquiz.quipoquiz.QuipoQuizAnswerChoice
import io.github.hansanto.quipoquiz.quipoquiz.QuipoQuizAnswerChoiceId
import io.github.hansanto.quipoquiz.util.createId
import kotlinx.coroutines.CoroutineScope

class ChoiceButtonComponent(
    kord: Kord,
    override val id: String,
    disabled: Boolean,
    val answerChoice: QuipoQuizAnswerChoice,
    private val numberVotes: suspend () -> Int?,
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
            style = answerChoice.style.button,
            customId = id
        ) {
            emoji = answerChoice.symbol
            disabled = this@ChoiceButtonComponent.disabled

            val numberVotes = numberVotes()
            if (numberVotes != null) {
                label = "$numberVotes"
            }
        }
    }
}

/**
 * Add choice buttons to the paginator.
 * The number of buttons will be computed based on the number of choices in the question.
 * @receiver Paginator where the buttons will be added.
 * @param kord Kord.
 * @param preProcess Pre-process action when a button is clicked.
 * @param row Number of the row, if null, the buttons will be added to the first place available.
 * @param addIf Condition to add the buttons.
 * @param getNumberOfVotes Function to get the number of votes for a choice.
 * @param onSelectedChoice Action to perform when a choice is selected.
 */
inline fun PaginatorComponent<QuestionComponent>.addChoiceButtons(
    kord: Kord,
    preProcess: PreProcessButtonInteraction,
    row: Int? = null,
    crossinline addIf: PageContext<QuestionComponent>.() -> Boolean = { true },
    crossinline getNumberOfVotes: suspend PageContext<QuestionComponent>.(choice: QuipoQuizAnswerChoiceId) -> Int?,
    crossinline onSelectedChoice: suspend PageContext<QuestionComponent>.(
        user: User,
        choice: QuipoQuizAnswerChoiceId
    ) -> Unit
) {
    onPageLoad {
        if (!addIf()) return@onPageLoad

        val page = page
        page.questionOverview.question.choices.forEach { answerChoice ->
            val choiceId = answerChoice.id
            addButton(
                row = row,
                component = ChoiceButtonComponent(
                    kord = kord,
                    id = createId(page.id, "choice_${choiceId.value}"),
                    disabled = page.revealed,
                    answerChoice = answerChoice,
                    numberVotes = { getNumberOfVotes(choiceId) },
                    executionEventScope = this,
                    launchInEventScope = this
                ).apply {
                    action {
                        if (page.revealed) return@action
                        if (!preProcess.perform(interaction)) return@action

                        onSelectedChoice(interaction.user, choiceId)
                        messageBuilder.acknowledge(this)
                    }
                }
            )
        }
    }
}
