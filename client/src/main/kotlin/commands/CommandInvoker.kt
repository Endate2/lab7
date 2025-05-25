package commands

import commands.consoleCommands.Command
import utils.OutputManager


class CommandInvoker(private val outputManager: OutputManager) {
    private var commandMap:Map<String, Command> = mapOf()
    private var commandsHistory:Array<String> = arrayOf()


    fun register(name: String, command: Command) {
        commandMap += name to command
    }

    fun clearCommandMap() {
        commandMap = mapOf()
    }

    fun executeCommand(query: List<String>,username:String, token: String) {
        try {
            if (query.isNotEmpty()) {
                commandsHistory += query[0]
                val command: Command = commandMap[query[0]]!!
                command.execute(query.slice(1 until query.size), username, token)
            }
        } catch (e:IllegalStateException) {
            outputManager.println("Команды ${query[0]} не существует")
        } catch (e:NullPointerException) {
            outputManager.println("Команды ${query[0]} не существует")
        }
    }

    fun getCommandMap() : Map<String, Command> {
        return commandMap
    }
}