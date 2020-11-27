package app.dto

import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.time.LocalDateTime

@Serializable
sealed class CompetitionResponse {

    @Serializable
    @SerialName("League")
    data class League(
        val id: Long,
        val name: String,
        val matches: List<Match>,
        val competitors: List<Competitor>,
        val rounds: List<Round>,
        val editable: Boolean?,
        val standingsTable: StandingsTable,
        val startDateTime: @Contextual LocalDateTime,
        val endDateTime: @Contextual LocalDateTime,
    ) : CompetitionResponse()

    @Serializable
    @SerialName("Cup")
    data class Cup(
        val id: Long,
        val name: String,
        val matches: List<Match>,
        val competitors: List<Competitor>,
        val rounds: List<Round>,
        val editable: Boolean?,
        val startDateTime: @Contextual LocalDateTime,
        val endDateTime: @Contextual LocalDateTime,
    ) : CompetitionResponse()

    @Serializable
    @SerialName("Tournament")
    data class Tournament(
        val id: Long,
        val name: String,
        val matches: List<Match>,
        val competitors: List<Competitor>,
        val groupStageRounds: List<Round>,
        val playoffsStageRounds: List<Round>,
        val groups: List<Group>,
        val editable: Boolean?,
        val startDateTime: @Contextual LocalDateTime,
        val endDateTime: @Contextual LocalDateTime,
    ) : CompetitionResponse() {
        @Serializable
        data class Group(
            val id: Long,
            val name: String,
            val matchIds: List<Long>,
            val competitorIds: List<Long>,
            val standingsTable: StandingsTable,
        )
    }


    @Serializable
    data class Match(
        val id: Long,
        val state: app.model.Match.State,
        val dateTime: @Contextual LocalDateTime,
        val participants: List<Participant>
    ) {

        // A match participant which is maybe not known yet
        @Serializable
        sealed class Participant {

            // The participant is known
            @Serializable
            @SerialName("Fix")
            data class Fix(
                val competitorId: Long,
                val score: Double?,
            ) : Participant()

            // The participant is elected from a match, maybe known
            @Serializable
            @SerialName("ProceededFromMatch")
            data class ProceededFromMatch(
                val matchId: Long,
                val competitorId: Long?,
                val score: Double?,
            ): Participant()

            // The participant is elected by ending up on a place in a group
            @Serializable
            @SerialName("ProceededFromGroup")
            data class ProceededFromGroup(
                val groupId: Long,
                val groupPlace: Int,
                val competitorId: Long?,
                val score: Double?
            ) : Participant()

        }
    }

    @Serializable
    data class Competitor(
        val id: Long,
        val name: String,
        val description: String
    )

    @Serializable
    data class Round(
        val id: Long,
        val name: String,
        val matchIds: List<Long>
    )

}