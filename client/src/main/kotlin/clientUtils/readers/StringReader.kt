package clientUtils.readers

import utils.InputManager
import utils.OutputManager


class StringReader(private val outputManager: OutputManager, private val inputManager: InputManager) {

    fun read(message: String): String {
        outputManager.println(message)

        var value:String = inputManager.read().trim()

        while (value.isEmpty()) {
            outputManager.println("Это поле не может быть пустым")
            outputManager.println(message)

            value = inputManager.read().trim()
        }

        return value
    }
}
