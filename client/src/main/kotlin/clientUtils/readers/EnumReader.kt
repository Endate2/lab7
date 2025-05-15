package clientUtils.readers

import utils.InputManager
import utils.OutputManager


class EnumReader(val outputManager: OutputManager, val inputManager: InputManager) {
    inline fun <reified T : Enum<T>> enumContains(name: String): Boolean {
        return enumValues<T>().any { it.name == name }
    }

    inline fun <reified T : Enum<T>> read(message: String, canBeNull:Boolean): T? {
        outputManager.println(message)
        for (item in enumValues<T>()) {
            outputManager.println(item.toString())
        }

        var value = inputManager.read().trim().uppercase()

        while (!enumContains<T>(value)) {
            value = if (value.isEmpty()) {
                outputManager.println("Это поле не может быть пустым. Пробовать снова: ")
                inputManager.read().trim().uppercase()
            } else if (value == "\\null") {
                if (canBeNull) {
                    return null
                } else {
                    outputManager.println("Это поле не может быть пустым. Пробовать снова: ")
                    inputManager.read().trim().uppercase()
                }
            } else {
                outputManager.println("Указанный тип enum не существует. Пробовать снова: ")
                inputManager.read().trim().uppercase()
            }
        }
        return enumValueOf<T>(value)
    }
}
