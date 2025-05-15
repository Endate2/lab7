package clientUtils.readers

import utils.InputManager
import utils.OutputManager

class IntReader(private val outputManager: OutputManager, private val inputManager: InputManager) {

    fun read(message: String, a:Int): Int {
        outputManager.println(message)
        var value: Int = a-1

        while (value  <= a) {
            try {
                value = inputManager.read().trim().toInt()

                if (value <= a) {
                    outputManager.println("Это поле не может быть ниже $a")
                }
            } catch (e: Exception) {
                outputManager.println("Это значение должно быть типа Int")
                continue
            }
        }
        return value
    }
}