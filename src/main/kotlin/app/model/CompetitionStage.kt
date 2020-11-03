package app.model

import org.neo4j.ogm.annotation.NodeEntity
import org.neo4j.ogm.annotation.Relationship

@NodeEntity
abstract class CompetitionStage : Entity() {

    abstract val name: String?
    abstract val roundCount: Int

    @Relationship(STAGE_OF_COMPETITION)
    lateinit var competition: Competition

    @Relationship(ROUND_OF_STAGE, direction = INCOMING)
    lateinit var rounds: List<Round>

}