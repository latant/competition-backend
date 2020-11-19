package app.service

import app.dao.CompetitionGraph
import app.dto.MatchListElementResponse
import app.dto.MatchResponse
import app.error.RequestError
import app.model.*
import app.security.UserPrincipal
import app.service.MatchRetrievalService.toMatchListElementCompetitionDTO
import app.service.MatchRetrievalService.toMatchListElementGroupDTO
import app.service.MatchRetrievalService.toMatchListElementParticipantDTO
import app.service.MatchRetrievalService.toMatchListElementRoundDTO
import org.neo4j.ogm.cypher.ComparisonOperator
import org.neo4j.ogm.cypher.Filter
import org.neo4j.ogm.cypher.Filters
import org.neo4j.ogm.session.load
import org.neo4j.ogm.session.loadAll
import java.time.LocalDateTime

object MatchRetrievalService {

    fun getMatchesBetween(startDateTime: LocalDateTime, endDateTime: LocalDateTime): List<MatchListElementResponse> {
        CompetitionGraph.session {
            val filter = matchDateTimeBetweenFilter(startDateTime, endDateTime)
            val matches = loadAll<Match>(filter, depth = 2).sortedBy { it.dateTime }
            return matches.map { it.toMatchListElementDTO() }
        }
    }

    fun getUsersMatchesBetween(
        startDateTime: LocalDateTime,
        endDateTime: LocalDateTime,
        userPrincipal: UserPrincipal
    ): List<MatchListElementResponse> {
        CompetitionGraph.session {
            val filter = matchDateTimeBetweenFilter(startDateTime, endDateTime)
            val matches = loadAll<Match>(filter, depth = 2)
                .filter { it.editPermissionForUserWithId(userPrincipal.id) != MatchResponse.EditPermission.NONE }
                .sortedBy { it.dateTime }
            return matches.map { it.toMatchListElementDTO() }
        }
    }

    fun getMatch(matchId: Long, userPrincipal: UserPrincipal?): MatchResponse {
        CompetitionGraph.session {
            val match = load<Match>(matchId, depth = 2) ?: RequestError.MatchNotFound()
            val editPermission = userPrincipal?.let { match.editPermissionForUserWithId(it.id) }
                ?: MatchResponse.EditPermission.NONE
            return match.toMatchDTO(editPermission)
        }
    }

    private fun matchDateTimeBetweenFilter(startDateTime: LocalDateTime, endDateTime: LocalDateTime): Filters {
        val startDateTimeFilter = Filter(Match::dateTime.name, ComparisonOperator.GREATER_THAN_EQUAL, startDateTime)
        val endDateTimeFilter = Filter(Match::dateTime.name, ComparisonOperator.LESS_THAN_EQUAL, endDateTime)
        return startDateTimeFilter.and(endDateTimeFilter)
    }

    private fun Match.toMatchListElementDTO() = MatchListElementResponse(
        id = id!!,
        dateTime = dateTime,
        state = state,
        participants = participations.map { it.toMatchListElementParticipantDTO() },
        competition = competition.toMatchListElementCompetitionDTO(),
        round = round.toMatchListElementRoundDTO(),
        group = group?.toMatchListElementGroupDTO(),
    )

    private fun MatchParticipation.toMatchListElementParticipantDTO(): MatchListElementResponse.Participant {
        return when (this) {
            is FixMatchParticipation -> MatchListElementResponse.Participant.Fix(
                participantId = competitionParticipant!!.id!!,
                participantName = competitionParticipant!!.name,
                score = score,
            )
            is ProceededMatchParticipation -> MatchListElementResponse.Participant.ProceededFromMatch(
                participantId = competitionParticipant?.id,
                participantName = competitionParticipant?.name,
                score = score,
                matchId = matchToWin.id!!,
            )
            is PlayoffsQuoteMatchParticipation -> MatchListElementResponse.Participant.ProceededFromGroup(
                participantId = competitionParticipant?.id,
                participantName = competitionParticipant?.name,
                score = score,
                groupId = group.id!!,
                groupName = group.name,
                groupPlace = groupPlace
            )
            else -> error("Unknown match participant type: ${this::class.qualifiedName}")
        }
    }

    private fun Competition.toMatchListElementCompetitionDTO(): MatchListElementResponse.Competition {
        return MatchListElementResponse.Competition(id = id!!, name = name)
    }

    private fun Round.toMatchListElementRoundDTO() = MatchListElementResponse.Round(id = id!!, name = name)

    private fun Group.toMatchListElementGroupDTO() = MatchListElementResponse.Group(id = id!!, name = name)


    private fun Match.editPermissionForUserWithId(userId: Long): MatchResponse.EditPermission {
        return MatchEditorService.editPermissionForUserWithId(this, userId)
    }

    private fun Match.toMatchDTO(editPermission: MatchResponse.EditPermission) = MatchResponse(
        id = id!!,
        dateTime = dateTime,
        description = description,
        state = state,
        editPermission = editPermission,
        participants = participations.map { it.toMatchParticipantDTO() },
        competition = competition.toMatchCompetitionDTO(),
        round = round.toMatchRoundDTO(),
        group = group?.toMatchGroupDTO()
    )

    private fun MatchParticipation.toMatchParticipantDTO(): MatchResponse.Participant {
        return when (this) {
            is FixMatchParticipation -> MatchResponse.Participant.Fix(
                participantId = competitionParticipant!!.id!!,
                participantName = competitionParticipant!!.name,
                score = score,
            )
            is ProceededMatchParticipation -> MatchResponse.Participant.ProceededFromMatch(
                participantId = competitionParticipant?.id,
                participantName = competitionParticipant?.name,
                score = score,
                matchId = matchToWin.id!!,
            )
            is PlayoffsQuoteMatchParticipation -> MatchResponse.Participant.ProceededFromGroup(
                participantId = competitionParticipant?.id,
                participantName = competitionParticipant?.name,
                score = score,
                groupId = group.id!!,
                groupName = group.name,
                groupPlace = groupPlace
            )
            else -> error("Unknown match participant type: ${this::class.qualifiedName}")
        }
    }

    private fun Competition.toMatchCompetitionDTO(): MatchResponse.Competition {
        return MatchResponse.Competition(id = id!!, name = name)
    }

    private fun Round.toMatchRoundDTO() = MatchResponse.Round(id = id!!, name = name)

    private fun Group.toMatchGroupDTO() = MatchResponse.Group(id = id!!, name = name)

}