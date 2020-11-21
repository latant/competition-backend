package app.service

import app.dao.CompetitionGraph
import app.dto.CompetitionListElementResponse
import app.dto.CompetitionResponse
import app.error.RequestError
import app.model.*
import app.security.UserPrincipal
import app.service.CompetitionRetrievalService.toCompetitionDTO
import org.neo4j.ogm.cypher.ComparisonOperator
import org.neo4j.ogm.cypher.Filter
import org.neo4j.ogm.session.load
import org.neo4j.ogm.session.loadAll
import java.time.LocalDateTime

object CompetitionRetrievalService {

    fun getCompetition(userPrincipal: UserPrincipal?, competitionId: Long): CompetitionResponse {
        CompetitionGraph.readOnlyTransaction {
            val competition = load<Competition>(competitionId, depth = 4) ?: RequestError.CompetitionNotFound()
            return competition.toCompetitionDTO(userPrincipal?.let { it.id == competition.creator.id })
        }
    }

    fun getCompetitionsBetween(startDateTime: LocalDateTime, endDateTime: LocalDateTime): List<CompetitionListElementResponse> {
        val startDateTimeFilter = Filter(Competition::dateTime.name, ComparisonOperator.GREATER_THAN_EQUAL, startDateTime)
        val endDateTimeFilter = Filter(Competition::dateTime.name, ComparisonOperator.LESS_THAN_EQUAL, endDateTime)
        val filter = startDateTimeFilter.and(endDateTimeFilter)
        return CompetitionGraph.readOnlyTransaction { loadAll<Competition>(filter) }
            .map { it.toCompetitionListElementDTO() }
    }

    private fun Competition.toCompetitionListElementDTO(): CompetitionListElementResponse = when (this) {
        is League -> CompetitionListElementResponse.League(id!!, name)
        is Cup -> CompetitionListElementResponse.Cup(id!!, name)
        is Tournament -> CompetitionListElementResponse.Tournament(id!!, name)
        else -> error("Unknown competition type: ${this::class.qualifiedName}")
    }

    private fun Competition.toCompetitionDTO(editable: Boolean?): CompetitionResponse = when (this) {
        is League -> CompetitionResponse.League(
            id = id!!,
            name = name,
            matches = matches.map { it.toMatchDTO() },
            participants = participants.map { it.toCompetitionParticipantDTO() },
            rounds = stage.rounds.map { it.toRoundDTO() },
            editable = editable
        )
        is Cup -> CompetitionResponse.Cup(
            id = id!!,
            name = name,
            matches = matches.map { it.toMatchDTO() },
            participants = participants.map { it.toCompetitionParticipantDTO() },
            rounds = stage.rounds.map { it.toRoundDTO() },
            editable = editable
        )
        is Tournament -> CompetitionResponse.Tournament(
            id = id!!,
            name = name,
            matches = matches.map { it.toMatchDTO() },
            participants = participants.map { it.toCompetitionParticipantDTO() },
            groupStageRounds = groupStage.rounds.map { it.toRoundDTO() },
            playoffsStageRounds = playoffsStage.rounds.map { it.toRoundDTO() },
            groups = groupStage.groups.map { it.toGroupDTO() },
            editable = editable
        )
        else -> error("Unknown competition type: ${this::class.qualifiedName}")
    }

    private fun Match.toMatchDTO() = CompetitionResponse.Match(
        id = id!!,
        dateTime = dateTime,
        participants = participations.map { it.toMatchParticipantDTO() },
    )

    private fun MatchParticipation.toMatchParticipantDTO(): CompetitionResponse.Match.Participant {
        return when(this) {
            is FixMatchParticipation -> CompetitionResponse.Match.Participant.Fix(
                participantId = competitionParticipant!!.id!!,
                score = score,
            )
            is ProceededMatchParticipation -> CompetitionResponse.Match.Participant.ProceededFromMatch(
                participantId = competitionParticipant?.id,
                score = score,
                matchId = matchToWin.id!!,
            )
            is PlayoffsQuoteMatchParticipation -> CompetitionResponse.Match.Participant.ProceededFromGroup(
                participantId = competitionParticipant?.id,
                score = score,
                groupId = group.id!!,
                groupPlace = groupPlace
            )
            else -> error("Unknown match participant type: ${this::class.qualifiedName}")
        }
    }

    private fun CompetitionParticipant.toCompetitionParticipantDTO(): CompetitionResponse.CompetitionParticipant {
        return CompetitionResponse.CompetitionParticipant(
            id = id!!,
            name = name,
            description = description
        )
    }

    private fun Round.toRoundDTO() = CompetitionResponse.Round(id = id!!, name = name, matchIds = matches.map { it.id!! })

    private fun Group.toGroupDTO() = CompetitionResponse.Tournament.Group(
        id = id!!,
        name = name,
        matchIds = matches.map { it.id!! },
        participantIds = participants.map { it.id!! }
    )

}