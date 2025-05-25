package commands.consoleCommands

import clientUtils.Validator
import exceptions.InvalidArgumentException
import utils.Query
import clientUtils.ConnectionManager
import utils.ExecuteType

class Exit(private val connectionManager: ConnectionManager) : Command() {

    override fun getInfo(): String {
        return "Завершение работы консольного приложения"
    }

    override fun execute(args: List<String>,username: String,token: String) {
        if (Validator.verifyArgs(0, args)) {
            setFlag(false)
            connectionManager.send(Query(ExecuteType.AUTHORIZATION, "logout", mutableMapOf()))
        } else throw InvalidArgumentException("Были введены неверные аргументы. Используйте команду HELP для проверки")
    }
}