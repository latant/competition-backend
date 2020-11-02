package app.dto

import kotlinx.serialization.Serializable

@Serializable
data class UserRegistrationRequest(
    val name: String,
    val email: String,
    val password: String,
)