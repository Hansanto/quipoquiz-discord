package io.github.hansanto.quipoquiz.util

import io.github.hansanto.quipoquiz.extension.exists
import io.github.hansanto.quipoquiz.extension.writeText
import kotlinx.io.files.Path

fun Path.createFile() {
    if (!exists()) {
        writeText("")
    }
}
