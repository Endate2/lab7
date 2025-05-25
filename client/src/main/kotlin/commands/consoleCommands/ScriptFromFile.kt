package commands.consoleCommands

import commands.CommandReceiver
import clientUtils.Validator
import exceptions.InvalidArgumentException


class ScriptFromFile(
    private val commandReceiver: CommandReceiver
): Command() {

    override fun getInfo(): String {
        return "Считывает и выполняет скрипт из предоставленного файла (скрипт должен содержать те же команды, что и в интерактивном режиме)."
    }


    override fun execute(args: List<String>,username: String,token: String) {
        if (Validator.verifyArgs(1, args)) {
            try {
                commandReceiver.executeScript(args[0])
            } catch (e:IndexOutOfBoundsException) {
                throw InvalidArgumentException("Ожидался аргумент, но он не был введен")
            }
        } else throw InvalidArgumentException("Были введены неверные аргументы. Используйте команду HELP для проверки")
    }

}