package app.dto

import app.validation.*
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.time.ZonedDateTime

@Serializable
class CompetitionUpdateRequest(
    val name: String? = null,
    val description: String? = null,
    val logo: String? = null,
    val dateTime: @Contextual ZonedDateTime? = null,
    val displayColor: String? = null,
    val styleSheet: String? = null,
) {

    fun validate() = validations {
        name?.requireNotBlank { "Name must not be blank" }
        displayColor?.requireValidCssHexColor { "The display color must be a valid css color in hex format" }
    }
}