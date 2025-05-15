package users


data class User (
    private var name: String,
    private var password: String
) {

    fun getName() : String {
        return name
    }

    fun setName(name: String) {
        this.name = name
    }

    fun getPassword() : String {
        return password
    }

    fun setPassword(password: String) {
        this.password = password
    }

}