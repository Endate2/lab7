package tokenutils

import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.security.Keys
import java.util.*

class JWTManager {
    private val key = Keys.secretKeyFor(SignatureAlgorithm.HS256)

    fun createToken(username: String): String {
        return Jwts.builder()
            .setSubject(username) // задаем имя
            .setIssuedAt(Date()) //текущую дату/время создания
            .setExpiration(Date(System.currentTimeMillis() + 86_400_000)) //дату/время истечения (текущее время + 24 часа)
            .signWith(key) // алгоритм шифрования
            .compact() // Компактифицирует в строку формата JWT
    }


    fun validateToken(token: String): Boolean {
        return try {
            Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun getUsername(token: String): String? {
        return try {
            Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .body
                .subject
        } catch (e: Exception) {
            null
        }
    }
}