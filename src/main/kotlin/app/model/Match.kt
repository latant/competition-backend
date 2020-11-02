package app.model

import org.neo4j.ogm.annotation.NodeEntity
import org.neo4j.ogm.annotation.Relationship
import java.time.LocalDateTime

@NodeEntity
class Match(
    val dateTime: LocalDateTime,
    val description: String,
): Entity() {

    val state = State.NOT_STARTED_YET

    @Relationship(MATCH_IN_COMPETITION)
    lateinit var competition: Competition

    @Relationship(EDITOR_OF_MATCH, direction = INCOMING)
    lateinit var editors: List<User>

    @Relationship(MATCH_IN_ROUND, direction = INCOMING)
    lateinit var round: Round

    @Relationship(PARTICIPANT_IN_MATCH, direction = INCOMING)
    lateinit var participations: List<MatchParticipation>


    enum class State {
        NOT_STARTED_YET,
        ONGOING,
        ENDED
    }

}