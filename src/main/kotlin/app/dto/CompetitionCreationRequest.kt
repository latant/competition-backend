package app.dto

import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.time.ZonedDateTime

@Serializable
sealed class CompetitionCreationRequest {

    @Serializable
    data class Competitor(val name: String, val description: String = "")

    @Serializable
    @SerialName("League")
    data class League(
        val name: String,
        val dateTime: @Contextual ZonedDateTime,
        val displayColor: String = "#0000ff",
        val description: String = "",
        val competitors: List<Competitor>,
        val roundCount: Int? = null,
    ) : CompetitionCreationRequest()

    @Serializable
    @SerialName("Cup")
    data class Cup(
        val name: String,
        val dateTime: @Contextual ZonedDateTime,
        val displayColor: String = "#0000ff",
        val description: String = "",
        val competitors: List<Competitor>
    ) : CompetitionCreationRequest()

    @Serializable
    @SerialName("Tournament")
    data class Tournament(
        val name: String,
        val dateTime: @Contextual ZonedDateTime,
        val displayColor: String = "#0000ff",
        val description: String = "",
        val competitors: List<Competitor>,
        val groupCount: Int,
        val playOffParticipantCount: Int,
    ) : CompetitionCreationRequest()

}