package app.security

import app.dao.CompetitionGraph
import app.dao.load
import app.model.User
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.auth.*
import io.ktor.auth.jwt.*
import java.util.*

private const val secret = "zAP5MBA4B4Ijz0MZaS48"
private const val issuer = "competition-service"
private const val validityMs = 36_000_00 * 10
private val algorithm = Algorithm.HMAC512(secret)

fun JWTAuthenticationProvider.Configuration.configureJwt() {
    verifier(JWT.require(Algorithm.HMAC512(secret)).withIssuer(issuer).build())
    realm = issuer
    validate {
        CompetitionGraph.session {
            load<User>(it.payload.getClaim("id").asLong(), 0)?.id?.let(::UserPrincipal)
        }
    }
}

fun UserPrincipal.jwtToken(): String = JWT.create()
    .withSubject("Authentication")
    .withIssuer(issuer)
    .withClaim("id", id)
    .withExpiresAt(Date(System.currentTimeMillis() + validityMs))
    .sign(algorithm)

fun User.principal() = UserPrincipal(id!!)

data class UserPrincipal(val id: Long) : Principal