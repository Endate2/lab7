package utils

import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer

class JsonWorker {
    inline fun <reified T> objectToString(clazz: T): String { // Превращает любой объект (T) в JSON-строку
        return Json.encodeToString(serializer(), clazz)
    }

    inline fun <reified T> stringToObject(json: String): T { // Преобразует JSON-строку обратно в объект нужного типа
        return Json.decodeFromString(serializer(), json)
    }
}