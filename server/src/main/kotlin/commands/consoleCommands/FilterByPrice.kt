package commands.consoleCommands

import commands.CommandReceiver
import serverUtils.Validator
import exceptions.InvalidArgumentException
import java.nio.channels.SocketChannel


class FilterByPrice() : Command() {

    private lateinit var commandReceiver: CommandReceiver
    constructor(commandReceiver: CommandReceiver) : this() {
        this.commandReceiver = commandReceiver
    }

    private val info = "Печатает элементы с указанной ценой"
    private val argsTypes = mapOf(
        "price" to "Int"
    )

    override fun getInfo(): String {
        return info
    }

    override fun getArgsTypes(): Map<String, String> {
        return argsTypes
    }


    override fun execute(args: Map<String, String>,channel: SocketChannel,username: String) {
        if (Validator.verifyArgs(2, args)) {
            commandReceiver.filterByPrice(args, channel)
        } else throw InvalidArgumentException("Были введены неверные аргументы. Используйте команду HELP для проверки")
    }
}
