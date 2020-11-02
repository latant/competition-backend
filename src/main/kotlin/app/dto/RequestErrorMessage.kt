package app.dto

import kotlinx.serialization.Serializable

@Serializable
data class RequestErrorMessage(
    val id: String,
    val message: String
)