package app.dto

import app.model.Match
import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.time.LocalDateTime

@Serializable
data class MatchListElementResponse(
    val id: Long,
    val dateTime: @Contextual LocalDateTime,
    val state: Match.State,
    val participants: List<Participant>,
    val competition: Competition,
    val round: Round,
    val group: Group?,
) {

    // A match participant which is maybe not known yet
    @Serializable
    sealed class Participant {

        // The participant is known
        @Serializable
        @SerialName("Fix")
        data class Fix(
            val participantId: Long,
            val participantName: String,
            val score: Double?,
        ) : Participant()

        // The participant is elected from a match, maybe known
        @Serializable
        @SerialName("ProceededFromMatch")
        data class ProceededFromMatch(
            val matchId: Long,
            val participantId: Long?,
            val participantName: String?,
            val score: Double?,
        ): Participant()

        // The participant is elected by ending up on a place in a group
        @Serializable
        @SerialName("ProceededFromGroup")
        data class ProceededFromGroup(
            val groupId: Long,
            val groupName: String,
            val groupPlace: Int,
            val participantId: Long?,
            val participantName: String?,
            val score: Double?
        ) : Participant()

    }

    @Serializable
    data class Competition(val id: Long, val name: String)

    @Serializable
    data class Round(val id: Long, val name: String)

    @Serializable
    data class Group(val id: Long, val name: String)

}