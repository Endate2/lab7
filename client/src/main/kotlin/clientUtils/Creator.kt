package clientUtils

import basicClasses.*
import clientUtils.readers.*
import kotlinx.serialization.json.Json
import utils.InputManager
import utils.OutputManager
import java.time.LocalDateTime


class Creator(outputManager: OutputManager, inputManager: InputManager) {
    private val stringReader = StringReader(outputManager, inputManager)
    private val enumReader = EnumReader(outputManager, inputManager)
    private val longReader = LongReader(outputManager, inputManager)
    private val intReader = IntReader(outputManager, inputManager)
    private val doubleReader = DoubleReader(outputManager, inputManager)


    fun createProduct(): Product {
        val name = stringReader.read("Введите имя нового Product ")
        val coordinates = createCoordinates()
        val price = intReader.read("Введите значение Price (не может быть null): ", 0)
        var date: LocalDateTime = LocalDateTime.now()
        val unitOfMeasure = enumReader.read<UnitOfMeasure>("Введите UnitOfMeasure из списка: ", false)!!
        val manufacturer = createOrganization()

        return Product(name, coordinates, price, unitOfMeasure, manufacturer)
    }


    fun createOrganization() : Organization {
        val name:String = stringReader.read("Введите имя Organization: ")
        val employeesCount: Int = intReader.read("Введите значение EmployeesCount: ", 0)
        val organizationType = enumReader.read<OrganizationType>("Введите OrganizationType из списка: ", false)!!
        return Organization(name, employeesCount, organizationType)
    }


    fun createCoordinates() : Coordinates {
        val x: Long = longReader.read("Введите значение X: ",-771)
        val y: Double = doubleReader.read("Введите значение Y: ",770)

        return Coordinates(x, y)
    }

}
