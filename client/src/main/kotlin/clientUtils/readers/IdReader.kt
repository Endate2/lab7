package clientUtils.readers

import utils.InputManager
import utils.OutputManager

class IdReader(val outputManager: OutputManager, val inputManager: InputManager) {

    fun read(message: String, canBeNull: Boolean): Int? {
        outputManager.println(message)

        while (true) {
            val input = inputManager.read().trim()

            when {
                input.isEmpty() -> {
                    outputManager.println("Это поле не может быть пустым. Пробовать снова: ")
                }
                input.equals("\\null", ignoreCase = true) -> {
                    if (canBeNull) {
                        return null
                    } else {
                        outputManager.println("Это поле не может быть null. Пробовать снова: ")
                    }
                }
                else -> {
                    try {
                        return input.toInt().takeIf { it > 0 }
                            ?: throw NumberFormatException("ID должен быть положительным числом")
                    } catch (e: NumberFormatException) {
                        outputManager.println("Некорректный формат ID. Введите положительное целое число: ")
                    }
                }
            }
        }
    }
}