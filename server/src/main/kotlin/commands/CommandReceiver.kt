package commands

import basicClasses.*
import collection.CollectionManager
import exceptions.InvalidArgumentException
import serverUtils.*
import utils.*
import java.nio.channels.SocketChannel


class CommandReceiver(private val collectionManager: CollectionManager,
                      private val connectionManager: ConnectionManager
) {

    private val jsonCreator = JsonWorker()


    fun add(args: Map<String, String>,channel: SocketChannel,username:String) {
        try {
            val product = jsonCreator.stringToObject<Product>(args["product"]!!)
            collectionManager.add(product,username)
            val answer = Answer(AnswerType.OK, "Product ${product.getName()} был создан и добавлен в коллекцию",receiver = args["sender"]!!)
            connectionManager.send(answer, channel)
        } catch (e: Exception) {
            val answer = Answer(AnswerType.ERROR, e.message.toString(),receiver = args["sender"]!!)
            connectionManager.send(answer, channel)
        }
    }

    fun removeByID(args: Map<String, String>,channel: SocketChannel,username: String) {
        val id = args["id"]!!

        try {
            val product= collectionManager.getByID(id.toInt())
                ?: throw InvalidArgumentException("Продукт с id: $id не найден")

            collectionManager.remove(product,username)
            val answer = Answer(AnswerType.OK, "Продукт ${product.getName()} был удален",receiver = args["sender"]!!)
            connectionManager.send(answer, channel)

        } catch (e: Exception) {
            val answer = Answer(AnswerType.ERROR, e.message.toString(),receiver = args["sender"]!!)
            connectionManager.send(answer, channel)
        }
    }
    fun show(args: Map<String, String>,channel: SocketChannel) {
        try {
            val answer = Answer(AnswerType.OK, collectionManager.show().joinToString("\n"),receiver = args["sender"]!!)
            connectionManager.send(answer, channel)
        } catch (e: Exception) {
            val answer = Answer(AnswerType.ERROR, e.message.toString(), receiver = args["sender"]!!)
            connectionManager.send(answer, channel)
        }
    }

    fun info(args: Map<String, String>,channel: SocketChannel) {
        try {
            val answer = Answer(AnswerType.OK, collectionManager.getInfo(),receiver = args["sender"]!!)
            connectionManager.send(answer, channel)
        } catch (e: Exception) {
            val answer = Answer(AnswerType.ERROR, e.message.toString(),receiver = args["sender"]!!)
            connectionManager.send(answer, channel)
        }
    }
    fun updateByID(args: Map<String, String>,channel: SocketChannel,username:String) {
        val id = args["id"]!!

        try {
            val oldProduct = collectionManager.getByID(id.toInt())
                ?: throw InvalidArgumentException("Нет продукта с Id: $id")
            val newProduct = jsonCreator.stringToObject<Product>(args["product"]!!)
            collectionManager.update(newProduct, oldProduct,username)
            val answer = Answer(AnswerType.OK, "Product ${oldProduct.getName()} был обнавлен", receiver = args["sender"]!!)
            connectionManager.send(answer, channel)

        } catch (e: Exception) {
            val answer = Answer(AnswerType.ERROR, e.message.toString(), receiver = args["sender"]!!)
            connectionManager.send(answer, channel)
        }
    }
    fun clear(args: Map<String, String>,channel: SocketChannel,username:String) {
        if (collectionManager.getCollection().size > 0) {
            try {
                collectionManager.clear(username)
                val answer = Answer(AnswerType.OK, "Коллекция была очищена",receiver = args["sender"]!!)
                connectionManager.send(answer, channel)
            } catch (e: Exception) {
                val answer = Answer(AnswerType.ERROR, e.message.toString(),receiver = args["sender"]!!)
                connectionManager.send(answer, channel)
            }
        } else {
            val answer = Answer(AnswerType.ERROR, "Коллекция уже пуста",receiver = args["sender"]!!)
            connectionManager.send(answer, channel)
        }
    }
    fun filterByPrice(args: Map<String, String>,channel: SocketChannel) {
        val price = args["price"]
        val filteredList = mutableListOf<Product>()
        try {

            val collection = collectionManager.getCollection()
            var count = 0

            for (product in collection) {
                if (product.value.getPrice() > price!!.toInt()) {
                    filteredList.add(product.value)
                    count++
                }
            }

            if (filteredList.isEmpty()) {
                throw Exception("Продукт с ценой $price не был найден")
            }

            val answer = Answer(AnswerType.OK, filteredList.joinToString("\n"),receiver = args["sender"]!!)
            connectionManager.send(answer, channel)


        } catch (e: Exception) {
            val answer = Answer(AnswerType.ERROR, e.message.toString(),receiver = args["sender"]!!)
            connectionManager.send(answer, channel)
        }

    }
    fun filterByUnitOfMeasure(args: Map<String, String>,channel: SocketChannel) {
        try {
            val unitOfMeasure = jsonCreator.stringToObject<UnitOfMeasure>(args["unitOfMeasure"]!!)
            val filteredList = mutableListOf<Product>()

            for (i in collectionManager.filter { e -> e.getUnitOfMeasure()!! == unitOfMeasure }) {
                filteredList.add(i)
            }
            if (filteredList.isEmpty()) {
                throw Exception("No Product with $unitOfMeasure were found")
            }

            val answer = Answer(AnswerType.OK, filteredList.joinToString("\n"),receiver = args["sender"]!!)
            connectionManager.send(answer, channel)

        } catch (e: Exception) {
            val answer = Answer(AnswerType.ERROR, e.message.toString(),receiver = args["sender"]!!)
            connectionManager.send(answer, channel)
        }
    }
    fun groupByCreationDate(args: Map<String, String>,channel: SocketChannel) {
        val collection = collectionManager.getCollection()

        if (collection.isEmpty()) {
            val answer = Answer(AnswerType.OK, "Коллекция пуста.",receiver = args["sender"]!!)
            connectionManager.send(answer, channel)
            return
        }

        val groupedByDate = collection
            .map { it.value }
            .groupBy { it.getCreationDate() }  // Предполагается, что creationDate есть в Product

        val result = buildString {
            appendLine("Группировка элементов по дате создания:")
            groupedByDate.forEach { (date, products) ->
                appendLine("- $date: ${products.size} элементов")
            }
        }

        val answer = Answer(AnswerType.OK, result,receiver = args["sender"]!!)
        connectionManager.send(answer, channel)
    }
    fun removeGreater(args: Map<String, String>,channel: SocketChannel,username: String) {

        try {
            val collection = collectionManager.getCollection()
            val product = jsonCreator.stringToObject<Product>(args["product"]!!)
            var count = 0

            if (collection.isNotEmpty()) {
                var lastEntry = collectionManager.getCollection().lastEntry()
                while (lastEntry != null && lastEntry.value > product) {
                    collectionManager.remove(lastEntry.value,username)
                    count++
                    lastEntry = collectionManager.getCollection().lastEntry()
                }
            }
            val answer = Answer(AnswerType.OK, when (count) {
                0 -> { "Ни один продукт не был удален" }
                1 -> { "Только один продукт был удален" }
                else -> { "$count Столько продуктов было удалено" }
            },receiver = args["sender"]!!)
            connectionManager.send(answer, channel)

        } catch (e: Exception) {
            val answer = Answer(AnswerType.ERROR, "ОШИБКА ВЫПОЛНЕНИЯ КОМАНДЫ" ,receiver = args["sender"]!!)
            connectionManager.send(answer, channel)
        }
    }
    fun removeLower(args: Map<String, String>,channel: SocketChannel,username: String) {
        try {
            val collection = collectionManager.getCollection()
            val product = jsonCreator.stringToObject<Product>(args["product"]!!)
            var count = 0

            if (collection.isNotEmpty()) {
                var lastEntry = collectionManager.getCollection().lastEntry()
                while (lastEntry != null && lastEntry.value < product) {
                    collectionManager.remove(lastEntry.value,username)
                    count++
                    lastEntry = collectionManager.getCollection().lastEntry()
                }
            }
            val answer = Answer(AnswerType.OK, when (count) {
                0 -> { "Ни один продукт не был удален" }
                1 -> { "Только один продукт был удален" }
                else -> { "$count Столько продуктов было удалено" }
            },receiver = args["sender"]!!)
            connectionManager.send(answer, channel)

        } catch (e: Exception) {
            val answer = Answer(AnswerType.ERROR, "ОШИБКА ВЫПОЛНЕНИЯ КОМАНДЫ" ,receiver = args["sender"]!!)
            connectionManager.send(answer, channel)
        }
    }
}
