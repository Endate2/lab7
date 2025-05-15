package commands.consoleCommands

import commands.CommandReceiver
import serverUtils.Validator
import exceptions.InvalidArgumentException
import java.nio.channels.SocketChannel


class GroupByCreationDate() : Command() {

    private lateinit var commandReceiver: CommandReceiver
    constructor(commandReceiver: CommandReceiver) : this() {
        this.commandReceiver = commandReceiver
    }

    private val info = "Группирует по дате создания"
    private val argsTypes = emptyMap<String, String>() // Аргументы не требуются

    override fun getInfo(): String {
        return info
    }

    override fun getArgsTypes(): Map<String, String> {
        return argsTypes
    }


    override fun execute(args: Map<String, String>, channel: SocketChannel,username: String) {
        if (Validator.verifyArgs(1, args)) {
            try {
                commandReceiver.groupByCreationDate(args, channel)
            } catch (e:Exception) {
                throw InvalidArgumentException("Аргумент не был найден")
            }
        } else throw InvalidArgumentException("Были введены неверные аргументы. Используйте команду HELP для проверки")
    }
}