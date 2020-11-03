package app.model

import org.neo4j.ogm.annotation.NodeEntity
import org.neo4j.ogm.annotation.Relationship

@NodeEntity
class PlayoffsStage(
    override val roundCount: Int,
    override val name: String? = null,
) : CompetitionStage() {

    @Relationship(FINAL_MATCH_OF_PLAYOFFS, direction = INCOMING)
    lateinit var finalMatch: Match

}