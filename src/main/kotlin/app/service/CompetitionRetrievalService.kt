package app.service

import app.dao.CompetitionGraph
import app.dto.CompetitionListElementResponse
import app.dto.CompetitionResponse
import app.dto.GroupResponse
import app.dto.StandingsTable
import app.error.RequestError
import app.model.*
import app.security.UserPrincipal
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

    fun getGroup(groupId: Long): GroupResponse {
        CompetitionGraph.readOnlyTransaction {
            val group = load<Group>(groupId, depth = 3) ?: RequestError.GroupNotFound()
            return group.toGroupDTO()
        }
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
            competitors = competitors.map { it.toCompetitorDTO() },
            rounds = stage.rounds.map { it.toRoundDTO() },
            editable = editable,
            standingsTable = standingsTable(),
        )
        is Cup -> CompetitionResponse.Cup(
            id = id!!,
            name = name,
            matches = matches.map { it.toMatchDTO() },
            competitors = competitors.map { it.toCompetitorDTO() },
            rounds = stage.rounds.map { it.toRoundDTO() },
            editable = editable,
        )
        is Tournament -> CompetitionResponse.Tournament(
            id = id!!,
            name = name,
            matches = matches.map { it.toMatchDTO() },
            competitors = competitors.map { it.toCompetitorDTO() },
            groupStageRounds = groupStage.rounds.map { it.toRoundDTO() },
            playoffsStageRounds = playoffsStage.rounds.map { it.toRoundDTO() },
            groups = groupStage.groups.map { it.toTournamentGroupDTO() },
            editable = editable,
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
                competitorId = competitor!!.id!!,
                score = score,
            )
            is ProceededMatchParticipation -> CompetitionResponse.Match.Participant.ProceededFromMatch(
                competitorId = competitor?.id,
                score = score,
                matchId = matchToWin.id!!,
            )
            is PlayoffsQuoteMatchParticipation -> CompetitionResponse.Match.Participant.ProceededFromGroup(
                competitorId = competitor?.id,
                score = score,
                groupId = group.id!!,
                groupPlace = groupPlace,
            )
            else -> error("Unknown match participant type: ${this::class.qualifiedName}")
        }
    }

    private fun Competitor.toCompetitorDTO(): CompetitionResponse.Competitor {
        return CompetitionResponse.Competitor(
            id = id!!,
            name = name,
            description = description,
        )
    }

    private fun Round.toRoundDTO() = CompetitionResponse.Round(id = id!!, name = name, matchIds = matches.map { it.id!! })

    private fun Group.toTournamentGroupDTO() = CompetitionResponse.Tournament.Group(
        id = id!!,
        name = name,
        matchIds = matches.map { it.id!! },
        competitorIds = competitors.map { it.id!! },
        standingsTable = standingsTable(),
    )

    private fun Group.toGroupDTO() = GroupResponse(
        id = id!!,
        name = name,
        matches = matches.map { it.toGroupMatchDTO() },
        competitors = competitors.map { it.toGroupCompetitorDTO() },
        standingsTable = standingsTable(),
        competitionId = groupStage.competition.id!!,
        competitionName = groupStage.competition.name,
    )

    private fun Competitor.toGroupCompetitorDTO() = GroupResponse.Competitor(
        id = id!!,
        name = name,
        description = description,
    )

    private fun Match.toGroupMatchDTO() = GroupResponse.Match(
        id = id!!,
        dateTime = dateTime,
        participants = participations.map { (it as FixMatchParticipation).toGroupMatchParticipantDTO() },
    )

    private fun FixMatchParticipation.toGroupMatchParticipantDTO() = GroupResponse.Match.Participant(
        competitorId = competitor!!.id!!,
        score = score,
    )

    private fun League.standingsTable() = standingsTable(matches.toSet(), competitors)
    private fun Group.standingsTable() = standingsTable(matches.toSet(), competitors)

    private fun standingsTable(matches: Set<Match>, competitors: List<Competitor>): StandingsTable {
        val records = competitors
            .map { it.standingsRecordIn(matches) }
            .sortedBy { it.scores }
            .sortedBy { it.wins }
            .mapIndexed { i, r -> r.copy(place = i + 1) }
        return StandingsTable(records)
    }

    private fun Competitor.standingsRecordIn(matches: Set<Match>): StandingsTable.Record {
        val competitorsGroupMatches = matchParticipations
            .map { it.match }
            .filter { it.state == Match.State.ENDED }
            .filter { it in matches }
        return StandingsTable.Record(
            place = -1,
            competitorId = id!!,
            wins = competitorsGroupMatches.count { won(it) },
            scores = competitorsGroupMatches.sumByDouble { it.scoreOf(this) ?: 0.0 }
        )
    }

    private fun Competitor.won(match: Match): Boolean {
        return match.participations.maxByOrNull { it.score ?: Double.MIN_VALUE }?.competitor?.id == this.id
    }

    private fun Match.scoreOf(competitor: Competitor): Double? {
        return participations.find { it.competitor == competitor }?.score
    }

}