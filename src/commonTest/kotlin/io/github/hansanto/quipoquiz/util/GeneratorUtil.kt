package io.github.hansanto.quipoquiz.util

import dev.kord.common.Color
import dev.kord.common.entity.Snowflake
import io.github.hansanto.quipoquiz.extension.createDirectories
import io.github.hansanto.quipoquiz.extension.resolve
import kotlinx.datetime.Instant
import kotlinx.io.files.Path
import kotlinx.io.files.SystemTemporaryDirectory

fun createTmpDir(): Path {
    return SystemTemporaryDirectory.resolve("quipoquiz-test-${randomString(size = 10)}").also {
        it.createDirectories()
    }
}

fun randomString(allowedChar: List<Char> = ('a'..'z') + ('A'..'Z') + ('0'..'9'), size: Int = 50): String {
    return List(size) { allowedChar.random() }.joinToString("")
}

fun randomInstant(): Instant {
    return Instant.fromEpochSeconds(randomLong(min = 0, max = 1000000000L))
}

fun randomColor(): Color {
    return Color(randomInt(min = 0, max = 0xFFFFFF))
}

fun randomInt(min: Int = Int.MIN_VALUE, max: Int = Int.MAX_VALUE): Int {
    return (min..max).random()
}

fun randomLong(min: Long = Long.MIN_VALUE, max: Long = Long.MAX_VALUE): Long {
    return (min..max).random()
}

fun randomSnowflake(): Snowflake {
    return Snowflake(randomLong())
}

fun randomBoolean(): Boolean {
    return listOf(true, false).random()
}
