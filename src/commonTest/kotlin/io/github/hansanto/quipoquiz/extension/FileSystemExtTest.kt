package io.github.hansanto.quipoquiz.extension

import io.github.hansanto.quipoquiz.util.createFile
import io.github.hansanto.quipoquiz.util.createTmpDir
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import kotlinx.io.IOException
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem

class FileSystemExtTest : ShouldSpec({

    lateinit var tmpDir: Path

    beforeTest {
        tmpDir = createTmpDir()
    }

    afterTest {
        tmpDir.delete()
    }

    should("isDirectory returns false for non-existing file") {
        val file = tmpDir.resolve("file")
        file.exists() shouldBe false
        file.isDirectory shouldBe false
    }

    should("isDirectory returns false for file") {
        val file = tmpDir.resolve("file").apply {
            createFile()
        }
        file.exists() shouldBe true
        file.isDirectory shouldBe false
        file.isRegularFile shouldBe true
    }

    should("isDirectory returns true for directory") {
        val dir = tmpDir.resolve("dir").apply {
            createDirectories()
        }
        dir.isDirectory shouldBe true
        dir.isRegularFile shouldBe false
    }

    should("isRegularFile returns false for non-existing file") {
        val file = tmpDir.resolve("file")
        file.exists() shouldBe false
        file.isRegularFile shouldBe false
        file.isDirectory shouldBe false
    }

    should("isRegularFile returns false for directory") {
        val dir = tmpDir.resolve("dir").apply {
            createDirectories()
        }
        dir.isRegularFile shouldBe false
        dir.isDirectory shouldBe true
    }

    should("isRegularFile returns true for file") {
        val file = tmpDir.resolve("file").apply {
            createFile()
        }
        file.isRegularFile shouldBe true
        file.isDirectory shouldBe false
    }

    should("exists returns false for non-existing file") {
        val file = tmpDir.resolve("file")
        file.exists() shouldBe false
    }

    should("exists returns true for existing file") {
        val file = tmpDir.resolve("file").apply {
            createFile()
        }
        file.exists() shouldBe true
    }

    should("exists returns true for existing directory") {
        val dir = tmpDir.resolve("dir").apply {
            createDirectories()
        }
        dir.exists() shouldBe true
    }

    should("resolve returns correct path") {
        val resolved = tmpDir.resolve("file", "subdir", "subfile")
        val file = tmpDir.resolve("file")
        resolved shouldBe file.resolve("subdir", "subfile")
    }

    should("createDirectories creates directory if path does not exist") {
        val dir = tmpDir.resolve("dir")
        dir.exists() shouldBe false
        dir.createDirectories()
        dir.exists() shouldBe true
        dir.isDirectory shouldBe true
        SystemFileSystem.list(tmpDir).isEmpty() shouldBe false
    }

    should("createDirectories throws exception if path is a file") {
        val file = tmpDir.resolve("file").apply {
            createFile()
        }
        shouldThrow<IOException> {
            file.createDirectories()
        }
        file.isRegularFile shouldBe true
    }

    should("createDirectories does nothing if directory already exists") {
        val dir = tmpDir.resolve("dir").apply {
            createDirectories()
        }
        dir.isDirectory shouldBe true
        SystemFileSystem.list(tmpDir).isEmpty() shouldBe false

        dir.createDirectories()
        dir.isDirectory shouldBe true
        SystemFileSystem.list(tmpDir).isEmpty() shouldBe false
    }

    should("delete does nothing when deleting non-existing file") {
        val file = tmpDir.resolve("file")
        file.delete()
        file.exists() shouldBe false
    }

    should("delete deletes file") {
        val file = tmpDir.resolve("file").apply {
            createFile()
        }
        file.delete()
        file.exists() shouldBe false
    }

    should("delete deletes empty directory") {
        val dir = tmpDir.resolve("dir").apply {
            createDirectories()
        }
        dir.delete()
        dir.exists() shouldBe false
    }

    should("delete deletes directory with files") {
        val dir = tmpDir.resolve("dir").apply {
            createDirectories()
        }
        val subFile = dir.resolve("file").apply {
            createFile()
        }
        val subDir = dir.resolve("dir").apply {
            createDirectories()
        }
        val subSubFile = subDir.resolve("file").apply {
            createFile()
        }

        dir.delete()

        dir.exists() shouldBe false
        subFile.exists() shouldBe false
        subDir.exists() shouldBe false
        subSubFile.exists() shouldBe false
    }

    should("deleteDirectoryFiles throws exception if directory does not exist") {
        val dir = tmpDir.resolve("dir")
        dir.exists() shouldBe false
        shouldThrow<IOException> {
            dir.deleteDirectoryFiles()
        }
    }

    should("deleteDirectoryFiles throws exception if file is not a directory") {
        val file = tmpDir.resolve("file").apply {
            createFile()
        }
        shouldThrow<IOException> {
            file.deleteDirectoryFiles()
        }
    }

    should("deleteDirectoryFiles deletes files in directory but not the directory itself") {
        val dir = tmpDir.resolve("dir").apply {
            createDirectories()
        }
        val subFile = dir.resolve("file").apply {
            createFile()
        }
        val subDir = dir.resolve("subdir").apply {
            createDirectories()
        }
        val subSubFile = subDir.resolve("file").apply {
            createFile()
        }

        dir.deleteDirectoryFiles()

        dir.exists() shouldBe true
        subFile.exists() shouldBe false
        subDir.exists() shouldBe false
        subSubFile.exists() shouldBe false
    }

    should("readText throws exception if file does not exist") {
        val file = tmpDir.resolve("file")
        file.exists() shouldBe false
        shouldThrow<IOException> {
            file.readText()
        }
    }

    should("readText throws exception if path is a directory") {
        val dir = tmpDir.resolve("dir").apply {
            createDirectories()
        }
        shouldThrow<IOException> {
            dir.readText()
        }
    }

    should("readText reads file content") {
        val content = "content"
        val file = tmpDir.resolve("file").apply {
            writeText(content)
        }
        file.readText() shouldBe content
    }

    should("readText reads empty file content") {
        val file = tmpDir.resolve("file").apply {
            createFile()
        }
        file.readText() shouldBe ""
    }

    should("readText reads file content with new lines") {
        val content = """
            content
            with
            new
            lines
        """.trimIndent()
        val file = tmpDir.resolve("file").apply {
            writeText(content)
        }
        file.readText() shouldBe content
    }

    should("writeText creates file if it does not exist") {
        val content = "content"
        val file = tmpDir.resolve("file")
        file.exists() shouldBe false

        file.writeText(content)
        file.readText() shouldBe content
    }

    should("writeText throws exception if path is a directory") {
        val dir = tmpDir.resolve("dir").apply {
            createDirectories()
        }
        shouldThrow<IOException> {
            dir.writeText("content")
        }
    }

    should("writeText overwrites file content") {
        val content = "content"
        val file = tmpDir.resolve("file").apply {
            writeText(content)
        }
        file.readText() shouldBe content

        val newContent = "new content"
        file.writeText(newContent)
        file.readText() shouldBe newContent
    }

    should("writeText append content to file") {
        val content = "content"
        val file = tmpDir.resolve("file").apply {
            writeText(content)
        }
        file.readText() shouldBe content

        val newContent = "new content"
        file.writeText(newContent, append = true)
        file.readText() shouldBe content + newContent
    }
})
