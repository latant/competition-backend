package app.model

import org.neo4j.ogm.annotation.NodeEntity
import org.neo4j.ogm.annotation.Relationship
import org.neo4j.ogm.annotation.Relationship.INCOMING
import java.time.LocalDateTime

@NodeEntity
class Match(
    var dateTime: LocalDateTime,
    var description: String,
): Entity() {

    var state = State.NOT_STARTED_YET
    var endDateTime: LocalDateTime? = null

    @Relationship(MATCH_IN_COMPETITION)
    lateinit var competition: Competition

    @Relationship(EDITOR_OF_MATCH, direction = INCOMING)
    var editors: List<User>? = null

    @Relationship(MATCH_IN_ROUND)
    lateinit var round: Round

    @Relationship(PARTICIPATION_IN_MATCH, direction = INCOMING)
    lateinit var participations: List<MatchParticipation>

    @Relationship(MATCH_IN_GROUP)
    val group: Group? = null

    enum class State {
        NOT_STARTED_YET,
        ONGOING,
        ENDED,
    }

}