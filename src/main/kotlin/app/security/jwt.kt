package app.security

import app.dao.CompetitionGraph
import app.model.User
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.auth.*
import io.ktor.auth.jwt.*
import org.neo4j.ogm.session.load
import java.util.*

private val secret: String = System.getenv("JWT_SECRET") ?: "sfdsafsadfdsa32r432f"
private const val issuer = "competition-service"
private const val validityMs = 36_000_00 * 10
private val algorithm = Algorithm.HMAC512(secret)

fun JWTAuthenticationProvider.Configuration.configureJwt() {
    verifier(JWT.require(Algorithm.HMAC512(secret)).withIssuer(issuer).build())
    realm = issuer
    validate {
        CompetitionGraph.readOnlyTransaction {
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