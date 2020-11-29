package app.error

import app.dto.RequestErrorResponse
import io.ktor.application.*
import io.ktor.features.*
import io.ktor.response.*
import kotlinx.serialization.SerializationException

fun StatusPages.Configuration.configureStatusPages() {

    exception<RequestErrorException> {
        RequestError.logger.info("{}: {}", it.requestError, it.message)
        call.respond(it.requestError.statusCode, RequestErrorResponse(it.requestError.name, it.requestError.message))
    }

    exception<SerializationException> {
        RequestError.InvalidRequestBody(it.message)
    }
}