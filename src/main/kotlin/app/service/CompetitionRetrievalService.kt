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
        val competition = CompetitionGraph.readOnlyTransaction {
            load<Competition>(competitionId, depth = 4) ?: RequestError.CompetitionNotFound()
        }
        return competition.toCompetitionDTO(userPrincipal?.let { it.id == competition.creator.id })
    }

    fun getCompetitionsBetween(startDateTime: LocalDateTime, endDateTime: LocalDateTime): List<CompetitionListElementResponse> {
        val startDateTimeFilter = Filter(Competition::endDateTime.name, ComparisonOperator.GREATER_THAN_EQUAL, startDateTime)
        val endDateTimeFilter = Filter(Competition::startDateTime.name, ComparisonOperator.LESS_THAN_EQUAL, endDateTime)
        val filter = startDateTimeFilter.and(endDateTimeFilter)
        return CompetitionGraph.readOnlyTransaction { loadAll<Competition>(filter) }
            .map { it.toCompetitionListElementDTO() }
    }

    fun getGroup(groupId: Long): GroupResponse {
        val group = CompetitionGraph.readOnlyTransaction {
            load<Group>(groupId, depth = 3) ?: RequestError.GroupNotFound()
        }
        return group.toGroupDTO()
    }

    private fun Competition.toCompetitionListElementDTO(): CompetitionListElementResponse = when (this) {
        is League -> CompetitionListElementResponse.League(id!!, name, startDateTime)
        is Cup -> CompetitionListElementResponse.Cup(id!!, name, startDateTime)
        is Tournament -> CompetitionListElementResponse.Tournament(id!!, name, startDateTime)
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
            startDateTime = startDateTime,
            endDateTime = endDateTime,
        )
        is Cup -> CompetitionResponse.Cup(
            id = id!!,
            name = name,
            matches = matches.map { it.toMatchDTO() },
            competitors = competitors.map { it.toCompetitorDTO() },
            rounds = stage.rounds.map { it.toRoundDTO() },
            editable = editable,
            startDateTime = startDateTime,
            endDateTime = endDateTime,
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
            startDateTime = startDateTime,
            endDateTime = endDateTime,
        )
        else -> error("Unknown competition type: ${this::class.qualifiedName}")
    }

    private fun Match.toMatchDTO() = CompetitionResponse.Match(
        id = id!!,
        dateTime = dateTime,
        participants = participations.map { it.toMatchParticipantDTO() },
        state = state,
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
        playoffsQuotes = playoffsQuotes.map { it.toGroupQuoteDTO() }
    )

    private fun PlayoffsQuoteMatchParticipation.toGroupQuoteDTO() = GroupResponse.PlayoffsQuote(
        place = groupPlace,
        matchId = match.id!!
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

    fun standingsTable(matches: Set<Match>, competitors: List<Competitor>): StandingsTable {
        val records = competitors
            .map { it.standingsRecordIn(matches) }
            .sortedBy { it.scores }
            .sortedBy { it.wins }
            .mapIndexed { i, r -> r.copy(place = i + 1) }
        return StandingsTable(records)
    }

    private fun Competitor.standingsRecordIn(matches: Set<Match>): StandingsTable.Record {
        val competitorsMatches = matchParticipations.map { it.match }.filter { it in matches }
        val competitorsEndedMatches = competitorsMatches.filter { it.state == Match.State.ENDED }
        return StandingsTable.Record(
            place = -1,
            competitorId = id!!,
            wins = competitorsEndedMatches.count { won(it) },
            scores = competitorsEndedMatches.sumByDouble { it.scoreOf(this) ?: 0.0 },
            matchesCount = competitorsMatches.size,
            matchesPlayed = competitorsEndedMatches.size,
        )
    }

    private fun Competitor.won(match: Match): Boolean {
        return match.participations.maxByOrNull { it.score ?: Double.MIN_VALUE }?.competitor?.id == this.id
    }

    private fun Match.scoreOf(competitor: Competitor): Double? {
        return participations.find { it.competitor == competitor }?.score
    }

}