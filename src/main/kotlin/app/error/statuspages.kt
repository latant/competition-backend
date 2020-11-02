package app.error

import app.dto.RequestErrorResponse
import io.ktor.application.*
import io.ktor.features.*
import io.ktor.http.*
import io.ktor.response.*
import kotlinx.serialization.SerializationException

fun StatusPages.Configuration.configureStatusPages() {

    exception<RequestErrorException> {
        call.respond(it.requestError.statusCode, RequestErrorResponse(it.requestError.name, it.requestError.message))
    }

    exception<SerializationException> {
        call.respond(HttpStatusCode.BadRequest, RequestErrorResponse("InvalidRequestBody", it.message ?: ""))
    }
}