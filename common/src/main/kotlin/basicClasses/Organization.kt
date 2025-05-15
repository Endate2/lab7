package basicClasses

import exceptions.OrganizationName
import exceptions.EmployeesCountValueError
import kotlinx.serialization.Serializable
import java.sql.Timestamp

@Serializable
data class Organization(
    private val id: Int = Timestamp(System.currentTimeMillis()).time.toInt(),
    private var name: String, //Поле не может быть null, Строка не может быть пустой
    private var employeesCount: Int, //Значение поля должно быть больше 0, Максимальное значение поля: 1000
    private var type: OrganizationType //Поле может быть null
) {

    constructor(name: String, employeesCount: Int, type: OrganizationType) :
            this(Timestamp(System.currentTimeMillis()).time.toInt(), name, employeesCount, type)

    init {
        if (name.isBlank()) throw OrganizationName("Имя не может быть пустым")
        else if (employeesCount <= 0) throw EmployeesCountValueError("Количество работников должно быть больше 0")
    }
    fun getEmployeesCount(): Int = employeesCount
    fun getName() = name
    fun getType() = type

}