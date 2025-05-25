package users

import org.apache.logging.log4j.LogManager
import serverUtils.DBManager
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.concurrent.locks.ReentrantLock
import kotlin.experimental.and

class UserManager(private val dbManager: DBManager) {

    private val lock = ReentrantLock()
    private val logger = LogManager.getLogger(UserManager::class.java)


    private fun hashing(stringToHash: String, salt: String): String {
        var hashedString = ""
        try {
            val md = MessageDigest.getInstance("SHA-224")
            md.update(salt.toByteArray())
            val bytes = md.digest(stringToHash.toByteArray())
            md.reset()
            val sb = StringBuilder()
            for (element in bytes) {
                sb.append(((element and 0xff.toByte()) + 0x100).toString(16).substring(1))
            }
            hashedString = sb.toString()
        } catch (e: Exception) {
            logger.error(e.message)
        }
        return hashedString
    }

    private fun createSalt(): String {
        val random = SecureRandom()
        val bytes = ByteArray(16)
        random.nextBytes(bytes)
        val sb = StringBuilder()
        for (element in bytes) {
            sb.append(((element and 0xff.toByte()) + 0x100).toString(16).substring(1))
        }
        return sb.toString()
    }

    fun login(username: String, password: String): Boolean {
        val salt = dbManager.retrieveSalt(username)
        val hashedPassword = hashing(password, salt)
        val authorized = dbManager.loginUser(username, hashedPassword)
        if (authorized) {
            logger.debug("Пользователь $username авторизован")
        } else {
            logger.debug("Авторизация не пройдена, $username")
        }
        return authorized
    }

    fun register(username: String, password: String): Boolean {
        val salt = createSalt()
        val registered = dbManager.registerUser(username, hashing(password, salt), salt)
        if (registered) {
            logger.debug("Пользователь $username зарегистрирован")
        } else {
            logger.debug("Регистрация не пройдена, $username")
        }
        return registered
    }

    fun userExists(username: String): Boolean {
        return dbManager.userExists(username)
    }
}