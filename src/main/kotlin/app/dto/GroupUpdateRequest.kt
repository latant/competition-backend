package app.dto

import app.validation.requireNotBlank
import kotlinx.serialization.Serializable

@Serializable
data class GroupUpdateRequest(
    val name: String?
) {
    fun validate() {
        name?.requireNotBlank { "Name must not be blank" }
    }
}