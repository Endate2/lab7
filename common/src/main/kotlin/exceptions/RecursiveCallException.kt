package exceptions

class RecursiveCallException : Exception() {
    override val message: String = "Скрипт может вызвать рекурсивный вызов, поэтому эта команда пропускается"
}