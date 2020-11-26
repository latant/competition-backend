package app.dto

import app.model.Match
import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.time.LocalDateTime

// Response that contains all necessary data for displaying a page of a match
@Serializable
data class MatchResponse(
    val id: Long,
    val dateTime: @Contextual LocalDateTime,
    val description: String,
    val state: Match.State,
    val editPermission: EditPermission,
    val editors: List<Editor>?,
    val participants: List<Participant>,
    val competition: Competition,
    val round: Round,
    val group: Group?,
) {

    // Determines what parts of the match's data can be edited by the user who retrieved it.
    enum class EditPermission {
        // The user can edit the scores, description and state
        BASIC,
        // The user can also edit the date & time and editor permissions
        FULL,
        // The user has no permission to edit any data regarding the match
        NONE,
    }

    @Serializable
    data class Editor(val email: String, val name: String)

    // A match participant which is maybe not known yet
    @Serializable
    sealed class Participant {

        // The participant is known
        @Serializable
        @SerialName("Fix")
        data class Fix(
            val competitorId: Long,
            val competitorName: String,
            val score: Double?,
        ) : Participant()

        // The participant is elected from a match, maybe known
        @Serializable
        @SerialName("ProceededFromMatch")
        data class ProceededFromMatch(
            val matchId: Long,
            val competitorId: Long?,
            val competitorName: String?,
            val score: Double?,
        ): Participant()

        // The participant is elected by ending up on a place in a group
        @Serializable
        @SerialName("ProceededFromGroup")
        data class ProceededFromGroup(
            val groupId: Long,
            val groupName: String,
            val groupPlace: Int,
            val competitorId: Long?,
            val competitorName: String?,
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