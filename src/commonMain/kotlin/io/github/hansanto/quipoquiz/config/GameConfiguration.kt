package io.github.hansanto.quipoquiz.config

import dev.kord.common.Color
import io.github.hansanto.quipoquiz.extension.hexColorOrNull
import io.github.hansanto.quipoquiz.util.environment.env
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

/**
 * Configuration about a game.
 */
object GameConfiguration {

    /**
     * The number of players per page in the score table at the end of the game.
     */
    val scorePlayerPerPage: Int by env(
        key = "BOT_GAME_SCORE_PLAYER_PER_PAGE",
        converter = { it.toInt() },
        defaultValue = { 10 }
    ) {
        require(it > 0) { "The value must be greater than 0" }
    }

    /**
     * The default color of the game if a question does not have a color.
     */
    val defaultColor: Color by env(
        key = "BOT_GAME_DEFAULT_COLOR",
        converter = {
            it.hexColorOrNull() ?: error("The value '$it' is not an hexadecimal color")
        },
        defaultValue = { Color(59, 114, 240) }
    )

    /**
     * Part of the timeout to shut down the game after a delay no matter what.
     */
    val timeoutAlivePerQuestion: Duration by env(
        key = "BOT_GAME_TIMEOUT_ALIVE_PER_QUESTION",
        converter = { it.toLong().seconds },
        defaultValue = { 3.minutes }
    ) {
        require(it > 0.seconds) { "The value must be greater than 0" }
    }

    /**
     * The timeout to shut down the game if no one is playing during the interval.
     */
    val timeoutIdle: Duration by env(
        key = "BOT_GAME_TIMEOUT_IDLE",
        converter = { it.toLong().seconds },
        defaultValue = { 1.hours }
    ) {
        require(it > 0.seconds) { "The value must be greater than 0" }
    }

    /**
     * The interval to update the timer left for the vote.
     */
    val timerVoteUpdateEvery: Duration by env(
        key = "BOT_GAME_TIMER_VOTE_UPDATE_EVERY",
        converter = { it.toLong().seconds },
        defaultValue = { 10.seconds }
    ) {
        require(it > 0.seconds) { "The value must be greater than 0" }
    }
}
