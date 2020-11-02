package app.service

import app.dao.CompetitionGraph
import app.dto.AccessToken
import app.dto.UserLogin
import app.dto.UserRegistration
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

    fun registerUser(userRegistration: UserRegistration) {
        userRegistration.run {
            CompetitionGraph.readWriteTransaction {
                if (loadAll<User>(Filter(User::email.name, EQUALS, email)).isNotEmpty()) {
                    RequestError.EmailAlreadyUsed()
                }
                save(User(name, email, password.hashed()))
            }
        }
    }

    fun getAccessToken(userLogin: UserLogin): AccessToken {
        userLogin.run {
            CompetitionGraph.session {
                val user = loadAll<User>(Filter(User::email.name, EQUALS, email)).firstOrNull()
                    ?: RequestError.InvalidUsernameOrPassword()
                if (password.hasHash(user.password)) {
                    return AccessToken(user.principal().jwtToken())
                } else {
                    RequestError.InvalidUsernameOrPassword()
                }
            }
        }
    }

}