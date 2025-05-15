package serverUtils

import basicClasses.Product
import collection.CollectionManager
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import users.UserManager
import utils.JsonWorker


class FileManager(
    private val dbManager: DBManager
) {
    private val jsonCreator = JsonWorker()
    private val logger: Logger = LogManager.getLogger(FileManager::class.java)

    fun load(collectionManager: CollectionManager) {
        try {
            logger.info("Загрузка из базы данных")
            val collection = dbManager.loadCollection()

            for (element in collection) {
                val product = jsonCreator.stringToObject<Product>(element.key)
                val parent = element.value
                try {
                    collectionManager.add(product, parent)
                    logger.info("Загружен $product")
                } catch (e:Exception) {
                    logger.warn(e.message.toString())
                }

            }
            logger.info("Загружено ${collectionManager.getCollection().size} элементов успешно")

        } catch (e: Exception) {
            logger.warn(e.message.toString())
        }
    }

    fun save(collectionManager: CollectionManager, userManager: UserManager) {
        try {
            logger.info("Сохранение в базу данных")

            for (element in collectionManager.getCollection()) {
                val relation = collectionManager.getRelationship()
                val username = relation[element.value.getId()]!!
                dbManager.saveProduct(element.value, username)
            }

            logger.info("Сохранено ${collectionManager.getCollection().size} успешно элементов")

        } catch (e: Exception) {
            logger.warn(e)
        }
    }

}