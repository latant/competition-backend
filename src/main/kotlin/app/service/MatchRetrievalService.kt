package app.service

import app.dao.CompetitionGraph
import app.dto.MatchListElementResponse
import app.dto.MatchResponse
import app.dto.MatchResponse.EditPermission.NONE
import app.error.RequestError
import app.model.*
import app.security.UserPrincipal
import org.neo4j.ogm.cypher.ComparisonOperator
import org.neo4j.ogm.cypher.Filter
import org.neo4j.ogm.cypher.Filters
import org.neo4j.ogm.session.load
import org.neo4j.ogm.session.loadAll
import utcNow
import java.time.LocalDateTime

object MatchRetrievalService {

    fun getMatchesBetween(
        startDateTime: LocalDateTime,
        endDateTime: LocalDateTime,
        userPrincipal: UserPrincipal?
    ): List<MatchListElementResponse> {
        val matches = CompetitionGraph.readOnlyTransaction {
            val filter = matchDateTimeBetweenFilter(startDateTime, endDateTime)
            loadAll<Match>(filter, depth = 2).sortedBy { it.dateTime }
        }
        return matches.map { it.toMatchListElementDTO(userPrincipal) }
    }

    fun getUsersMatchesBetween(
        startDateTime: LocalDateTime,
        endDateTime: LocalDateTime,
        userPrincipal: UserPrincipal
    ): List<MatchListElementResponse> {
        return getMatchesBetween(startDateTime, endDateTime, userPrincipal).filter { it.editPermission != NONE }
    }

    fun getMatch(matchId: Long, userPrincipal: UserPrincipal?): MatchResponse {
        val match = CompetitionGraph.readOnlyTransaction {
            load<Match>(matchId, depth = 2) ?: RequestError.MatchNotFound()
        }
        val editPermission = userPrincipal?.let { match.editPermissionForUserWithId(it.id) } ?: NONE
        return match.toMatchDTO(editPermission)
    }

    fun getActualMatchesOfCompetition(competitionId: Long, userPrincipal: UserPrincipal?): List<MatchListElementResponse> {
        val now = utcNow()
        val fiveAgo = now.minusMinutes(5)
        val fiveLater = now.plusMinutes(5)
        val competition = CompetitionGraph.readOnlyTransaction {
            load<Competition>(competitionId, depth = 2) ?: RequestError.CompetitionNotFound()
        }
        return competition.matches
            .filter {
                it.state == Match.State.ONGOING ||
                    it.dateTime in (now..fiveLater) ||
                    it.endDateTime in (fiveAgo..now)
            }
            .map { it.toMatchListElementDTO(userPrincipal) }
    }

    private fun matchDateTimeBetweenFilter(minDateTime: LocalDateTime, maxDateTime: LocalDateTime): Filters {
        val minDateTimeFilter = Filter(Match::dateTime.name, ComparisonOperator.GREATER_THAN_EQUAL, minDateTime)
        val maxDateTimeFilter = Filter(Match::dateTime.name, ComparisonOperator.LESS_THAN_EQUAL, maxDateTime)
        return minDateTimeFilter.and(maxDateTimeFilter)
    }

    private fun Match.toMatchListElementDTO(userPrincipal: UserPrincipal?) = MatchListElementResponse(
        id = id!!,
        dateTime = dateTime,
        state = state,
        participants = participations.map { it.toMatchListElementParticipantDTO() },
        competition = competition.toMatchListElementCompetitionDTO(),
        round = round.toMatchListElementRoundDTO(),
        group = group?.toMatchListElementGroupDTO(),
        editPermission = userPrincipal?.let { editPermissionForUserWithId(it.id) }
    )

    private fun MatchParticipation.toMatchListElementParticipantDTO(): MatchListElementResponse.Participant {
        return when (this) {
            is FixMatchParticipation -> MatchListElementResponse.Participant.Fix(
                competitorId = competitor!!.id!!,
                competitorName = competitor!!.name,
                score = score,
            )
            is ProceededMatchParticipation -> MatchListElementResponse.Participant.ProceededFromMatch(
                competitorId = competitor?.id,
                competitorName = competitor?.name,
                score = score,
                matchId = matchToWin.id!!,
            )
            is PlayoffsQuoteMatchParticipation -> MatchListElementResponse.Participant.ProceededFromGroup(
                competitorId = competitor?.id,
                competitorName = competitor?.name,
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
        editors = if (editPermission == NONE) null else editors?.map { it.toMatchEditorDTO() } ?: emptyList(),
        participants = participations.map { it.toMatchParticipantDTO() },
        competition = competition.toMatchCompetitionDTO(),
        round = round.toMatchRoundDTO(),
        group = group?.toMatchGroupDTO()
    )

    private fun User.toMatchEditorDTO() = MatchResponse.Editor(email, name)

    private fun MatchParticipation.toMatchParticipantDTO(): MatchResponse.Participant {
        return when (this) {
            is FixMatchParticipation -> MatchResponse.Participant.Fix(
                competitorId = competitor!!.id!!,
                competitorName = competitor!!.name,
                score = score,
            )
            is ProceededMatchParticipation -> MatchResponse.Participant.ProceededFromMatch(
                competitorId = competitor?.id,
                competitorName = competitor?.name,
                score = score,
                matchId = matchToWin.id!!,
            )
            is PlayoffsQuoteMatchParticipation -> MatchResponse.Participant.ProceededFromGroup(
                competitorId = competitor?.id,
                competitorName = competitor?.name,
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