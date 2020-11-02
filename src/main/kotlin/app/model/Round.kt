package app.model

import org.neo4j.ogm.annotation.NodeEntity
import org.neo4j.ogm.annotation.Relationship

@NodeEntity
class Round(
    val name: String,
    val ordinal: Int,
    val description: String,
) : Entity() {

    @Relationship(ROUND_OF_STAGE)
    lateinit var stage: CompetitionStage

    @Relationship(MATCH_IN_ROUND, direction = INCOMING)
    lateinit var matches: List<Match>

}