package app.model

import org.neo4j.ogm.annotation.NodeEntity

@NodeEntity
class LeagueStage(override val roundCount: Int) : CompetitionStage() {

    override val name: String? get() = null

    val league get() = competition as League

}