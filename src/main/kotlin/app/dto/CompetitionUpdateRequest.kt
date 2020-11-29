package app.dto

import app.validation.requireNotBlank
import app.validation.requireValidCssHexColor
import app.validation.validations
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.time.LocalDateTime

@Serializable
class CompetitionUpdateRequest(
    val name: String? = null,
    val description: String? = null,
    val logo: String? = null,
    val startDateTime: @Contextual LocalDateTime? = null,
    val endDateTime: @Contextual LocalDateTime? = null,
    val displayColor: String? = null,
    val styleSheet: String? = null,
) {

    fun validate() = validations {
        name?.requireNotBlank { "Name must not be blank" }
        displayColor?.requireValidCssHexColor { "The display color must be a valid css color in hex format" }
    }
}