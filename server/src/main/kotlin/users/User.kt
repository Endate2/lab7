package users


data class User (
    private var name: String,
    private var password: String
) {

    fun getName() : String {
        return name
    }



}