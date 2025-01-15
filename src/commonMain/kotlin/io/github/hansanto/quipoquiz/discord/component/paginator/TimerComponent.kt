package io.github.hansanto.quipoquiz.discord.component.paginator

import dev.kord.x.emoji.Emojis
import io.github.hansanto.generated.i18n.Messages
import io.github.hansanto.quipoquiz.Language
import io.github.hansanto.quipoquiz.discord.component.question.QuestionComponent
import io.github.hansanto.quipoquiz.discord.framework.builder.message.CustomEmbedBuilder
import io.github.hansanto.quipoquiz.discord.framework.component.EmbedComponent
import io.github.hansanto.quipoquiz.discord.framework.component.paginator.PageContext
import io.github.hansanto.quipoquiz.discord.framework.component.paginator.PaginatorComponent
import io.github.hansanto.quipoquiz.discord.framework.extension.toDiscordPartialEmoji
import io.github.hansanto.quipoquiz.discord.framework.extension.visualizer
import io.github.hansanto.quipoquiz.util.createId
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.time.Duration

class TimerComponent<P : EmbedComponent>(
    override val id: String,
    private val pageContext: PageContext<P>,
    private val language: Language,
    private val initialTime: Duration,
    private val intervalUpdate: Duration,
    private val onFinish: suspend PageContext<P>.() -> Unit
) : EmbedComponent {

    private var timeLeft: Duration = initialTime

    private var job: Job = pageContext.launch(start = CoroutineStart.LAZY) {
        while (isActive && timeLeft > Duration.ZERO) {
            val seconds = intervalUpdate.coerceAtMost(timeLeft)
            delay(seconds)
            timeLeft -= seconds
        }
        onFinish(pageContext)
    }

    override suspend fun render(builder: CustomEmbedBuilder) {
        builder.apply {
            description = Messages.game_time_left(
                Emojis.hourglassFlowingSand.toDiscordPartialEmoji().visualizer(),
                timeLeft.inWholeSeconds,
                language.i18nLocale
            )
        }

        job.start()
    }

    override suspend fun cancel() {
        job.cancel()
    }
}

fun PaginatorComponent<QuestionComponent>.addTimer(
    timer: Duration,
    intervalUpdate: Duration,
    language: Language,
    onFinished: suspend PageContext<QuestionComponent>.() -> Unit
) {
    onPageLoad {
        if (page.revealed) return@onPageLoad

        addEmbed(
            TimerComponent(
                id = createId(page.id, "timer"),
                pageContext = this,
                language = language,
                initialTime = timer,
                intervalUpdate = intervalUpdate,
                onFinish = onFinished
            )
        )
    }
}
