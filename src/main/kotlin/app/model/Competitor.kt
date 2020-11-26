package app.model

import org.neo4j.ogm.annotation.NodeEntity
import org.neo4j.ogm.annotation.Relationship

@NodeEntity
class Competitor(
    val name: String,
    val description: String,
) : Entity() {

    @Relationship(COMPETITOR_IN_COMPETITION)
    lateinit var competition: Competition

    @Relationship(COMPETITOR_OF_MATCH_PARTICIPATION)
    lateinit var matchParticipations: List<MatchParticipation>

}