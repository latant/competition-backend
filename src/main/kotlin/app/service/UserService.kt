package app.service

import app.dao.CompetitionGraph
import app.dto.AccessTokenResponse
import app.dto.UserLoginRequest
import app.dto.UserRegistrationRequest
import app.error.RequestError
import app.model.User
import app.security.hasHash
import app.security.hashed
import app.security.jwtToken
import app.security.principal
import org.neo4j.ogm.cypher.ComparisonOperator.EQUALS
import org.neo4j.ogm.cypher.Filter
import org.neo4j.ogm.session.loadAll

object UserService {

    fun registerUser(userRegistration: UserRegistrationRequest) {
        userRegistration.run {
            CompetitionGraph.readWriteTransaction {
                if (loadAll<User>(Filter(User::email.name, EQUALS, email)).isNotEmpty()) {
                    RequestError.EmailAlreadyUsed()
                }
                save(User(name, email, password.hashed()))
            }
        }
    }

    fun getAccessToken(userLogin: UserLoginRequest): AccessTokenResponse {
        userLogin.run {
            CompetitionGraph.session {
                val user = loadAll<User>(Filter(User::email.name, EQUALS, email)).firstOrNull()
                    ?: RequestError.InvalidUsernameOrPassword()
                if (password.hasHash(user.password)) {
                    return AccessTokenResponse(user.principal().jwtToken())
                } else {
                    RequestError.InvalidUsernameOrPassword()
                }
            }
        }
    }

}