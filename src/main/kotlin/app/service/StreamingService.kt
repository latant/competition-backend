package app.service

import app.dao.CompetitionGraph
import app.dto.ActualMatchesStreamFrame
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

}