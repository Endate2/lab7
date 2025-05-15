package exceptions


class InvalidInputException : Exception() {
    override val message: String = "Невозможный ввоод"
}