package app.model

import org.neo4j.ogm.annotation.NodeEntity
import org.neo4j.ogm.annotation.Relationship
import org.neo4j.ogm.annotation.Relationship.INCOMING
import java.time.LocalDateTime

@NodeEntity
abstract class Competition : Entity() {

    abstract var name: String
    abstract var description: String
    abstract var logo: String?
    abstract var dateTime: LocalDateTime
    abstract var displayColor: String
    abstract val participantCount: Int

    val state = State.NOT_STARTED_YET

    @Relationship(PARTICIPANT_IN_COMPETITION, direction = INCOMING)
    lateinit var participants: List<Competitor>

    @Relationship(MATCH_IN_COMPETITION, direction = INCOMING)
    lateinit var matches: List<Match>

    @Relationship(CREATOR_OF_COMPETITION, direction = INCOMING)
    lateinit var creator: User

    @Relationship(STAGE_OF_COMPETITION, direction = INCOMING)
    lateinit var stages: List<CompetitionStage>

    enum class State {
        NOT_STARTED_YET,
        ONGOING,
        ENDED,
    }

}