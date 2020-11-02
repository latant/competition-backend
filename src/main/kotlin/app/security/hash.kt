package app.security

import org.mindrot.jbcrypt.BCrypt
import java.security.SecureRandom

private val secureRandom = SecureRandom()
private const val saltRounds = 10

fun String.hashed(): String = BCrypt.hashpw(this, BCrypt.gensalt(saltRounds, secureRandom))
fun String.hasHash(hashed: String) = BCrypt.checkpw(this, hashed)