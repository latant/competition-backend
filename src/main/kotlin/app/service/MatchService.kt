package app.service

import app.dao.CompetitionGraph
import app.dto.MatchListElementResponse
import app.model.*
import org.neo4j.ogm.cypher.ComparisonOperator
import org.neo4j.ogm.cypher.Filter
import org.neo4j.ogm.cypher.query.SortOrder
import org.neo4j.ogm.session.loadAll
import java.time.LocalDateTime

object MatchService {

    fun getMatchesBetween(startTime: LocalDateTime, endTime: LocalDateTime): List<MatchListElementResponse> {
        CompetitionGraph.session {
            val startTimeFilter = Filter(Match::dateTime.name, ComparisonOperator.GREATER_THAN_EQUAL, startTime)
            val endTimeFilter = Filter(Match::dateTime.name, ComparisonOperator.LESS_THAN_EQUAL, endTime)
            val sortOrder = SortOrder().asc(Match::dateTime.name)
            val matches = loadAll<Match>(startTimeFilter.and(endTimeFilter), sortOrder, depth = 2)
            return matches.map { m ->
                MatchListElementResponse(
                    id = m.id!!,
                    dateTime = m.dateTime,
                    state = m.state,
                    participants = m.participations.map { it.toParticipantDTO() },
                    competition = m.competition.toCompetitionDTO(),
                    round = m.round.toRoundDTO(),
                    group = m.group?.toGroupDTO(),
                )
            }
        }
    }

    private fun MatchParticipation.toParticipantDTO() = when (val p = participant) {
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

    private fun Competition.toCompetitionDTO() = MatchListElementResponse.Competition(id = id!!, name = name)

    private fun Round.toRoundDTO() = MatchListElementResponse.Round(id = id!!, name = name)

    private fun Group.toGroupDTO() = MatchListElementResponse.Group(id = id!!, name = name)

}