package serverUtils

import collection.CollectionManager
import commands.CommandInvoker
import commands.CommandReceiver
import commands.consoleCommands.*
import utils.*
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import tokenutils.JWTManager
import java.nio.channels.SocketChannel
import java.util.concurrent.Executors
import users.UserManager

class Console {
    private val connectionManager = ConnectionManager()
    val dbManager = DBManager(
        "jdbc:postgresql://localhost:5432/studs",
        "s468012",
        System.getenv("dbPassword") ?: "anton"
    )
    private val fileManager = FileManager(dbManager)
    private val collectionManager = CollectionManager(dbManager)
    private val commandInvoker = CommandInvoker(connectionManager)
    private val jwtManager = JWTManager() // Добавьте эту строку
    private val commandReceiver = CommandReceiver(collectionManager, connectionManager)
    private val jsonCreator = JsonWorker()
    private val logger: Logger = LogManager.getLogger(Console::class.java)
    private var executeFlag = true
    private val userManager = UserManager(dbManager)
    private val executor = Executors.newFixedThreadPool(10)
    private val handlerExecutor = Executors.newFixedThreadPool(10)

    fun start(actions: ConnectionManager.() -> Unit) {
        connectionManager.actions()
    }

    fun initialize() {
        dbManager.initDB()
        logger.info("Инициализация сервера")
        commandInvoker.register("show", Show(commandReceiver))
        commandInvoker.register("add", Add(commandReceiver))
        commandInvoker.register("remove_by_id", RemoveID(commandReceiver))
        commandInvoker.register("info", Info(commandReceiver))
        commandInvoker.register("update_by_id", Update(commandReceiver))
        commandInvoker.register("clear", Clear(commandReceiver))
        commandInvoker.register("filter_by_price", FilterByPrice(commandReceiver))
        commandInvoker.register("filter_by_unitofmeasure", FilterByUnitOfMeasure(commandReceiver))
        commandInvoker.register("filter_by_creationdate", GroupByCreationDate(commandReceiver))
        commandInvoker.register("remove_greater", RemoveGreater(commandReceiver))
        commandInvoker.register("remove_lower", RemoveLower(commandReceiver))

        logger.debug("Все команды сервера были загружены")
        fileManager.load(collectionManager)
        logger.info("Коллекция загружена")
    }

    fun startInteractiveMode() {
        logger.info("Сервер готов к приему команд")

        while (executeFlag) {
            val clientChannel = connectionManager.acceptClient()
            if (clientChannel != null) {
                Thread {
                    handleClient(clientChannel)
                }.start()
            }
        }

        executor.shutdown()
        connectionManager.stop()
        logger.info("Сервер закрыт")
    }

    private fun handleClient(clientChannel: SocketChannel) {
        try {
            while (true) {
                val query = connectionManager.receive(clientChannel) ?: break

                // Передаем обработку запроса в отдельный поток из handlerExecutor
                handlerExecutor.submit {
                    when (query.queryType) {
                        ExecuteType.COMMAND_READ -> {
                            val token = query.token // Получаем токен из запроса
                            if (token == null || !jwtManager.validateToken(token)) {
                                connectionManager.send(
                                    Answer(AnswerType.AUTH_ERROR, "Требуется авторизация", receiver = ""),
                                    clientChannel
                                )
                                return@submit
                            }
                            val username = jwtManager.getUsername(token)!!
                            commandInvoker.executeCommand(query, clientChannel, username)
                        }
                        ExecuteType.DISCHARGE -> {
                            val receiver = query.args["sender"]!!
                            logger.trace("Получен запрос на инициализацию подключения")

                            val sendingInfo = mutableMapOf(
                                "commands" to commandInvoker.getCommandMap().mapValues { it.value.getInfo() },
                                "arguments" to commandInvoker.getCommandMap().mapValues { jsonCreator.objectToString(it.value.getArgsTypes()) }
                            )

                            val answer = Answer(AnswerType.SYSTEM, jsonCreator.objectToString(sendingInfo), receiver = receiver)
                            connectionManager.send(answer, clientChannel)
                        }
                        ExecuteType.AUTHORIZATION -> {
                            val receiver = query.args["username"]!!
                            logger.info("Получен запрос на авторизацию")
                            if (query.message != "logout") {
                                fileManager.load(collectionManager)
                                val answer = if (userManager.userExists(query.args["username"]!!)) {
                                    if (userManager.login(query.args["username"]!!, query.args["password"]!!)) {
                                        val token = jwtManager.createToken(query.args["username"]!!)
                                        Answer(AnswerType.OK, token, receiver = receiver)
                                    } else {
                                        Answer(AnswerType.AUTH_ERROR, "Неправильный пароль", receiver = receiver)
                                    }
                                } else {
                                    if (userManager.register(query.args["username"]!!, query.args["password"]!!)) {
                                        val token = jwtManager.createToken(query.args["username"]!!)
                                        Answer(AnswerType.OK, token, receiver = receiver)
                                    } else {
                                        Answer(AnswerType.AUTH_ERROR, "Нельзя зарегистрироваться", receiver = receiver)
                                    }
                                }
                                connectionManager.send(answer, clientChannel)
                            }
                        }
                        null -> {
                            logger.warn("Получен нулевой запрос, соединение закрывается")
                            connectionManager.closeClient(clientChannel)
                        }
                    }
                }
            }
        } catch (e: Exception) {
            logger.error("Ошибка обработки клиента: ${e.message}")
            connectionManager.closeClient(clientChannel)
        }
    }

    fun stop() {
        executeFlag = false
        executor.shutdown()
        handlerExecutor.shutdown() // Не забываем закрыть новый пул
        connectionManager.stop()
        logger.info("Сервер закрыт")
    }

    fun save() {
        try {
            fileManager.save(collectionManager, userManager)
            logger.info("Коллекция успешно сохранена")
        } catch (e: Exception) {
            logger.warn("Коллекция не была сохранена: ${e.message}")
        }
    }
}