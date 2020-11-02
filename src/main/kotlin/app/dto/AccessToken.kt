package app.dto

import kotlinx.serialization.Serializable

@Serializable
data class AccessToken(
    val accessToken: String
)