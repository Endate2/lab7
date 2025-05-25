package basicClasses

import kotlinx.serialization.Serializable



@Serializable
data class Coordinates (
    private var x: Long, //Поле не может быть null
    private var y: Double
    ){
    fun getX(): Long = x
    fun getY(): Double = y
}
