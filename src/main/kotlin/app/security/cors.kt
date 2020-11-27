package app.security

import io.ktor.features.*
import io.ktor.http.HttpHeaders.Authorization
import io.ktor.http.HttpMethod.Companion.Delete
import io.ktor.http.HttpMethod.Companion.Get
import io.ktor.http.HttpMethod.Companion.Head
import io.ktor.http.HttpMethod.Companion.Options
import io.ktor.http.HttpMethod.Companion.Patch
import io.ktor.http.HttpMethod.Companion.Post
import io.ktor.http.HttpMethod.Companion.Put

fun CORS.Configuration.configureCORS() {
    anyHost()
    allowCredentials = true
    headers += setOf(Authorization)
    methods += setOf(Get, Put, Patch, Post, Delete, Head, Options)
}