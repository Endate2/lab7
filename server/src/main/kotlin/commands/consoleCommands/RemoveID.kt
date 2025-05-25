package commands.consoleCommands

import commands.CommandReceiver
import serverUtils.Validator
import exceptions.InvalidArgumentException
import java.nio.channels.SocketChannel


class RemoveID() : Command() {

    private lateinit var commandReceiver: CommandReceiver
    constructor(commandReceiver: CommandReceiver) : this() {
        this.commandReceiver = commandReceiver
    }

    private val info = "Удалить элемент по Id"
    private val argsTypes = mapOf(
        "id" to "Int"
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
                commandReceiver.removeByID(args, channel,username)
            } catch (e:Exception) {
                throw InvalidArgumentException("Id не найден")
            }
        } else throw InvalidArgumentException("Были введены неверные аргументы. Используйте команду HELP для проверки")
    }


}