package exceptions

class NoEnvironmentVariableFound : Exception() {
    override val message: String = "Не найдено значение переменной окружения 'COLLECTION'"
}