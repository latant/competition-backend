package app.model

import org.neo4j.ogm.annotation.NodeEntity
import org.neo4j.ogm.annotation.Relationship
import org.neo4j.ogm.annotation.Relationship.INCOMING

@NodeEntity
class ProceededMatchParticipation : MatchParticipation() {

    @Relationship(IS_MATCH_TO_PROCEED_WITH, direction = INCOMING)
    lateinit var matchToWin: Match

}