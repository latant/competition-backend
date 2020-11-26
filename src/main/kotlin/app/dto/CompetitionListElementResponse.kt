package app.dto

import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.time.LocalDateTime

@Serializable
sealed class CompetitionListElementResponse {

    @Serializable
    @SerialName("League")
    data class League(
        val id: Long,
        val name: String,
        val dateTime: @Contextual LocalDateTime,
    ) : CompetitionListElementResponse()

    @Serializable
    @SerialName("Cup")
    data class Cup(
        val id: Long,
        val name: String,
        val dateTime: @Contextual LocalDateTime,
    ) : CompetitionListElementResponse()

    @Serializable
    @SerialName("Tournament")
    data class Tournament(
        val id: Long,
        val name: String,
        val dateTime: @Contextual LocalDateTime,
    ) : CompetitionListElementResponse()

}
