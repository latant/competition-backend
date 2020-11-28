package app.service

import app.dao.CompetitionGraph
import app.error.RequestError
import app.model.*
import kotlinx.html.*
import org.neo4j.ogm.session.load

object StreamingService {

    fun getHtmlForMatch(id: Long): HTML.() -> Unit {
        val match = CompetitionGraph.readOnlyTransaction { load<Match>(id, depth = 3) ?: RequestError.MatchNotFound() }
        return match.competition.streamHtml("match") {
            div("match-logo") {
                img(src = match.competition.logo) { }
            }
            div("match-participants") {
                match.participations.forEach { p ->
                    div("match-participant") {
                        div("match-participant-name") {
                            text(p.competitor?.name ?: "-")
                        }
                        div("match-participant-score") {
                            p.score?.let { text(it) }
                        }
                    }
                }
            }
        }
    }

    fun getHtmlForCompetitionStandings(id: Long): HTML.() -> Unit {
        val competition = CompetitionGraph.readOnlyTransaction {
            load<Competition>(id, depth = 4) ?: RequestError.CompetitionNotFound()
        }
        return when (competition) {
            is League -> competition.leagueStandingsHtml()
            is Cup -> competition.cupStandingsHtml()
            is Tournament -> competition.tournamentStandingsHtml()
            else -> error("Unsupported competition type '${competition::class.simpleName}")
        }
    }

    private fun League.leagueStandingsHtml() = streamHtml("league-standing") {
        div("league-standing-name") {
            text(name)
        }
        div("league-standing-logo") {
            img(src = logo)
        }
        table("league-standing-table") {
            tr("league-standing-table-head") {
                th(classes = "league-standing-table-head-place") {
                    text("#")
                }
                th(classes = "league-standing-table-head-name") {

                }
                th(classes = "league-standing-table-head-matches-won") {
                    text("W")
                }
                th(classes = "league-standing-table-head-scores") {
                    text("S")
                }
                th(classes = "league-standing-table-head-matches-played") {
                    text("M")
                }
            }
            CompetitionRetrievalService.standingsTable(matches.toSet(), competitors).records.forEach { r ->
                tr("league-standings-table-record") {
                    td(classes = "league-standing-table-record-place") {
                        text(r.place)
                    }
                    td(classes = "league-standing-table-record-name") {
                        text(r.competitorName)
                    }
                    td(classes = "league-standing-table-record-matches-won") {
                        text(r.wins)
                    }
                    td(classes = "league-standing-table-record-scores") {
                        text(r.scores)
                    }
                    td(classes = "league-standing-table-record-matches-played") {
                        text("${r.matchesPlayed}/${r.matchesCount}")
                    }
                }
            }
        }
    }

    private fun Cup.cupStandingsHtml() = streamHtml("cup-standing") {
        div("cup-standing-name") {
            text(name)
        }
        div("cup-standing-logo") {
            img(src = logo)
        }
        div("cup-standing-matches") {
            val displayedMatches = matches.filter { m ->
                m.state != Match.State.ENDED &&
                    m.participations.any { it.competitor != null }
            }
            displayedMatches.forEach { m ->
                div("cup-standing-match") {
                    m.participations.forEach { p ->
                        div("cup-standing-match-participant") {
                            div("cup-standing-match-participant-name") {
                                text(p.competitor?.name ?: "-")
                            }
                            div("cup-standing-match-participant-score") {
                                p.score?.let { text(it) }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun Tournament.tournamentStandingsHtml() = streamHtml("tournament-standing") {
        div("tournament-standing-name") {
            text(name)
        }
        div("tournament-standing-logo") {
            img(src = logo)
        }
        div("tournament-standing-groups") {
            groupStage.groups.forEach { g ->
                div("tournament-standing-group") {
                    div("tournament-standing-group-name") {
                        text(g.name)
                    }
                    table("tournament-standing-group-table") {
                        tr("tournament-standing-group-table-head") {
                            th(classes = "tournament-standing-group-table-head-place") {
                                text("#")
                            }
                            th(classes = "tournament-standing-group-table-head-name") {

                            }
                            th(classes = "tournament-standing-group-table-head-matches-won") {
                                text("W")
                            }
                            th(classes = "tournament-standing-group-table-head-scores") {
                                text("S")
                            }
                            th(classes = "tournament-standing-group-table-head-matches-played") {
                                text("M")
                            }
                        }
                        CompetitionRetrievalService.standingsTable(g.matches.toSet(), g.competitors).records.forEach { r ->
                            tr("tournament-standing-group-table-record") {
                                td(classes = "tournament-standing-group-table-record-place") {
                                    text(r.place)
                                }
                                td(classes = "tournament-standing-group-table-record-name") {
                                    text(r.competitorName)
                                }
                                td(classes = "tournament-standing-group-table-record-matches-won") {
                                    text(r.wins)
                                }
                                td(classes = "tournament-standing-group-table-record-scores") {
                                    text(r.scores)
                                }
                                td(classes = "tournament-standing-group-table-record-matches-played") {
                                    text("${r.matchesPlayed}/${r.matchesCount}")
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun Competition.streamHtml(name: String, body: BODY.() -> Unit): HTML.() -> Unit {
        return {
            head {
                link(rel = "stylesheet", href = "/static/stream/$name.css")
                link(rel = "stylesheet", href = "/competitions/${this@streamHtml.id}/stylesheet-base")
                link(rel = "stylesheet", href = "/competitions/${this@streamHtml.id}/stylesheet")
            }
            body("$name-frame") {
                body()
                script(src = "/static/stream/$name.js") {}
            }
        }
    }

}