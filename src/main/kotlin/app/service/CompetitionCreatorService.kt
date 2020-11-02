package app.service

import app.dao.CompetitionGraph
import app.dao.load
import app.dto.CompetitionCreation
import app.model.*
import app.security.UserPrincipal
import atUTC
import org.neo4j.ogm.session.Session

object CompetitionCreatorService {

    fun createCompetition(userPrincipal: UserPrincipal, competitionCreation: CompetitionCreation): Long {
        return CompetitionGraph.readWriteTransaction {
            val user = load<User>(userPrincipal.id, 1)!!
            when (competitionCreation) {
                is CompetitionCreation.League -> createLeague(user, competitionCreation)
                is CompetitionCreation.Cup -> createCup(user, competitionCreation)
                is CompetitionCreation.Tournament -> createTournament(user, competitionCreation)
            }
        }
    }

    private fun Session.createLeague(creator: User, leagueCreation: CompetitionCreation.League): Long {
        val league = leagueCreation.toNode()
        league.creator = creator
        league.participants = leagueCreation.participants.map { it.toNode() }
        val scheduling = CompetitionAlgorithmService.leagueScheduling(league.participantCount).shuffled()
        league.stages = listOf(LeagueStage(scheduling.size))
            .let { s -> leagueCreation.roundCount?.let { s.take(it) } ?: s }
        val participantsShuffled = league.participants.shuffled()
        league.stage.rounds = scheduling.mapIndexed { i, pairs ->
            Round("Round ${i + 1}", i + 1, "").also { r ->
                r.matches = pairs.mapIndexed { j, (a, b) ->
                    Match(league.dateTime, "").also { m ->
                        m.competition = league
                        m.participations = listOf(a, b).map { pi ->
                            MatchParticipation().also { p ->
                                p.match = m
                                p.participant = FixMatchParticipant().also { fmp ->
                                    fmp.competitionParticipant = participantsShuffled[pi]
                                }
                            }
                        }
                    }
                }
            }
        }
        save(league)
        return league.id!!
    }

    private fun Session.createCup(creator: User, cupCreation: CompetitionCreation.Cup): Long {
        val cup = cupCreation.toNode()
        cup.creator = creator
        cup.participants = cupCreation.participants.map { it.toNode() }
        val finalMatchSource = CompetitionAlgorithmService.cupMatchSource(cup.participants.indices)
            as CompetitionAlgorithmService.CupMatchSource.Match
        cup.stages = listOf(PlayoffsStage(roundCount = finalMatchSource.roundOrdinal))
        cup.stage.run {
            rounds = (1..roundCount).map { Round("Round $it", it, "") }
            finalMatch = createCupMatchOf(finalMatchSource, cup)
        }
        save(cup)
        return cup.id!!
    }

    private fun Session.createTournament(creator: User, tournamentCreation: CompetitionCreation.Tournament): Long {
        val tournament = tournamentCreation.toNode()
        tournament.creator = creator
        tournament.participants = tournamentCreation.participants.map { it.toNode() }.shuffled()
        val groupsStructure = CompetitionAlgorithmService
            .groupsWithCount(tournament.participants, tournamentCreation.groupCount)
            .map { it to CompetitionAlgorithmService.leagueScheduling(it.size) }
        val groupStage = GroupStage(groupsStructure.maxOfOrNull { (_, sch) -> sch.size }!!).also { gs ->
            gs.rounds = (1..gs.roundCount).map { Round("Group stage Round $it", it, "") }
            gs.groups = groupsStructure.mapIndexed { gi, (gps, gsch) ->
                Group("Group ${gi + 1}", "").also { g ->
                    g.participants = gps
                    g.matches = gsch.flatMapIndexed { ri, pairs ->
                        pairs.map { (a, b) ->
                            Match(tournament.dateTime, "").also { m ->
                                m.competition = tournament
                                m.round = gs.rounds[ri]
                                m.participations = listOf(a, b).map { pi ->
                                    MatchParticipation().also { mp ->
                                        mp.match = m
                                        mp.participant = FixMatchParticipant().also { fmp ->
                                            fmp.competitionParticipant = tournament.participants[pi]
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        val playoffsFinalSource = CompetitionAlgorithmService
            .playOffs(groupsStructure.map { it.first }, tournamentCreation.playOffParticipantCount)
            as CompetitionAlgorithmService.PlayoffMatchSource.Match
        val playoffsStage = PlayoffsStage(roundCount = playoffsFinalSource.roundOrdinal).also { pst ->
            pst.rounds = (1..pst.roundCount).map { Round("Playoffs Round $it", it, "") }
        }
        tournament.stages = listOf(groupStage, playoffsStage)
        playoffsStage.finalMatch = createPlayoffMatchOf(playoffsFinalSource, tournament)
        save(tournament)
        return tournament.id!!
    }

    private fun CompetitionCreation.Participant.toNode() = CompetitionParticipant(name, description)

    private fun CompetitionCreation.League.toNode() = League(
        name = name,
        description = description,
        logo = null,
        dateTime = dateTime.atUTC(),
        displayColor = displayColor,
        participantCount = participants.size
    )

    private fun CompetitionCreation.Cup.toNode() = Cup(
        name = name,
        description = description,
        dateTime = dateTime.atUTC(),
        logo = null,
        displayColor = displayColor,
        participantCount = participants.size
    )

    private fun CompetitionCreation.Tournament.toNode() = Tournament(
        name = name,
        description = description,
        logo = null,
        dateTime = dateTime.atUTC(),
        displayColor = displayColor,
        participantCount = participants.size
    )

    private fun createCupMatchOf(cupMatchSource: CompetitionAlgorithmService.CupMatchSource.Match, cup: Cup): Match {
        return Match(cup.dateTime, "").also { m ->
            m.round = cup.stage.rounds[cupMatchSource.roundOrdinal - 1]
            m.competition = cup
            m.participations = listOf(cupMatchSource.home, cupMatchSource.away).map { cms ->
                MatchParticipation().also { mp ->
                    mp.match = m
                    mp.participant = participantOf(cms, cup)
                }
            }
        }
    }

    private fun participantOf(cupMatchSource: CompetitionAlgorithmService.CupMatchSource, cup: Cup): MatchParticipant {
        return when (cupMatchSource) {
            is CompetitionAlgorithmService.CupMatchSource.Participant -> {
                FixMatchParticipant().also { fmp ->
                    fmp.competitionParticipant = cup.participants[cupMatchSource.index]
                }
            }
            is CompetitionAlgorithmService.CupMatchSource.Match -> {
                ProceededMatchParticipant().also { pmp ->
                    pmp.matchToWin = createCupMatchOf(cupMatchSource, cup)
                }
            }
        }
    }

    private fun createPlayoffMatchOf(playoffMatchSource: CompetitionAlgorithmService.PlayoffMatchSource.Match, tournament: Tournament): Match {
        return Match(tournament.dateTime, "").also { m ->
            m.round = tournament.playoffsStage.rounds[playoffMatchSource.roundOrdinal - 1]
            m.competition = tournament
            m.participations = listOf(playoffMatchSource.home, playoffMatchSource.away).map { pms ->
                MatchParticipation().also { mp ->
                    mp.match = m
                    mp.participant = participantOf(pms, tournament)
                }
            }
        }
    }

    private fun participantOf(playoffMatchSource: CompetitionAlgorithmService.PlayoffMatchSource, tournament: Tournament): MatchParticipant {
        return when (playoffMatchSource) {
            is CompetitionAlgorithmService.PlayoffMatchSource.Quote -> playoffMatchSource.run {
                PlayoffsQuoteMatchParticipant(place, value).also { pqmp ->
                    pqmp.group = tournament.groupStage.groups[group.index]
                }
            }
            is CompetitionAlgorithmService.PlayoffMatchSource.Match -> {
                ProceededMatchParticipant().also { pmp ->
                    pmp.matchToWin = createPlayoffMatchOf(playoffMatchSource, tournament)
                }
            }
        }
    }

}


