package app.test

import app.configureApplication
import app.serialization.JsonConfig
import io.ktor.http.*
import io.ktor.server.testing.*
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString

fun withApp(action: TestApplicationEngine.() -> Unit) {
    withApplication {
        application.configureApplication()
        action()
    }
}

inline fun <reified T: Any> TestApplicationRequest.jsonBody(value: T) {
    addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
    setBody(JsonConfig.json.encodeToString(value))
}

inline fun <reified T: Any> TestApplicationCall.responseBody() = response.content?.let { JsonConfig.json.decodeFromString<T>(it) }