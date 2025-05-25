package clientUtils.readers

import utils.InputManager
import utils.OutputManager

class LongReader(private val outputManager: OutputManager, private val inputManager: InputManager) {

    fun read(message: String, a:Long): Long {
        outputManager.println(message)
        var value: Long = a-1

        while (value  <= a) {
            try {
                value = inputManager.read().trim().toLong()

                if (value <= a) {
                    outputManager.println("Это поле не может быть ниже $a")
                }
            } catch (e: Exception) {
                outputManager.println("Это значение должно быть типа Long")
                continue
            }
        }
        return value
    }
}