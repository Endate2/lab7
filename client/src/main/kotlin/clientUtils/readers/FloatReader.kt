package clientUtils.readers


import utils.InputManager
import utils.OutputManager


class FloatReader(private val outputManager: OutputManager, private val inputManager: InputManager) {

    fun read(message: String, canBeNull: Boolean): Float? {
        outputManager.println(message)

        var value = 0F

        do {
            val input = inputManager.read()

            if (input == "\\null") {
                if (canBeNull) {
                    return null
                } else {
                    outputManager.println("Это поле не может быть пустым")
                }

            } else {
                try {
                    value = input.trim().toFloat()
                } catch (e:Exception) {
                    outputManager.println("Вам необходимо ввести Float-type значение")
                    continue
                }
            }

            if (value <= 0) {
                outputManager.println("Значение этого поля не может быть меньше нуля")
            }

        } while (value <= 0)

        return value
    }
}