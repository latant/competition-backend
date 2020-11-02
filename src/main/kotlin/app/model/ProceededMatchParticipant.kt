package app.model

import org.neo4j.ogm.annotation.NodeEntity
import org.neo4j.ogm.annotation.Relationship

@NodeEntity
class ProceededMatchParticipant : MatchParticipant() {

    @Relationship(IS_MATCH_TO_PROCEED_WITH, direction = INCOMING)
    lateinit var matchToWin: Match

}