package app.model

import org.neo4j.ogm.annotation.EndNode
import org.neo4j.ogm.annotation.RelationshipEntity
import org.neo4j.ogm.annotation.StartNode

@RelationshipEntity(PARTICIPANT_IN_MATCH)
class MatchParticipation : Entity() {

    val score: Double? = null
    val place: Int? = null

    @StartNode
    lateinit var participant: MatchParticipant

    @EndNode
    lateinit var match: Match

}