package io.github.hansanto.quipoquiz.discord.component.paginator.button

import dev.kord.common.entity.ButtonStyle
import io.github.hansanto.quipoquiz.config.EmojiConfiguration
import io.github.hansanto.quipoquiz.discord.component.question.QuestionComponent
import io.github.hansanto.quipoquiz.discord.framework.builder.component.CustomActionRowBuilder
import io.github.hansanto.quipoquiz.discord.framework.component.paginator.PageContext
import io.github.hansanto.quipoquiz.discord.framework.component.paginator.PaginatorComponent
import io.github.hansanto.quipoquiz.discord.framework.component.row.NonClickableButtonComponent
import io.github.hansanto.quipoquiz.util.createId

class VoterCounterButtonComponent(
    override val id: String,
    private val numberVotes: suspend () -> Int
) : NonClickableButtonComponent() {

    override suspend fun renderActionRow(builder: CustomActionRowBuilder) {
        builder.interactionButton(
            style = ButtonStyle.Secondary,
            customId = id
        ) {
            emoji = EmojiConfiguration.voteCounter
            disabled = true
            label = "${numberVotes()}"
        }
    }
}

/**
 * Add a voter counter button to the paginator.
 * The button is not clickable and will display the number of voters.
 * @receiver Paginator where the button will be added.
 * @param row Number of the row, if null, the button will be added to the first place available.
 * @param addIf Condition to add the button.
 * @param getNumberOfVoters Function to get the number of voters.
 */
inline fun PaginatorComponent<QuestionComponent>.addVoterCounterButton(
    row: Int? = null,
    crossinline addIf: PageContext<QuestionComponent>.() -> Boolean = { true },
    crossinline getNumberOfVoters: suspend PageContext<QuestionComponent>.() -> Int
) {
    onPageLoad {
        if (!addIf()) return@onPageLoad

        val page = page
        addButton(
            row = row,
            component = VoterCounterButtonComponent(
                id = createId(page.id, "voter_counter"),
                numberVotes = { getNumberOfVoters() }
            )
        )
    }
}
