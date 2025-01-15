import kotlinx.coroutines.runBlocking

/**
 * Entry point for native platforms.
 * The main function cannot be suspendable, so we need to wrap it in a runBlocking block.
 */
fun main() = runBlocking {
    io.github.hansanto.quipoquiz.main()
}
