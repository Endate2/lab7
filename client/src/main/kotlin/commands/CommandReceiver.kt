package commands

import basicClasses.*
import commands.consoleCommands.Command
import clientUtils.*
import clientUtils.readers.*
import utils.*

class CommandReceiver(private val commandInvoker: CommandInvoker,
                      private val outputManager: OutputManager,
                      private val inputManager: InputManager,
                      private val connectionManager: ConnectionManager
) {

    private val creator = Creator(outputManager, inputManager)
    private val enumReader = EnumReader(outputManager, inputManager)
    private val jsonCreator = JsonWorker()
    private val commandBuffer = mutableListOf<Pair<String, Map<String, String>>>()

    /**
     * Gets a command map from [commandInvoker], and prints each command's info or info of provided command in arg
     */
    fun help() {
        commandInvoker.getCommandMap().forEach { (name: String?, command: Command) -> outputManager.println(name?.lowercase() + " - " + command.getInfo()) }
    }

    fun executeScript(filepath: String) {
        inputManager.startScriptReader(filepath)
    }

    fun serverCommand(commandName: String, args: Map<String, String>,username: String, token: String) {
        val sending = mutableMapOf<String, String>()
        sending["sender"] = username
        for ((argName, argValue) in args) {
            sending[argName] = when (argValue) {
                "UnitOfMeasure" -> jsonCreator.objectToString(enumReader.read<UnitOfMeasure>("Введите UnitOfMeasure из списка: ", true))
                "Organization" -> jsonCreator.objectToString(creator.createOrganization())
                "Coordinates" -> jsonCreator.objectToString(creator.createCoordinates())
                "Product" -> jsonCreator.objectToString(creator.createProduct())
                "Int" -> jsonCreator.objectToString(IdReader(outputManager, inputManager).read("Введите Id: ", true))
                "Price" -> jsonCreator.objectToString(IdReader(outputManager, inputManager).read("Введите цену: ", true))
                "Quantity" -> jsonCreator.objectToString(IdReader(outputManager, inputManager).read("Введите количество элементов, которые хотите добавить: ", true))
                else -> ""
            }
        }

        if (!connectionManager.isConnected()) {
            commandBuffer.add(Pair(commandName, sending))
            outputManager.println("Сервер недоступен. Команда сохранена в буфер.")
            return
        }

        try {
            val query = Query(ExecuteType.COMMAND_READ, commandName, sending,token)
            val answer = connectionManager.checkedSendReceive(query)
            outputManager.println(answer.message)
        } catch (e: Exception) {
            commandBuffer.add(Pair(commandName, sending))
            outputManager.println("Ошибка при отправке команды. Команда сохранена в буфер.")
        }
    }

    fun trySendBufferedCommands() {
        if (!connectionManager.isConnected()) return

        val iterator = commandBuffer.iterator()
        while (iterator.hasNext()) {
            val (commandName, args) = iterator.next()
            try {
                val query = Query(ExecuteType.COMMAND_READ, commandName, args)
                val answer = connectionManager.checkedSendReceive(query)
                outputManager.println("Выполнение отложенной команды $commandName:")
                outputManager.println(answer.message)
                iterator.remove()
            } catch (e: Exception) {
                outputManager.println("Не удалось выполнить отложенную команду $commandName. Она останется в буфере.")
                break // Прерываем цикл при первой же ошибке
            }
        }
    }
}