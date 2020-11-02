package app.model

import org.neo4j.ogm.annotation.NodeEntity

@NodeEntity
class LeagueStage(override val roundCount: Int) : CompetitionStage() {

    val league get() = competition as League

}