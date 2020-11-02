package app.model

import org.neo4j.ogm.annotation.NodeEntity
import org.neo4j.ogm.annotation.Relationship

@NodeEntity
class Group(val name: String, val description: String) : Entity() {

    @Relationship(MATCH_IN_GROUP, direction = INCOMING)
    lateinit var matches: List<Match>

    @Relationship(PARTICIPANT_IN_GROUP, direction = INCOMING)
    lateinit var participants: List<CompetitionParticipant>

    @Relationship(GROUP_OF_GROUP_STAGE)
    lateinit var groupStage: GroupStage

    @Relationship(PLAYOFFS_QUOTE_FOR_GROUP, direction = INCOMING)
    lateinit var playoffsQuotes: List<PlayoffsQuoteMatchParticipant>

}