package collection

import basicClasses.Product
import exceptions.ProductIdAlreadyExists
import java.util.*
import java.util.concurrent.locks.ReentrantLock
import java.util.function.Predicate
import serverUtils.DBManager
import exceptions.NotAuthorized
import kotlin.toString


class CollectionManager(private val dbManager: DBManager) {
    private val collection = TreeMap<Int,Product>()
    private val date: Date = Date()
    private val relationship = Collections.synchronizedMap(mutableMapOf<Int, String>())
    private val lock = ReentrantLock()

    fun getCollection(): TreeMap<Int, Product> {
        return collection
    }

    fun getRelationship(): MutableMap<Int, String> {
        return relationship
    }

    fun add(element: Product, username: String) {
        if (getByID(element.getId()) != null) throw ProductIdAlreadyExists("Продукт с id ${element.getId()} уже существует")
        lock.lock()
        try {
            dbManager.saveProduct(element, username)
            collection[element.getId()] = element
            relationship[element.getId()] = username
        } finally {
            lock.unlock()
        }
    }
    fun show(): MutableList<String> {
        return if (collection.isEmpty()) {
            mutableListOf("Коллекция пуста")
        } else {
            val output = mutableListOf<String>()
            lock.lock()
            try {
                for (product in collection) {
                    output.add(product.toString())
                }
            } finally {
                lock.unlock()
            }
            output
        }
    }

    fun getInfo() : String {
        return "Информация о коллекции: размер=${collection.size}"
    }
    fun remove(product: Product, username: String) {
        if ((relationship[product.getId()] != username) and (username != "admin")) throw NotAuthorized("У вас нет разрешения на удаление этого элемента")
        lock.lock()
        try {
            dbManager.deleteProduct(product.getId())
            relationship.remove(product.getId())
            collection.remove(product.getId())
        } finally {
            lock.unlock()
        }
    }
    fun getByID(id: Int) : Product? {
        lock.lock()
        try {
            for ((_, product) in collection) {
                if (product.getId() == id) {
                    return product
                }
            }
        } finally {
            lock.unlock()
        }
        return null
    }
    fun update(data: Product, product: Product, username: String) {
        if ((relationship[product.getId()] != username) and (username != "admin")) throw NotAuthorized("У вас нет разрешения на удаление этого элемента")

        lock.lock()
        try {
            product.setName(data.getName())
            product.setCoordinates(data.getCoordinates())
            product.setPrice(data.getPrice())
            product.setUnitOfMeasure(data.getUnitOfMeasure())
            dbManager.saveProduct(product, username)
        } finally {
            lock.unlock()
        }
    }
    fun clear(username: String) {
        val toBeCleared = mutableListOf<Product>()
        lock.lock()
        for (product in collection) {
            if ((relationship[product.value.getId()] == username) or (username == "admin")) {
                toBeCleared.add(product.value)
            }
        }
        lock.unlock()
        for (product in toBeCleared) {
            try {
                remove(product, username)
            } catch (_:Exception) {}

        }

    }
    fun filter(predicate: Predicate<Product>): List<Product> {
        lock.lock()
        try {
            return collection.values.filter { predicate.test(it) }
        } finally {
            lock.unlock()
        }
    }

}


