package app.model

import org.neo4j.ogm.annotation.NodeEntity
import org.neo4j.ogm.annotation.Relationship

@NodeEntity
class CompetitionParticipant(
    val name: String,
    val description: String,
) : MatchParticipant() {

    @Relationship(PARTICIPANT_IN_COMPETITION)
    lateinit var competition: Competition

    @Relationship(IS_MATCH_PARTICIPANT)
    lateinit var matchParticipants: List<MatchParticipant>

}