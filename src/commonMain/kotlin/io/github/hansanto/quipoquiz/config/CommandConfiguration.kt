package io.github.hansanto.quipoquiz.config

import io.github.hansanto.quipoquiz.util.environment.env
import io.github.hansanto.quipoquiz.util.environment.requireGreaterThan
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

/**
 * Configuration about the commands.
 */
object CommandConfiguration {

    /**
     * Maximum size for the auto complete.
     */
    val maximumSizeAutoComplete: Int by env(
        key = "COMMAND_AUTO_COMPLETE_MAX_SIZE",
        converter = { it.toInt() },
        defaultValue = { 25 }
    ) {
        it.requireGreaterThan(0)
    }

    object LifeArgument {

        /**
         * The minimum life that a user can define for a player.
         */
        val minimum: Int by env(
            key = "COMMAND_LIFE_MIN",
            converter = { it.toInt() },
            defaultValue = { 1 }
        ) {
            it.requireGreaterThan(0)
        }

        /**
         * The maximum life that a user can define for a player.
         */
        val maximum: Int by env(
            key = "COMMAND_LIFE_MAX",
            converter = { it.toInt() },
            defaultValue = { Int.MAX_VALUE }
        ) {
            it.requireGreaterThan(0)
            it.requireGreaterThan(minimum)
        }
    }

    object TimeVoteArgument {

        /**
         * The minimum limit that a user can define for a command to define the maximum time to vote.
         * The value is in seconds.
         */
        val minimum: Int by env(
            key = "COMMAND_TIME_VOTE_MIN",
            converter = { it.toInt() },
            defaultValue = { 5 }
        ) {
            it.requireGreaterThan(0)
        }

        /**
         * The maximum limit that a user can define for a command to define the maximum time to vote.
         * The value is in seconds.
         */
        val maximum: Int by env(
            key = "COMMAND_TIME_VOTE_MAX",
            converter = { it.toInt() },
            defaultValue = { 2.minutes.inWholeSeconds.toInt() }
        ) {
            it.requireGreaterThan(0)
            it.requireGreaterThan(minimum)
        }

        /**
         * The default time to vote for a command.
         * The value is in seconds.
         */
        val defaultDuel: Duration by env(
            key = "COMMAND_TIME_VOTE_DUEL_DEFAULT",
            converter = { it.toInt().seconds },
            defaultValue = { 1.minutes }
        ) {
            it.requireGreaterThan(0.seconds)
        }
    }

    object LimitArgument {

        /**
         * The minimum limit that a user can define for a command that produces a list.
         */
        val minimum: Int by env(
            key = "COMMAND_LIMIT_QUIZ_MIN",
            converter = { it.toInt() },
            defaultValue = { 1 }
        ) {
            it.requireGreaterThan(0)
        }

        /**
         * The maximum limit that a user can define for a command that produces a list.
         */
        val maximum: Int by env(
            key = "COMMAND_LIMIT_QUIZ_MAX",
            converter = { it.toInt() },
            defaultValue = { 100 }
        ) {
            it.requireGreaterThan(0)
            it.requireGreaterThan(minimum)
        }

        /**
         * The default limit for the classic mode to play a quiz.
         */
        val defaultClassic: Int by env(
            key = "COMMAND_LIMIT_QUIZ_DEFAULT_NORMAL_MODE",
            converter = { it.toInt() },
            defaultValue = { 10 }
        ) {
            it.requireGreaterThan(0)
        }

        /**
         * The default limit for the duel mode to play a quiz.
         */
        val defaultDuel: Int by env(
            key = "COMMAND_LIMIT_QUIZ_DEFAULT_DUEL_MODE",
            converter = { it.toInt() },
            defaultValue = { 10 }
        ) {
            it.requireGreaterThan(0)
        }
    }

    object HideVoteArgument {

        val default: Boolean by env(
            key = "COMMAND_HIDE_VOTE_DEFAULT",
            converter = { it.toBoolean() },
            defaultValue = { false }
        )
    }

    object CategoryCommand {

        /**
         * Size of the page for the category command.
         * @see io.github.hansanto.quipoquiz.discord.command.category.CategoryCommand
         */
        val pageSize: Int by env(
            key = "COMMAND_CATEGORY_PAGE_SIZE",
            converter = { it.toInt() },
            defaultValue = { 10 }
        ) {
            it.requireGreaterThan(0)
        }
    }
}
