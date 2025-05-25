package serverUtils

import kotlinx.serialization.json.Json
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import utils.Answer
import utils.Query
import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.nio.channels.ServerSocketChannel
import java.nio.channels.SocketChannel
import java.nio.charset.Charset
import java.util.concurrent.Executors

class ConnectionManager {
    private var host = "localhost"
    private var port = 3333
    private val logger: Logger = LogManager.getLogger(ConnectionManager::class.java)
    internal lateinit var serverSocketChannel: ServerSocketChannel
    private val senderExecutor = Executors.newFixedThreadPool(10)
    private val readerExecutor = Executors.newCachedThreadPool()

    fun startServer(host: String, port: Int) {
        this.host = host
        this.port = port
        serverSocketChannel = ServerSocketChannel.open()
        var unbound = true
        val maxPort = 49151
        var attempts = 0
        val maxAttempts = 100

        while (unbound && attempts < maxAttempts && this.port <= maxPort) {
            try {
                serverSocketChannel.bind(InetSocketAddress(host, this.port))
                unbound = false
                logger.info("TCP-сервер запущен на ${serverSocketChannel.localAddress}")
            } catch (e: Exception) {
                attempts++
                val oldPort = this.port
                this.port++
                logger.warn("Порт $oldPort занят, пробуем ${this.port} (попытка $attempts/$maxAttempts)")
                Thread.sleep(100)
            }
        }

        if (unbound) {
            throw IllegalStateException("Не удалось привязаться ни к одному порту после $attempts попыток")
        }
    }

    fun acceptClient(): SocketChannel? {
        return try {
            val clientChannel = serverSocketChannel.accept()
            logger.info("Новый клиент: ${clientChannel.remoteAddress}")
            clientChannel
        } catch (e: Exception) {
            logger.error("Ошибка при принятии подключения: ${e.message}")
            null
        }
    }

    fun receive(channel: SocketChannel): Query? {
        return try {
            val lengthBuffer = ByteBuffer.allocate(4)
            while (lengthBuffer.hasRemaining()) {
                if (channel.read(lengthBuffer) == -1) {
                    logger.info("Клиент ${channel.remoteAddress} отключился (конец потока)")
                    closeClient(channel)
                    return null
                }
            }
            lengthBuffer.flip()
            val messageLength = lengthBuffer.int

            val messageBuffer = ByteBuffer.allocate(messageLength)
            while (messageBuffer.hasRemaining()) {
                if (channel.read(messageBuffer) == -1) {
                    logger.info("Клиент ${channel.remoteAddress} отключился (конец потока)")
                    closeClient(channel)
                    return null
                }
            }
            messageBuffer.flip()
            val jsonQuery = Charset.defaultCharset().decode(messageBuffer).toString()
            logger.info("Получено от ${channel.remoteAddress}: $jsonQuery")
            Json.decodeFromString(Query.serializer(), jsonQuery)
        } catch (e: Exception) {
            logger.error("Ошибка получения: ${e.message}")
            closeClient(channel)
            null
        }
    }

    fun send(answer: Answer, channel: SocketChannel) {
        senderExecutor.submit {
            try {
                val jsonAnswer = Json.encodeToString(Answer.serializer(), answer)
                val messageBytes = jsonAnswer.toByteArray()
                val lengthBytes = ByteBuffer.allocate(4).putInt(messageBytes.size).array()

                channel.write(ByteBuffer.wrap(lengthBytes))
                channel.write(ByteBuffer.wrap(messageBytes))
                logger.info("Отправлено ${channel.remoteAddress}: $jsonAnswer")
            } catch (e: Exception) {
                logger.error("Ошибка отправки: ${e.message}")
                closeClient(channel)
            }
        }
    }

    fun closeClient(channel: SocketChannel) {
        channel.close()
        logger.info("Клиент ${channel.remoteAddress} отключен")
    }

    fun stop() {
        try {
            serverSocketChannel.close()
            senderExecutor.shutdown()
            readerExecutor.shutdown()
            logger.info("Сервер завершен")
        } catch (e: Exception) {
            logger.error("Ошибка при остановке сервера: ${e.message}")
        }
    }
}