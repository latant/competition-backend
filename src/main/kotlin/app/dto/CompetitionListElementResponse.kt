package app.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed class CompetitionListElementResponse {

    @Serializable
    @SerialName("League")
    data class League(
        val id: Long,
        val name: String,
    ) : CompetitionListElementResponse()

    @Serializable
    @SerialName("Cup")
    data class Cup(
        val id: Long,
        val name: String,
    ) : CompetitionListElementResponse()

    @Serializable
    @SerialName("Tournament")
    data class Tournament(
        val id: Long,
        val name: String,
    ) : CompetitionListElementResponse()

}
