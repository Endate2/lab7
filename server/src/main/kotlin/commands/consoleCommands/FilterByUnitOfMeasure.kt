package commands.consoleCommands

import commands.CommandReceiver
import exceptions.InvalidArgumentException
import serverUtils.Validator
import java.nio.channels.SocketChannel

class FilterByUnitOfMeasure() : Command() {

    private lateinit var commandReceiver: CommandReceiver
    constructor(commandReceiver: CommandReceiver) : this() {
        this.commandReceiver = commandReceiver
    }

    private val info = "Выводит элементы с совпадающим UnitOfMeasure"
    private val argsTypes = mapOf(
        "unitOfMeasure" to "UnitOfMeasure"
    )
    override fun getInfo(): String {
        return info
    }

    override fun getArgsTypes(): Map<String, String> {
        return argsTypes
    }

    override fun execute(args: Map<String, String>, channel: SocketChannel,username: String) {
        if (Validator.verifyArgs(2, args)) {
            commandReceiver.filterByUnitOfMeasure(args, channel)
        } else throw InvalidArgumentException("Были введены неверные аргументы. Используйте команду HELP для проверки")
    }
}
