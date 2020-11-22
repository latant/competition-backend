package app.service

import app.dao.CompetitionGraph
import app.dto.GroupStandingsStreamFrame
import app.dto.LeagueStandingsStreamFrame
import app.dto.MatchStreamFrame
import app.error.RequestError
import app.model.Match
import kotlinx.html.HTML
import kotlinx.html.body
import kotlinx.html.div
import kotlinx.html.script
import org.neo4j.ogm.session.load

object StreamingService {

    fun getHtmlForMatch(id: Long): HTML.() -> Unit {
        val match = CompetitionGraph.readOnlyTransaction { load<Match>(id, depth = 2) ?: RequestError.MatchNotFound() }
        return {
            body {
                div("container") {
                    div("logo-container") {  }
                    div("participants") {
                        match.participations.forEach { p ->
                            div("participant") {
                                div("participant-name") {
                                    text(p.competitor?.name ?: "-")
                                }
                                div("participant-score") {
                                    p.score?.let { text(it) }
                                }
                            }
                        }
                    }
                }
                script(src = "/static/match_stream.js") {}
            }
        }
    }

    suspend fun feedMatchStream(id: Long, sendFrame: suspend (MatchStreamFrame) -> Unit) {
        SubscriptionService.subscribeForMatch(id) { m ->
            val frame = MatchStreamFrame(
                participants = m.participations.map { MatchStreamFrame.Participant(it.competitor?.name, it.score) },
            )
            sendFrame(frame)
        }
    }

    suspend fun feedGroupStandingsStream(id: Long, sendFrame: suspend (GroupStandingsStreamFrame) -> Unit) {
        SubscriptionService.subscribeForGroup(id) { g ->
            val frame = GroupStandingsStreamFrame(
                name = g.name,
                standingsTable = CompetitionRetrievalService.standingsTable(g.matches.toSet(), g.competitors),
            )
            sendFrame(frame)
        }
    }

    suspend fun feedLeagueStandingsStream(id: Long, sendFrame: suspend (LeagueStandingsStreamFrame) -> Unit) {
        SubscriptionService.subscribeForLeague(id) { l ->
            val frame = LeagueStandingsStreamFrame(
                name = l.name,
                standingsTable = CompetitionRetrievalService.standingsTable(l.matches.toSet(), l.competitors),
            )
            sendFrame(frame)
        }
    }

}