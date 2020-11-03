package app.dto

import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.time.ZonedDateTime

@Serializable
sealed class CompetitionCreationRequest {

    @Serializable
    data class Participant(val name: String, val description: String = "")

    @Serializable
    @SerialName("League")
    data class League(
        val name: String,
        @Contextual
        val dateTime: ZonedDateTime,
        val displayColor: String = "#0000ff",
        val description: String = "",
        val participants: List<Participant>,
        val roundCount: Int? = null,
    ) : CompetitionCreationRequest()

    @Serializable
    @SerialName("Cup")
    data class Cup(
        val name: String,
        @Contextual
        val dateTime: ZonedDateTime,
        val displayColor: String = "#0000ff",
        val description: String = "",
        val participants: List<Participant>
    ) : CompetitionCreationRequest()

    @Serializable
    @SerialName("Tournament")
    data class Tournament(
        val name: String,
        @Contextual
        val dateTime: ZonedDateTime,
        val displayColor: String = "#0000ff",
        val description: String = "",
        val participants: List<Participant>,
        val groupCount: Int,
        val playOffParticipantCount: Int,
    ) : CompetitionCreationRequest()

}