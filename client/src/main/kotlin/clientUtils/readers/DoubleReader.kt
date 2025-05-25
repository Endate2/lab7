package clientUtils.readers

import utils.InputManager
import utils.OutputManager

class DoubleReader(private val outputManager: OutputManager, private val inputManager: InputManager) {
    fun read(message: String, maxValue: Int): Double {
        outputManager.println(message)

        while (true) {
            try {
                val input = inputManager.read().trim()
                val value = input.toDouble()

                if (value > maxValue) {
                    outputManager.println("Это поле не может быть больше, чем $maxValue")
                } else {
                    return value
                }
            } catch (e: NumberFormatException) {
                outputManager.println("Вам необходимо ввести Double-type значение")
            }
        }
    }
}