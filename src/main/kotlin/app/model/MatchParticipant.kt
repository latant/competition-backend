package app.model

import org.neo4j.ogm.annotation.NodeEntity
import org.neo4j.ogm.annotation.Relationship

@NodeEntity
abstract class MatchParticipant : Entity() {

    @Relationship(PARTICIPANT_IN_MATCH)
    lateinit var match: Match

    @Relationship(IS_MATCH_PARTICIPANT, direction = INCOMING)
    var competitionParticipant: CompetitionParticipant? = null

}