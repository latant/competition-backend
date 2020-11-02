package app

import app.error.configureStatusPages
import app.security.configureJwt
import app.serialization.JsonConfig
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.auth.jwt.*
import io.ktor.features.*
import io.ktor.routing.*
import io.ktor.serialization.*

fun Application.configureApplication() {

    install(StatusPages) { configureStatusPages() }
    install(DefaultHeaders)
    install(CallLogging)
    install(ContentNegotiation) { json(JsonConfig.json) }
    install(Authentication) { jwt { configureJwt() } }
    routing { configureRoutes() }

}