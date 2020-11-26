package app

import app.error.configureStatusPages
import app.security.configureCORS
import app.security.configureJwt
import app.serialization.JsonConfig
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.auth.jwt.*
import io.ktor.features.*
import io.ktor.routing.*
import io.ktor.serialization.*
import io.ktor.websocket.*

fun Application.configureApplication() {

    install(WebSockets)
    install(StatusPages) { configureStatusPages() }
    install(DefaultHeaders)
    install(CallLogging)
    install(ContentNegotiation) { json(JsonConfig.json) }
    install(Authentication) { jwt { configureJwt() } }
    install(CORS) { configureCORS() }

    routing { configureRoutes() }

}