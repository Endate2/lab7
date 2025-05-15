package serverUtils

import basicClasses.Product
import utils.JsonWorker
import java.sql.DriverManager

class DBManager(
    private val dbUrl: String,
    private val dbUser: String,
    private val dbPassword: String
) {
    init {
        // Регистрация драйвера PostgreSQL
        Class.forName("org.postgresql.Driver")
    }
    private val jsonCreator = JsonWorker()

    private fun initUsers() {
        val connection = DriverManager.getConnection(dbUrl, dbUser, dbPassword)
        val statement = connection.createStatement()
        statement.executeUpdate("create table if not exists users(login character varying(50) primary key,password character varying(500),salt character varying(100));")
        statement.close()
        connection.close()
    }

    private fun initCollection() {
        val connection = DriverManager.getConnection(dbUrl, dbUser, dbPassword)
        val statement = connection.createStatement()
        statement.executeUpdate("create table if not exists collection(id serial primary key,info character varying(1000) not null, user_login character varying(50) references users (login) ON DELETE SET NULL ON UPDATE CASCADE);")
        statement.close()
        connection.close()
    }

    fun initDB() {
        initUsers()
        initCollection()
    }

    fun userExists(login: String) : Boolean {
        val connection = DriverManager.getConnection(dbUrl, dbUser, dbPassword)
        val statement = connection.prepareStatement("SELECT * FROM users WHERE login = ?")
        statement.setString(1, login)
        val resultSet = statement.executeQuery()
        val result = resultSet.next()
        resultSet.close()
        statement.close()
        connection.close()
        return result
    }

    fun retrieveSalt(login: String) : String {
        val connection = DriverManager.getConnection(dbUrl, dbUser, dbPassword)
        val statement = connection.prepareStatement("SELECT salt FROM users WHERE login = ?")
        statement.setString(1, login)
        val resultSet = statement.executeQuery()
        resultSet.next()
        return resultSet.getString("salt")
    }

    fun registerUser(login: String, password: String, salt: String) : Boolean {
        val connection = DriverManager.getConnection(dbUrl, dbUser, dbPassword)
        val statement = connection.prepareStatement("SELECT * FROM users WHERE login = ?")
        statement.setString(1, login)
        val resultSet = statement.executeQuery()
        val result = resultSet.next()
        if (!result) {
            val st = connection.prepareStatement("INSERT INTO users (login, password, salt) VALUES (?, ?, ?)")
            st.setString(1, login)
            st.setString(2, password)
            st.setString(3, salt)
            st.executeUpdate()
        }
        resultSet.close()
        statement.close()
        connection.close()
        return !result
    }

    fun loginUser(login: String, password: String) : Boolean {
        val connection = DriverManager.getConnection(dbUrl, dbUser, dbPassword)
        val statement = connection.prepareStatement("SELECT * FROM users WHERE login = ? AND password = ?")
        statement.setString(1, login)
        statement.setString(2, password)
        val resultSet = statement.executeQuery()
        val result = resultSet.next()
        resultSet.close()
        statement.close()
        connection.close()
        return result
    }

    fun changePassword(login: String, oldPassword: String, newPassword: String) {
        val connection = DriverManager.getConnection(dbUrl, dbUser, dbPassword)
        val statement = connection.createStatement()
        val resultSet = statement.executeQuery("SELECT * FROM users WHERE login = '$login' AND password = '$oldPassword'")
        val result = resultSet.next()
        if (result) {
            statement.executeUpdate("UPDATE users SET password = '$newPassword' WHERE login = '$login'")
        }
        resultSet.close()
        statement.close()
        connection.close()
    }

    fun changeUsername(oldLogin: String, newLogin: String, password: String) {
        val connection = DriverManager.getConnection(dbUrl, dbUser, dbPassword)
        val statement = connection.createStatement()
        val resultSet = statement.executeQuery("SELECT * FROM users WHERE login = '$oldLogin' AND password = '$password'")
        val result = resultSet.next()
        if (result) {
            statement.executeUpdate("UPDATE users SET login = '$newLogin' WHERE login = '$oldLogin'")
        }
        resultSet.close()
        statement.close()
        connection.close()
    }

    fun deleteUser(login: String, password: String) {
        val connection = DriverManager.getConnection(dbUrl, dbUser, dbPassword)
        val statement = connection.createStatement()
        val resultSet = statement.executeQuery("SELECT * FROM users WHERE login = '$login' AND password = '$password'")
        val result = resultSet.next()
        if (result) {
            statement.executeUpdate("DELETE FROM users WHERE login = '$login'")
        }
        resultSet.close()
        statement.close()
        connection.close()
    }

    fun deleteProduct(id: Int) {
        val connection = DriverManager.getConnection(dbUrl, dbUser, dbPassword)
        val statement = connection.prepareStatement("DELETE FROM collection WHERE id = ?")
        statement.setInt(1, id)
        statement.executeUpdate()
        statement.close()
        connection.close()
    }


    fun saveProduct(product: Product, username: String) {
        val connection = DriverManager.getConnection(dbUrl, dbUser, dbPassword)
        var statement = connection.prepareStatement("SELECT * FROM collection WHERE ID = ?")
        val id = product.getId()
        statement.setInt(1, id)
        val resultSet = statement.executeQuery()
        val result = resultSet.next()
        if (result) {
            statement = connection.prepareStatement("UPDATE collection SET INFO = ? WHERE ID = ?")
            statement.setString(1, jsonCreator.objectToString(product))
            statement.setInt(2, product.getId())
            statement.executeUpdate()
        } else {
            statement = connection.prepareStatement("INSERT INTO collection (INFO, USER_LOGIN) VALUES (?, ?)")
            statement.setString(1, jsonCreator.objectToString(product))
            statement.setString(2, username)
            statement.executeUpdate()
        }
        resultSet.close()
        statement.close()
        connection.close()
    }


    fun loadCollection() : Map<String, String> {
        val connection = DriverManager.getConnection(dbUrl, dbUser, dbPassword)
        val statement = connection.createStatement()
        val resultSet = statement.executeQuery("SELECT * FROM collection")
        val result = mutableMapOf<String, String>()
        while (resultSet.next()) {
            result[resultSet.getString("INFO")] = resultSet.getString("USER_LOGIN")
        }
        resultSet.close()
        statement.close()
        connection.close()
        return result
    }


    private fun loadPasswordByUsername(username: String) : String {
        val connection = DriverManager.getConnection(dbUrl, dbUser, dbPassword)
        val statement = connection.createStatement()
        val resultSet = statement.executeQuery("SELECT * FROM users where login = '$username'")
        resultSet.next()
        return resultSet.getString("password")
    }

    fun getIdByProduct(product: Product) : Int {
        val connection = DriverManager.getConnection(dbUrl, dbUser, dbPassword)
        val statement = connection.createStatement()
        val resultSet = statement.executeQuery("SELECT * FROM collection WHERE INFO = '${jsonCreator.objectToString(product)}'")
        resultSet.next()
        return resultSet.getInt("id")
    }

}