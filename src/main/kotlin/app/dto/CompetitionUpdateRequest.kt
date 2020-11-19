package app.dto

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
)