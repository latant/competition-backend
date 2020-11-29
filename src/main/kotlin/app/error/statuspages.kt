package app.error

import app.dto.RequestErrorResponse
import io.ktor.application.*
import io.ktor.features.*
import io.ktor.response.*
import io.ktor.util.pipeline.*
import kotlinx.serialization.SerializationException

fun StatusPages.Configuration.configureStatusPages() {

    exception<RequestErrorException> {
        handle(it)
    }

    exception<SerializationException> {
        handle(RequestErrorException(RequestError.InvalidRequestBody, it.message))
    }
}

suspend fun PipelineContext<Unit, ApplicationCall>.handle(ex: RequestErrorException) {
    RequestError.logger.info("{}: {}", ex.requestError, ex.message)
    call.respond(ex.requestError.statusCode, RequestErrorResponse(ex.requestError.name, ex.requestError.message))
}