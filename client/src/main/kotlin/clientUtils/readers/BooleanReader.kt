package clientUtils.readers

import utils.InputManager
import utils.OutputManager


class BooleanReader(private val outputManager: OutputManager, private val inputManager: InputManager) {

    fun read(message: String) : Boolean {
        outputManager.println(message)

        var loyalty: Boolean? = null
        do {
            try {
                loyalty = inputManager.read().trim().lowercase().toBooleanStrict()
            } catch (e: Exception) {
                outputManager.println("Вам необходимо ввести Boolean-type значение: ")
            }
        } while (loyalty == null)

        return loyalty
    }
}