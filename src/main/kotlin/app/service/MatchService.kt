package app.service

import app.dao.CompetitionGraph
import app.dto.MatchListElementResponse
import app.dto.MatchResponse
import app.model.*
import app.security.UserPrincipal
import org.neo4j.ogm.cypher.ComparisonOperator
import org.neo4j.ogm.cypher.Filter
import org.neo4j.ogm.cypher.Filters
import org.neo4j.ogm.session.loadAll
import java.time.LocalDateTime

object MatchService {

    fun getMatchesBetween(startDateTime: LocalDateTime, endDateTime: LocalDateTime): List<MatchListElementResponse> {
        CompetitionGraph.session {
            val filter = matchDateTimeBetweenFilter(startDateTime, endDateTime)
            val matches = loadAll<Match>(filter, depth = 2).sortedBy { it.dateTime }
            return matches.map { m ->
                MatchListElementResponse(
                    id = m.id!!,
                    dateTime = m.dateTime,
                    state = m.state,
                    participants = m.participations.map { it.toMatchListElementParticipantDTO() },
                    competition = m.competition.toMatchListElementCompetitionDTO(),
                    round = m.round.toMatchListElementRoundDTO(),
                    group = m.group?.toMatchListElementGroupDTO(),
                )
            }
        }
    }

    fun getUsersMatchesBetween(
        startDateTime: LocalDateTime,
        endDateTime: LocalDateTime,
        userPrincipal: UserPrincipal
    ): List<MatchResponse> {
        CompetitionGraph.session {
            val filter = matchDateTimeBetweenFilter(startDateTime, endDateTime)
            val matchesWithPermissions = loadAll<Match>(filter, depth = 2)
                .map { it to it.editPermissionForUserWithId(userPrincipal.id) }
                .sortedBy { (m) -> m.dateTime }
            return matchesWithPermissions.map { (m, p) ->
                MatchResponse(
                    id = m.id!!,
                    dateTime = m.dateTime,
                    description = m.description,
                    state = m.state,
                    editPermission = p,
                    participants = m.participations.map { it.toMatchParticipantDTO() },
                    competition = m.competition.toMatchCompetitionDTO(),
                    round = m.round.toMatchRoundDTO(),
                    group = m.group?.toMatchGroupDTO()
                )
            }
        }
    }

    private fun matchDateTimeBetweenFilter(startDateTime: LocalDateTime, endDateTime: LocalDateTime): Filters {
        val startDateTimeFilter = Filter(Match::dateTime.name, ComparisonOperator.GREATER_THAN_EQUAL, startDateTime)
        val endDateTimeFilter = Filter(Match::dateTime.name, ComparisonOperator.LESS_THAN_EQUAL, endDateTime)
        return startDateTimeFilter.and(endDateTimeFilter)
    }

    private fun MatchParticipation.toMatchListElementParticipantDTO(): MatchListElementResponse.Participant {
        return when (val p = participant) {
            is FixMatchParticipant -> MatchListElementResponse.Participant.Fix(
                participantId = p.competitionParticipant!!.id!!,
                participantName = p.competitionParticipant!!.name,
                score = score,
            )
            is ProceededMatchParticipant -> MatchListElementResponse.Participant.ProceededFromMatch(
                participantId = p.competitionParticipant?.id,
                participantName = p.competitionParticipant?.name,
                score = score,
                matchId = p.matchToWin.id!!,
            )
            is PlayoffsQuoteMatchParticipant -> MatchListElementResponse.Participant.ProceededFromGroup(
                participantId = p.competitionParticipant?.id,
                participantName = p.competitionParticipant?.name,
                score = score,
                groupId = p.group.id!!,
                groupName = p.group.name,
                groupPlace = p.place
            )
            else -> error("Unknown match participant type: ${p::class.qualifiedName}")
        }
    }

    private fun Competition.toMatchListElementCompetitionDTO(): MatchListElementResponse.Competition {
        return MatchListElementResponse.Competition(id = id!!, name = name)
    }

    private fun Round.toMatchListElementRoundDTO() = MatchListElementResponse.Round(id = id!!, name = name)

    private fun Group.toMatchListElementGroupDTO() = MatchListElementResponse.Group(id = id!!, name = name)


    private fun Match.editPermissionForUserWithId(userId: Long): MatchResponse.EditPermission {
        return when {
            competition.creator.id == userId -> MatchResponse.EditPermission.FULL
            editors.any { it.id == userId } -> MatchResponse.EditPermission.BASIC
            else -> MatchResponse.EditPermission.NONE
        }
    }

    private fun MatchParticipation.toMatchParticipantDTO(): MatchResponse.Participant {
        return when (val p = participant) {
            is FixMatchParticipant -> MatchResponse.Participant.Fix(
                participantId = p.competitionParticipant!!.id!!,
                participantName = p.competitionParticipant!!.name,
                score = score,
            )
            is ProceededMatchParticipant -> MatchResponse.Participant.ProceededFromMatch(
                participantId = p.competitionParticipant?.id,
                participantName = p.competitionParticipant?.name,
                score = score,
                matchId = p.matchToWin.id!!,
            )
            is PlayoffsQuoteMatchParticipant -> MatchResponse.Participant.ProceededFromGroup(
                participantId = p.competitionParticipant?.id,
                participantName = p.competitionParticipant?.name,
                score = score,
                groupId = p.group.id!!,
                groupName = p.group.name,
                groupPlace = p.place
            )
            else -> error("Unknown match participant type: ${p::class.qualifiedName}")
        }
    }

    private fun Competition.toMatchCompetitionDTO(): MatchResponse.Competition {
        return MatchResponse.Competition(id = id!!, name = name)
    }

    private fun Round.toMatchRoundDTO() = MatchResponse.Round(id = id!!, name = name)

    private fun Group.toMatchGroupDTO() = MatchResponse.Group(id = id!!, name = name)

}