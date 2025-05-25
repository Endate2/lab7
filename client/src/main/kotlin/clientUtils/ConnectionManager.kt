package clientUtils

import kotlinx.serialization.json.Json
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import utils.*
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.net.InetSocketAddress
import java.net.Socket
import java.net.SocketTimeoutException
import java.nio.ByteBuffer

class ConnectionManager(private val host: String, private val port: Int) {
    private val logger: Logger = LogManager.getLogger(ConnectionManager::class.java)
    private var socket: Socket? = null // tcp соект для соединения с сервером
    private var inputStream: InputStream? = null // поток ввода для чтения данных от сервера
    private var outputStream: OutputStream? = null // поток вывода для отправки данных на сервер

    fun connect(): Boolean {
        return try {
            socket = Socket() // создает новый сокет
            socket!!.connect(InetSocketAddress(host, port), 5000) // устанавливает соединение с сервером по указоному хосту и порту с таймаутом 5 секунд
            inputStream = socket!!.getInputStream() // входной поток
            outputStream = socket!!.getOutputStream() // выходной поток
            logger.debug("Подключен к серверу по адресу $host:$port")
            true
        } catch (e: Exception) {
            logger.error("Ошибка подключения: ${e.message}")
            false
        }
    }


    fun checkedSendReceive(query: Query): Answer {
        return try {
            if (socket == null || !socket!!.isConnected || socket!!.isClosed) {
                connect() // Пытаемся переподключиться
            }
            send(query) // поподробнее
            receive() // поподробнее
        } catch (e: SocketTimeoutException) {
            logger.error("Время ожидания: ${e.message}")
            reconnect()
            Answer(AnswerType.ERROR, "Долгое ожидание. Пробуем переподключиться...", receiver = "")
        } catch (e: Exception) {
            logger.error("Ошибка: ${e.message}")
            reconnect()
            Answer(AnswerType.ERROR, "Соединение потеряно. Пробуем переподключиться...",receiver = "")
        }
    }

    private fun reconnect() {
        close() // Закрываем старое соединение
        Thread.sleep(1000) // Пауза перед переподключением
        connect() // Пытаемся подключиться снова
    }
    fun isConnected(): Boolean {
        return socket != null && socket!!.isConnected && !socket!!.isClosed && try {
            socket!!.sendUrgentData(0xFF) // Проверка активности соединения
            true
        } catch (e: IOException) {
            false
        }
    }

    fun send(query: Query) {
        val json = Json.encodeToString(Query.serializer(), query)
        val messageBytes = json.toByteArray()
        val lengthBytes = ByteBuffer.allocate(4).putInt(messageBytes.size).array()

        // Отправляем длину сообщения
        outputStream!!.write(lengthBytes)
        // Отправляем само сообщение
        outputStream!!.write(messageBytes)
        outputStream!!.flush()
        logger.info("Отправлено на сервер: $json")
    }

    fun receive(): Answer {
        // Читаем длину сообщения (4 байта)
        val lengthBytes = ByteArray(4)
        inputStream!!.read(lengthBytes)
        val messageLength = ByteBuffer.wrap(lengthBytes).int

        // Читаем само сообщение
        val messageBytes = ByteArray(messageLength)
        var bytesRead = 0
        while (bytesRead < messageLength) {
            bytesRead += inputStream!!.read(messageBytes, bytesRead, messageLength - bytesRead)
        }
        val json = String(messageBytes)
        logger.info("Получено от сервера: $json")
        return Json.decodeFromString(Answer.serializer(), json)
    }

    fun close() {
        socket?.close()
        logger.info("Соединение закрыто")
    }
}