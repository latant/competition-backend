package app.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed class CompetitionResponse {

    @Serializable
    data class Match(val id: Long, )

    @Serializable
    data class Participant(val name: String, val description: String)

    @Serializable
    @SerialName("League")
    data class League(
        val id: Long,
        val name: String,
        val matches: List<Match>,
    ) : CompetitionResponse()

    @Serializable
    @SerialName("Cup")
    data class Cup(
        val id: Long,
        val name: String,
        val matches: List<Match>,
    ) : CompetitionResponse()

    @Serializable
    @SerialName("Tournament")
    data class Tournament(
        val id: Long,
        val name: String,
        val matches: List<Match>,
    ) : CompetitionResponse()

}