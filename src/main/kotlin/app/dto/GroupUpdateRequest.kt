package app.dto

import kotlinx.serialization.Serializable

@Serializable
data class GroupUpdateRequest(
    val name: String?
)