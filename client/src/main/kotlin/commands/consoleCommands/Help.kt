package commands.consoleCommands

import commands.CommandReceiver
import clientUtils.Validator
import exceptions.InvalidArgumentException


class Help(
    private val commandReceiver: CommandReceiver
) : Command() {

    override fun getInfo(): String {
        return "Выводит информацию обо всех командах"
    }


    override fun execute(args: List<String>,username: String,token: String) {
        if (Validator.verifyArgs(0, args)) {
                commandReceiver.help()

        } else throw InvalidArgumentException("Были введены неверные аргументы. Используйте команду HELP для проверки")
    }
}
