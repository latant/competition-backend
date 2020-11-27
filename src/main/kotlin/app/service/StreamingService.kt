package app.service

import app.dao.CompetitionGraph
import app.error.RequestError
import app.model.Match
import kotlinx.html.*
import org.neo4j.ogm.session.load

object StreamingService {

    fun getHtmlForMatch(id: Long): HTML.() -> Unit {
        val match = CompetitionGraph.readOnlyTransaction { load<Match>(id, depth = 2) ?: RequestError.MatchNotFound() }
        return {
            head {
                link(rel = "stylesheet", href = "/static/match_stream.css")
            }
            body {
                div("logo-container") {
                    img(src = match.competition.logo) {  }
                }
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
                script(src = "/static/match_stream.js") {}
            }
        }
    }

}