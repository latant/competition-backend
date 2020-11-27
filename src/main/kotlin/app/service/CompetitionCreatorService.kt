package app.service

import app.dao.CompetitionGraph
import app.dto.CompetitionCreationRequest
import app.model.*
import app.security.UserPrincipal
import atUTC
import org.neo4j.ogm.session.Session
import org.neo4j.ogm.session.load

object CompetitionCreatorService {

    fun createCompetition(userPrincipal: UserPrincipal, competitionCreation: CompetitionCreationRequest): Long {
        return CompetitionGraph.readWriteTransaction {
            this.load<User>(userPrincipal.id, 1)!!
            val user = load<User>(userPrincipal.id, 1)!!
            when (competitionCreation) {
                is CompetitionCreationRequest.League -> createLeague(user, competitionCreation)
                is CompetitionCreationRequest.Cup -> createCup(user, competitionCreation)
                is CompetitionCreationRequest.Tournament -> createTournament(user, competitionCreation)
            }
        }
    }

    private fun Session.createLeague(creator: User, leagueCreation: CompetitionCreationRequest.League): Long {
        val league = leagueCreation.toNode()
        league.creator = creator
        league.competitors = leagueCreation.competitors.map { it.toNode() }
        val scheduling = CompetitionAlgorithmService.leagueScheduling(league.competitorCount).shuffled()
        league.stages = listOf(LeagueStage(scheduling.size))
            .let { s -> leagueCreation.roundCount?.let { s.take(it) } ?: s }
        val participantsShuffled = league.competitors.shuffled()
        league.stage.rounds = scheduling.mapIndexed { i, pairs ->
            Round("Round ${i + 1}", i + 1, "").also { r ->
                r.matches = pairs.map { (a, b) ->
                    Match(league.startDateTime, "").also { m ->
                        m.competition = league
                        m.participations = listOf(a, b).map { pi ->
                            FixMatchParticipation().also { fmp ->
                                fmp.competitor = participantsShuffled[pi]
                            }
                        }
                    }
                }
            }
        }
        save(league)
        return league.id!!
    }

    private fun Session.createCup(creator: User, cupCreation: CompetitionCreationRequest.Cup): Long {
        val cup = cupCreation.toNode()
        cup.creator = creator
        cup.competitors = cupCreation.competitors.map { it.toNode() }
        val finalMatchSource = CompetitionAlgorithmService.cupMatchSource(cup.competitors.indices)
            as CompetitionAlgorithmService.CupMatchSource.Match
        cup.stages = listOf(PlayoffsStage(roundCount = finalMatchSource.roundOrdinal))
        cup.stage.run {
            rounds = (1..roundCount).map { Round("Round $it", it, "") }
            finalMatch = createCupMatchOf(finalMatchSource, cup)
        }
        save(cup)
        return cup.id!!
    }

    private fun Session.createTournament(creator: User, tournamentCreation: CompetitionCreationRequest.Tournament): Long {
        val tournament = tournamentCreation.toNode()
        tournament.creator = creator
        tournament.competitors = tournamentCreation.competitors.map { it.toNode() }.shuffled()
        val groupsStructure = CompetitionAlgorithmService
            .groupsWithCount(tournament.competitors, tournamentCreation.groupCount)
            .map { it to CompetitionAlgorithmService.leagueScheduling(it.size) }
        val groupStageRoundCount = groupsStructure.maxOfOrNull { (_, sch) -> sch.size }!!
        val groupStage = GroupStage(name = "Group stage", roundCount = groupStageRoundCount).also { gs ->
            gs.rounds = (1..gs.roundCount).map { Round("Group stage Round $it", it, "") }
            gs.groups = groupsStructure.mapIndexed { gi, (gps, gsch) ->
                Group("Group ${gi + 1}", "").also { g ->
                    g.competitors = gps
                    g.matches = gsch.flatMapIndexed { ri, pairs ->
                        pairs.map { (a, b) ->
                            Match(tournament.startDateTime, "").also { m ->
                                m.competition = tournament
                                m.round = gs.rounds[ri]
                                m.participations = listOf(a, b).map { ci ->
                                    FixMatchParticipation().also { fmp ->
                                        fmp.competitor = gps[ci]
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        val playoffsFinalSource = CompetitionAlgorithmService
            .playOffs(groupsStructure.map { it.first }, tournamentCreation.playoffsCompetitorCount)
            as CompetitionAlgorithmService.PlayoffMatchSource.Match
        val playoffsStageRoundCount = playoffsFinalSource.roundOrdinal
        val playoffsStage = PlayoffsStage(name = "Play-offs", roundCount = playoffsStageRoundCount).also { pst ->
            pst.rounds = (1..pst.roundCount).map { Round("Playoffs Round $it", it, "") }
        }
        tournament.stages = listOf(groupStage, playoffsStage)
        playoffsStage.finalMatch = createPlayoffMatchOf(playoffsFinalSource, tournament)
        save(tournament)
        return tournament.id!!
    }

    private fun CompetitionCreationRequest.Competitor.toNode() = Competitor(name, description)

    private fun CompetitionCreationRequest.League.toNode() = League(
        name = name,
        description = description,
        logo = null,
        startDateTime = startDateTime.atUTC(),
        endDateTime = endDateTime.atUTC(),
        displayColor = displayColor,
        competitorCount = competitors.size,
        styleSheet = styleSheet,
    )

    private fun CompetitionCreationRequest.Cup.toNode() = Cup(
        name = name,
        description = description,
        startDateTime = startDateTime.atUTC(),
        endDateTime = endDateTime.atUTC(),
        logo = null,
        displayColor = displayColor,
        competitorCount = competitors.size,
        styleSheet = styleSheet,
    )

    private fun CompetitionCreationRequest.Tournament.toNode() = Tournament(
        name = name,
        description = description,
        logo = null,
        startDateTime = startDateTime.atUTC(),
        endDateTime = endDateTime.atUTC(),
        displayColor = displayColor,
        competitorCount = competitors.size,
        styleSheet = styleSheet,
    )

    private fun createCupMatchOf(cupMatchSource: CompetitionAlgorithmService.CupMatchSource.Match, cup: Cup): Match {
        return Match(cup.startDateTime, "").also { m ->
            m.round = cup.stage.rounds[cupMatchSource.roundOrdinal - 1]
            m.competition = cup
            m.participations = listOf(cupMatchSource.home, cupMatchSource.away).map { cms ->
                participationOf(cms, cup)
            }
        }
    }

    private fun participationOf(cupMatchSource: CompetitionAlgorithmService.CupMatchSource, cup: Cup): MatchParticipation {
        return when (cupMatchSource) {
            is CompetitionAlgorithmService.CupMatchSource.Participant -> {
                FixMatchParticipation().also { fmp ->
                    fmp.competitor = cup.competitors[cupMatchSource.index]
                }
            }
            is CompetitionAlgorithmService.CupMatchSource.Match -> {
                ProceededMatchParticipation().also { pmp ->
                    pmp.matchToWin = createCupMatchOf(cupMatchSource, cup)
                }
            }
        }
    }

    private fun createPlayoffMatchOf(playoffMatchSource: CompetitionAlgorithmService.PlayoffMatchSource.Match, tournament: Tournament): Match {
        return Match(tournament.startDateTime, "").also { m ->
            m.round = tournament.playoffsStage.rounds[playoffMatchSource.roundOrdinal - 1]
            m.competition = tournament
            m.participations = listOf(playoffMatchSource.home, playoffMatchSource.away).map { pms ->
                participationOf(pms, tournament)
            }
        }
    }

    private fun participationOf(playoffMatchSource: CompetitionAlgorithmService.PlayoffMatchSource, tournament: Tournament): MatchParticipation {
        return when (playoffMatchSource) {
            is CompetitionAlgorithmService.PlayoffMatchSource.Quote -> playoffMatchSource.run {
                PlayoffsQuoteMatchParticipation(place, value).also { pqmp ->
                    pqmp.group = tournament.groupStage.groups[group.index]
                }
            }
            is CompetitionAlgorithmService.PlayoffMatchSource.Match -> {
                ProceededMatchParticipation().also { pmp ->
                    pmp.matchToWin = createPlayoffMatchOf(playoffMatchSource, tournament)
                }
            }
        }
    }

}


