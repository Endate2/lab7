import serverUtils.Console
import kotlin.concurrent.thread

fun runServer(console: Console, host: String, port: Int) {
    console.apply {
        initialize()

        // Запуск сервера в отдельном потоке
        thread {
            start {
                startServer(host, port)
                startInteractiveMode()


            }
        }

        // Обработка команд консоли
        while (true) {
            when (readlnOrNull()?.trim()?.lowercase()) {
                "exit" -> {
                    save()
                    stop()
                }
                "save" -> save()
            }
        }
    }
}

fun main() {
    val serverConsole = Console()
    runServer(serverConsole, "localhost", 3333)
}