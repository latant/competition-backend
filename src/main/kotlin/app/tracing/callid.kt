package app.tracing

import io.ktor.features.*
import io.ktor.http.*
import java.util.*

fun CallId.Configuration.configureCallIds() {
    header(HttpHeaders.XRequestId)
    generate { UUID.randomUUID().toString() }
    verify { it.isNotEmpty() }
}