package io.github.hansanto.quipoquiz.discord.component.question

import io.github.hansanto.quipoquiz.discord.framework.component.EmbedComponent
import io.github.hansanto.quipoquiz.quipoquiz.QuipoQuizQuestionOverview

interface QuestionComponent : EmbedComponent {

    /**
     * The question overview.
     */
    val questionOverview: QuipoQuizQuestionOverview

    /**
     * `true` if the answer is revealed and should be displayed.
     */
    var revealed: Boolean
}
