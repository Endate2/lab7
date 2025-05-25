package utils

import exceptions.InvalidInputException
import exceptions.RecursiveCallException
import exceptions.FileNotFoundException
import java.io.File
import java.io.FileReader
import java.io.InputStream
import java.util.*

class InputManager(private val outputManager: OutputManager) {
    private val scanners: Stack<Scanner> = Stack()
    private var inputStream = System.`in`
    private var scriptMode = false
    private var files:Stack<File> = Stack()

    constructor(inputStream: InputStream, outputManager: OutputManager) : this(outputManager) {
        this.inputStream = inputStream
    }
    init {
        scanners.push(Scanner(inputStream))
    }


    fun read(): String {
        return if (scanners.peek().hasNextLine()) {
            scanners.peek().nextLine()
        } else {
            if (scriptMode) {
                finishScriptReader()
                ""
            } else {
                throw InvalidInputException()
            }
        }
    }

    fun startScriptReader(filepath: String) {
        val file = File(filepath).absoluteFile // Получаем абсолютный путь

        if (!file.exists()) {
            throw FileNotFoundException("Файл $filepath не найден")
        }

        if (file in files) {
            throw RecursiveCallException()
        }

        outputManager.println("Начато выполнение скрипта из файла ${file.name}.")
        scanners.push(Scanner(file.reader()))
        files.push(file)
        scriptMode = true
        outputManager.silentMode()
    }

    private fun finishScriptReader() {
        scriptMode = false
        scanners.pop()
        outputManager.enableOutput()
        outputManager.println("Скрипт из файла был выполнен")
        files.pop()
    }
}