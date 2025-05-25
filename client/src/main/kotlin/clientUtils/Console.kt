package clientUtils

import clientUtils.readers.StringReader
import commands.*
import commands.consoleCommands.*
import exceptions.InvalidInputException
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import utils.*
import kotlin.concurrent.thread

class Console {
    private val connectionManager = ConnectionManager("localhost", 3333)
    private val RECONNECTION_INTERVAL = 10000L // 5 секунд между попытками переподключения

    private val outputManager = OutputManager()
    private val inputManager = InputManager(outputManager)

    private val commandInvoker = CommandInvoker(outputManager)
    private val commandReceiver = CommandReceiver(commandInvoker, outputManager, inputManager, connectionManager)

    private val jsonCreator = JsonWorker()

    private val logger: Logger = LogManager.getLogger(Console::class.java)
    private var connectionCheckThread: Thread? = null
    private var serverAvailable = false
    private var token: String = "" // Храним токен после авторизации
    private var password = "щкуо"
    private var username = ""
    var authorized: Boolean = false
    fun getConnection() {
        serverAvailable = connectionManager.connect()
        if (serverAvailable) {
            logger.debug("Подключение к серверу")
            authorize() // Добавлено: сначала авторизация
            initialize()
            startConnectionMonitor()
        } else {
            handleConnectionFailure()
        }
    }

    private fun startConnectionMonitor() {
        connectionCheckThread = thread {
            while (true) {
                Thread.sleep(RECONNECTION_INTERVAL)
                if (!connectionManager.isConnected()) {
                    serverAvailable = false
                    outputManager.println("\nСервер недоступен! Пытаемся переподключиться...")
                    logger.warn("Сервер недоступен! Пытаемся переподключиться...")
                    tryReconnect()
                }
            }
        }
    }

    private fun tryReconnect() {
        while (!serverAvailable) {
            serverAvailable = connectionManager.connect()
            if (serverAvailable) {
                outputManager.println("Подключение восстановлено!")
                logger.info("Подключение восстановлено!")
                commandReceiver.trySendBufferedCommands() // Добавлен вызов отправки буферных команд
                outputManager.print("> ")
                initialize()
            } else {
                Thread.sleep(RECONNECTION_INTERVAL)
                outputManager.println("Попытка переподключения...")
                logger.info("Попытка переподключения...")
            }
        }
    }

    private fun handleConnectionFailure() {
        outputManager.println("Нет подключения к серверу")
        logger.warn("Нет подключения к серверу")
        outputManager.println("Переподключиться? [да/нет]")
        outputManager.print("> ")
        var query = inputManager.read().trim().lowercase().split(" ")
        while ((query[0] != "да") and (query[0] != "нет")) {
            outputManager.println("Неправильный ввод\nПереподключиться? [да/нет]")
            outputManager.print("> ")
            query = inputManager.read().trim().lowercase().split(" ")
        }
        if (query[0] == "да") {
            getConnection()
        } else {
            registerBasicCommands()
        }
    }

    private fun registerBasicCommands() {
        commandInvoker.register("help", Help(commandReceiver))
        commandInvoker.register("exit", Exit(connectionManager))
        commandInvoker.register("execute_script", ScriptFromFile(commandReceiver))
        logger.debug("Выгрузка базовых команд на сервер")
    }

    fun initialize() {
        if (!serverAvailable) return

        val query = Query(
            queryType = ExecuteType.DISCHARGE,
            message = "",
            args = mapOf("sender" to username, "password" to password),
        )
        val answer = connectionManager.checkedSendReceive(query)
        logger.debug("Отправлен запрос на выгрузку")
        when (answer.answerType) {
            AnswerType.ERROR -> {
                outputManager.println(answer.message)
                serverAvailable = false
                tryReconnect()
            }
            else -> {
                val serverCommands = jsonCreator.stringToObject<Map<String, Map<String, String>>>(answer.message)
                logger.info("Получены команды от сервера: ${serverCommands["commands"]!!.keys.toList()}")

                commandInvoker.clearCommandMap()

                for (i in serverCommands["commands"]!!.keys) {
                    commandInvoker.register(i, ServerCommand(commandReceiver, i, serverCommands["commands"]!![i]!!, jsonCreator.stringToObject(serverCommands["arguments"]!![i]!!)
                    )
                    )
                }
            }
        }

        registerBasicCommands()
    }
    fun authorize() {
        outputManager.surePrint("Войдите в приложение или зарегистрируйтесь, чтобы пользоваться коллекцией: ")
        val username = StringReader(outputManager, inputManager).read("Username: ")
        val password = StringReader(outputManager, inputManager).read("Password: ")

        val query = Query(ExecuteType.AUTHORIZATION, "", mutableMapOf("username" to username, "password" to password))
        val answer = connectionManager.checkedSendReceive(query)
        logger.debug("Отправленный запрос на авторизацию")
        if (answer.answerType == AnswerType.AUTH_ERROR || answer.answerType == AnswerType.ERROR) {
            outputManager.println(answer.message)
            authorize()
        } else {
            logger.debug("Авторизован")
            token = answer.message
            outputManager.surePrint("Добро пожаловать в консольное приложение, "+ username)
            authorized = true
        }
    }
    fun startInteractiveMode() {
        var executeFlag: Boolean? = true
        outputManager.surePrint("Ждем ввод пользователя...")

        do {
            try {
                if (!serverAvailable) {

                    tryReconnect()
                    if (!serverAvailable) continue
                }

                outputManager.print("> ")
                val query = inputManager.read().trim().split(" ")
                if (query[0] != "") {
                    if (!serverAvailable && query[0] !in listOf("help", "exit", "execute_script")) {
                        outputManager.println("Команда недоступна: сервер не отвечает")
                        continue
                    }
                    commandInvoker.executeCommand(query,username,token)
                    executeFlag = commandInvoker.getCommandMap()[query[0]]?.getExecutionFlag()
                }

            } catch (e: InvalidInputException) {
                outputManager.surePrint(e.message)
                logger.warn(e.message)
                break
            } catch (e: Exception) {
                outputManager.surePrint(e.message.toString())
                logger.warn(e.message)
            }

        } while (executeFlag != false)

        connectionCheckThread?.interrupt()
    }
}