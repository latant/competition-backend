package app.model

import org.neo4j.ogm.annotation.NodeEntity
import org.neo4j.ogm.annotation.Relationship

@NodeEntity
class CompetitionParticipant(
    val name: String,
    val description: String,
) : Entity() {

    @Relationship(PARTICIPANT_IN_COMPETITION)
    lateinit var competition: Competition

    @Relationship(PARTICIPATE_IN_MATCH_PARTICIPATION)
    lateinit var matchParticipations: List<MatchParticipation>

}