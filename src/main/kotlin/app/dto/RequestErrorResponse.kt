package app.dto

import kotlinx.serialization.Serializable

@Serializable
data class RequestErrorResponse(
    val id: String,
    val message: String
)