package basicClasses

import serializers.LocalDateSerializer
import exceptions.ProductPriceLowerThanZero
import exceptions.ProductIdLowerThanZero
import exceptions.ProductNameIsNullOrBlank
import kotlinx.serialization.Serializable
import java.sql.Timestamp
import java.time.LocalDate
import kotlin.math.absoluteValue
import kotlin.random.Random


@Suppress("BooleanMethodIsAlwaysInverted")
@Serializable
data class Product (

    private var id: Int = (Timestamp(System.currentTimeMillis()).time % Int.MAX_VALUE).toInt().absoluteValue + 1,//Значение поля должно быть больше 0, Значение этого поля должно быть уникальным, Значение этого поля должно генерироваться автоматически
    private var name: String, //Поле не может быть null, Строка не может быть пустой
    private var coordinates: Coordinates, //Поле не может быть null

    @Serializable(with = LocalDateSerializer::class)
    private val creationDate: LocalDate = LocalDate.now(), //Поле не может быть null, Значение этого поля должно генерироваться автоматически
    private var price: Int, //Поле может быть null, Значение поля должно быть больше 0
    private var unitOfMeasure: UnitOfMeasure, //Поле может быть null
    private var manufacturer: Organization? //Поле может быть null

) : Comparable<Product>{

    constructor (
        name: String,
        coordinates: Coordinates,
        price: Int,
        unitOfMeasure: UnitOfMeasure,
        manufacturer: Organization?
    ) : this((Timestamp(System.currentTimeMillis()).time % Int.MAX_VALUE).toInt().absoluteValue + 1, name, coordinates, LocalDate.now(), price, unitOfMeasure, manufacturer)


    constructor() : this("Noname", Coordinates(0, 0.0), 1, UnitOfMeasure.METERS, Organization("a",1,OrganizationType.COMMERCIAL))

    init {
        if (name.isBlank()) throw ProductNameIsNullOrBlank("Имя не может быть пустым")
        else if (price != null) {
            if (price!! <= 0) throw ProductPriceLowerThanZero("Цена должна быть больше 0")
        }
        if (id <= 0) throw ProductIdLowerThanZero("id должно быть больше 0")
    }


    override fun compareTo(other: Product): Int {
        return this.getId().compareTo(other.getId())
    }
    override fun toString(): String {
        return "Product(\n" +
                "  id=$id,\n" +
                "  name=$name,\n" +
                "  coordinates=$coordinates,\n" +
                "  creationDate=$creationDate,\n" +
                "  price=$price,\n" +
                "  unitOfMeasure=$unitOfMeasure,\n" +
                "  manufacturer=$manufacturer\n" +
                ")"
    }


    fun getId(): Int {
        return id
    }
    private fun generateId(): Int {
        return (System.nanoTime() % Int.MAX_VALUE).toInt() + Random.nextInt(1000)
    }

    fun getCreationDate(): LocalDate {
        return creationDate
    }

    fun getName(): String {
        return name
    }


    fun getCoordinates(): Coordinates {
        return coordinates
    }


    fun getPrice(): Int {
        return price
    }


    fun getUnitOfMeasure(): UnitOfMeasure {
        return unitOfMeasure
    }



    fun getChapter(): Organization? {
        return manufacturer
    }
    fun getManufacturer(): Organization? {
        return manufacturer
    }

    fun setName(string: String) {
        if (string.isNotEmpty()){
            this.name = string
        }
    }
    fun setCoordinates(coordinates: Coordinates) {
        this.coordinates = coordinates
    }

    fun setPrice(price: Int) {
        this.price = price
    }
    fun setId(id: Int) {
        this.id = id
    }



    fun setUnitOfMeasure(unitOfMeasure: UnitOfMeasure) {
        this.unitOfMeasure = unitOfMeasure
    }



    fun setOrganization(manufacturer: Organization?) {
        this.manufacturer = manufacturer
    }
}