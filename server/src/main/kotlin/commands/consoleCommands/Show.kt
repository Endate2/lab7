package commands.consoleCommands

import commands.CommandReceiver
import serverUtils.Validator
import exceptions.InvalidArgumentException
import java.nio.channels.SocketChannel


class Show() : Command() {

    private lateinit var commandReceiver: CommandReceiver
    constructor(commandReceiver: CommandReceiver) : this() {
        this.commandReceiver = commandReceiver
    }

    private val info = "Выводит все элементы коллекции"
    private val argsTypes = mapOf<String, String>()

    override fun getInfo(): String {
        return info
    }

    override fun getArgsTypes(): Map<String, String> {
        return argsTypes
    }


    override fun execute(args: Map<String, String>,channel: SocketChannel,username: String) {
        if (Validator.verifyArgs(1, args)) {
            commandReceiver.show(args,channel)
        } else throw InvalidArgumentException("Были введены неверные аргументы. Используйте команду HELP для проверки")
    }

}