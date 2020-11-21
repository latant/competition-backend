package app.model

import org.neo4j.ogm.annotation.NodeEntity
import org.neo4j.ogm.annotation.Relationship
import org.neo4j.ogm.annotation.Relationship.INCOMING

@NodeEntity
abstract class MatchParticipation : Entity() {

    var score: Double? = null

    @Relationship(PARTICIPATION_IN_MATCH)
    lateinit var match: Match

    @Relationship(PARTICIPATE_IN_MATCH_PARTICIPATION, direction = INCOMING)
    var competitor: Competitor? = null

}