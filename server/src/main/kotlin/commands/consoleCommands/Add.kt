package commands.consoleCommands

import commands.CommandReceiver
import serverUtils.Validator
import exceptions.InvalidArgumentException
import serverUtils.ConnectionManager
import java.nio.channels.SocketChannel


class Add(private val commandReceiver: CommandReceiver) : Command() {
    private val info = "Adds a new element into the collection"
    private val argsTypes = mapOf(
        "product" to "Product"
    )

    override fun getInfo(): String {
        return info
    }

    override fun getArgsTypes(): Map<String, String> {
        return argsTypes
    }


    override fun execute(args: Map<String, String>,channel: SocketChannel,username: String) {
        if (Validator.verifyArgs(2, args)) {
            commandReceiver.add(args, channel,username)
        } else throw InvalidArgumentException("Были введены неверные аргументы. Используйте команду HELP для проверки"+ args)
    }
}