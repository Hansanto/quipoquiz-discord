package io.github.hansanto.quipoquiz.extension

import dev.kord.common.Color

/**
 * The default symbol to append to the truncated string.
 */
const val DEFAULT_TRUNCATE_SYMBOL = ".."

/**
 * The radix for hexadecimal numbers.
 */
private const val RADIX_HEXADECIMAL = 16

/**
 * Regex to match with all tags represented by a text between '<' and '>'.
 */
private val REGEX_HTML_TAG = Regex("<[^>]+>")

/**
 * Regex to match a hex color.
 * The # character is optional at the beginning of the string.
 * Supports the format #RRGGBB or #RGB.
 */
private val HEX_COLOR_REGEX = Regex("^#?(?<hex>[A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})$")

/**
 * Remove everything between < and > and the brackets themselves.
 * @receiver String to remove tag.
 * @return String without tag.
 */
fun String.removeHtmlTag(): String {
    return this.replace(REGEX_HTML_TAG, "")
}

/**
 * Convert a string to a color.
 * The # character is optional at the beginning of the string.
 * @receiver String to convert.
 * @return Color or null if the string is not a valid hex color.
 */
fun String.hexColorOrNull(): Color? {
    val (hex) = HEX_COLOR_REGEX.matchEntire(this)?.destructured ?: return null

    val hexColor = when (hex.length) {
        3 -> buildString { // 3 char -> 6 char
            hex.forEach {
                append(it)
                append(it)
            }
        }

        6 -> hex
        else -> return null
    }

    val r = hexColor.substring(0, 2).toIntOrNull(RADIX_HEXADECIMAL) ?: return null
    val g = hexColor.substring(2, 4).toIntOrNull(RADIX_HEXADECIMAL) ?: return null
    val b = hexColor.substring(4, 6).toIntOrNull(RADIX_HEXADECIMAL) ?: return null
    return Color(r, g, b)
}

/**
 * Truncates the string to the given [maxLength] and appends "..." if the string is longer than the [maxLength].
 * @param maxLength The maximum length of the string.
 * @param truncateSymbol The symbol to append to the truncated string.
 * @return The truncated string if it is longer than the [maxLength], otherwise the original string.
 */
fun String.truncate(maxLength: Int, truncateSymbol: String = DEFAULT_TRUNCATE_SYMBOL): String {
    if (maxLength == 0) return ""
    if (maxLength <= truncateSymbol.length) return truncateSymbol.take(maxLength)

    return if (this.length > maxLength) {
        this.take(maxLength - truncateSymbol.length) + truncateSymbol
    } else {
        this
    }
}
