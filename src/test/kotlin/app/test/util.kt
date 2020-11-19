package app.test

import app.configureApplication
import app.serialization.JsonConfig
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.server.testing.*
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import java.net.URLEncoder
import java.util.*

fun <T> withApp(action: TestApplicationEngine.() -> T): T = withApplication {
    application.configureApplication()
    action()
}

inline fun <reified T: Any> TestApplicationRequest.jsonBody(value: T) {
    addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
    setBody(JsonConfig.json.encodeToString(value))
}

inline fun <reified T: Any> TestApplicationResponse.body() = content?.let { JsonConfig.json.decodeFromString<T>(it) }

fun String.urlEncoded(): String = URLEncoder.encode(this, "UTF-8")