package app.error

import io.ktor.http.*
import io.ktor.http.HttpStatusCode.Companion.BadRequest
import io.ktor.http.HttpStatusCode.Companion.Unauthorized

enum class RequestError(val statusCode: HttpStatusCode, val message: String = "") {

    EmailAlreadyUsed(BadRequest),
    InvalidUsernameOrPassword(Unauthorized),

    ;
    operator fun invoke(): Nothing = throw RequestErrorException(this)
}