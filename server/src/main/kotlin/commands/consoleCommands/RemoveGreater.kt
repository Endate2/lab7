package commands.consoleCommands

import commands.CommandReceiver
import serverUtils.Validator
import exceptions.InvalidArgumentException
import java.nio.channels.SocketChannel


class RemoveGreater() : Command() {

    private lateinit var commandReceiver: CommandReceiver
    constructor(commandReceiver: CommandReceiver) : this() {
        this.commandReceiver = commandReceiver
    }

    private val info = "Удаляет из коллекции все элементы, размер которых превышает указанный"
    private val argsTypes = mapOf(
        "product" to "Product"
    )

    override fun getInfo(): String {
        return info
    }

    override fun getArgsTypes(): Map<String, String> {
        return argsTypes
    }


    override fun execute(args: Map<String, String>, channel: SocketChannel,username: String) {
        if (Validator.verifyArgs(2, args)) {
            try {
                commandReceiver.removeGreater(args, channel,username)
            } catch (e:Exception) {
                throw InvalidArgumentException("Ожидал найти аргумент, но он не был найден")
            }

        } else throw InvalidArgumentException("Были введены неверные аргументы. Используйте команду HELP для проверки")
    }
}