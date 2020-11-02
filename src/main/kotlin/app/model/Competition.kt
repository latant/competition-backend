package app.model

import org.neo4j.ogm.annotation.NodeEntity
import org.neo4j.ogm.annotation.Relationship
import java.time.LocalDateTime

@NodeEntity
abstract class Competition : Entity() {

    abstract val name: String
    abstract val description: String
    abstract val logo: ByteArray?
    abstract val dateTime: LocalDateTime
    abstract val displayColor: String
    abstract val participantCount: Int

    val state = State.NOT_STARTED_YET

    @Relationship(PARTICIPANT_IN_COMPETITION, direction = INCOMING)
    lateinit var participants: List<CompetitionParticipant>

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