package app.error

import io.ktor.http.*
import io.ktor.http.HttpStatusCode.Companion.BadRequest
import io.ktor.http.HttpStatusCode.Companion.NotFound
import io.ktor.http.HttpStatusCode.Companion.Unauthorized

enum class RequestError(val statusCode: HttpStatusCode, val message: String = "") {

    EmailAlreadyUsed(BadRequest),
    InvalidUsernameOrPassword(Unauthorized),
    CompetitionNotFound(NotFound),
    MatchNotFound(NotFound),

    ;

    operator fun invoke(): Nothing = throw RequestErrorException(this)
}