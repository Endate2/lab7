package commands

import commands.consoleCommands.Command
import utils.Answer
import utils.AnswerType
import serverUtils.ConnectionManager
import utils.Query
import java.nio.channels.SocketChannel


class CommandInvoker(private val connectionManager: ConnectionManager) {
    private var commandMap:Map<String, Command> = mapOf()
    private var commandsHistory:Array<String> = arrayOf()

    fun register(name: String, command: Command) {
        commandMap += name to command
    }

    fun executeCommand(query: Query, channel: SocketChannel, username: String) {
        try {
            val commandName = query.message
            commandsHistory += commandName

            val command = commandMap[commandName] ?: throw IllegalArgumentException("Команда не найдена")
            command.execute(query.args, channel, username)
        } catch (e: Exception) {
            val answer = Answer(AnswerType.ERROR, e.toString(), receiver = query.args["sender"]!!)
            connectionManager.send(answer, channel)
        }
    }


    fun getCommandMap() : Map<String, Command> {
        return commandMap
    }
}