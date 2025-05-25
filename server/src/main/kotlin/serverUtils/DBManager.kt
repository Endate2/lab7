package serverUtils

import basicClasses.Coordinates
import basicClasses.Organization
import basicClasses.OrganizationType
import basicClasses.Product
import basicClasses.UnitOfMeasure
import utils.JsonWorker
import java.sql.DriverManager
import java.sql.PreparedStatement
import java.sql.Types
import collection.CollectionManager

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
        statement.executeUpdate("CREATE TABLE IF NOT EXISTS collection (\n" +
                "    id SERIAL PRIMARY KEY,\n" +
                "    name VARCHAR(100) NOT NULL,\n" +
                "    coordinate_x INTEGER NOT NULL,\n" +
                "    coordinate_y DOUBLE PRECISION NOT NULL,\n" +
                "    creation_date DATE NOT NULL,\n" +
                "    price INTEGER CHECK (price > 0),\n" +
                "    unit_of_measure VARCHAR(20),\n" +
                "    manufacturer_name VARCHAR(100),\n" +
                "    manufacturer_employees_count INTEGER,\n" +
                "    manufacturer_type VARCHAR(30),\n" +
                "    user_login VARCHAR(50) REFERENCES users(login) ON DELETE SET NULL ON UPDATE CASCADE\n" +
                ");")
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
        val id = product.getId()

        // Проверяем, существует ли продукт с таким ID
        val checkStatement = connection.prepareStatement("SELECT id FROM collection WHERE id = ?")
        checkStatement.setInt(1, id)
        val exists = checkStatement.executeQuery().next()
        checkStatement.close()

        if (exists) {
            // Обновляем существующий продукт
            val updateStatement = connection.prepareStatement("""
            UPDATE collection SET
                name = ?, 
                coordinate_x = ?, 
                coordinate_y = ?, 
                creation_date = ?, 
                price = ?, 
                unit_of_measure = ?::unitofmeasure, 
                manufacturer_name = ?, 
                manufacturer_employees_count = ?, 
                manufacturer_type = ?::organizationtype, 
                user_login = ?
            WHERE id = ?
        """)

            setProductParameters(updateStatement, product, username)
            updateStatement.setDate(4, java.sql.Date.valueOf(product.getCreationDate()))
            updateStatement.setInt(11, id) // WHERE id = ?

            updateStatement.executeUpdate()
            updateStatement.close()
        } else {
            // Вставка нового продукта
            val insertStatement = connection.prepareStatement("""
            INSERT INTO collection (
                name, coordinate_x, coordinate_y, creation_date, 
                price, unit_of_measure, manufacturer_name, 
                manufacturer_employees_count, manufacturer_type, user_login
            ) VALUES (?, ?, ?, ?, ?, ?::unitofmeasure, ?, ?, ?::organizationtype, ?)
            RETURNING id
        """)

            setProductParameters(insertStatement, product, username)
            insertStatement.setDate(4, java.sql.Date.valueOf(product.getCreationDate()))

            val rs = insertStatement.executeQuery()
            if (rs.next()) {
                product.setId(rs.getInt(1))
            }
            insertStatement.close()
        }
        connection.close()
    }

    private fun setProductParameters(statement: PreparedStatement, product: Product, username: String) {
        statement.setString(1, product.getName())
        statement.setLong(2, product.getCoordinates().getX())
        statement.setDouble(3, product.getCoordinates().getY())
        statement.setInt(5, product.getPrice())
        statement.setString(6, product.getUnitOfMeasure().name)

        val manufacturer = product.getManufacturer()
        if (manufacturer != null) {
            statement.setString(7, manufacturer.getName())
            statement.setInt(8, manufacturer.getEmployeesCount())
            statement.setString(9, manufacturer.getType().name)
        } else {
            statement.setNull(7, Types.VARCHAR)
            statement.setNull(8, Types.INTEGER)
            statement.setNull(9, Types.VARCHAR)
        }

        statement.setString(10, username)
    }


    fun loadCollection(): List<Pair<Product, String>> {
        val connection = DriverManager.getConnection(dbUrl, dbUser, dbPassword)
        val statement = connection.createStatement()
        val resultSet = statement.executeQuery("""
        SELECT id, name, coordinate_x, coordinate_y, creation_date, 
               price, unit_of_measure, manufacturer_name, 
               manufacturer_employees_count, manufacturer_type, user_login 
        FROM collection
    """)

        val result = mutableListOf<Pair<Product, String>>()

        while (resultSet.next()) {
            try {
                val product = Product(
                    resultSet.getInt("id"),
                    resultSet.getString("name"),
                    Coordinates(
                        resultSet.getLong("coordinate_x"),
                        resultSet.getDouble("coordinate_y")
                    ),
                    resultSet.getDate("creation_date").toLocalDate(),
                    resultSet.getInt("price"),
                    UnitOfMeasure.valueOf(resultSet.getString("unit_of_measure")),
                    if (resultSet.getString("manufacturer_name") != null) Organization(
                        resultSet.getString("manufacturer_name"),
                        resultSet.getInt("manufacturer_employees_count"),
                        OrganizationType.valueOf(resultSet.getString("manufacturer_type"))
                    ) else null
                )
                result.add(product to resultSet.getString("user_login"))
            } catch (e: Exception) {

            }
        }

        resultSet.close()
        statement.close()
        connection.close()
        return result
    }


}