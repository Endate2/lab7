import clientUtils.Console


fun main() {
    val console = Console()

    console.getConnection()

    console.initialize()
    console.startInteractiveMode()
}