package app.security

import io.ktor.features.*

fun CORS.Configuration.configureCORS() {
    anyHost()
    allowNonSimpleContentTypes = true
    allowCredentials = true
}