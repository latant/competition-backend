package app.model

import org.neo4j.ogm.annotation.NodeEntity
import org.neo4j.ogm.annotation.Relationship
import org.neo4j.ogm.annotation.Relationship.INCOMING

@NodeEntity
class Group(var name: String, val description: String) : Entity() {

    @Relationship(MATCH_IN_GROUP, direction = INCOMING)
    lateinit var matches: List<Match>

    @Relationship(PARTICIPANT_IN_GROUP, direction = INCOMING)
    lateinit var competitors: List<Competitor>

    @Relationship(GROUP_OF_GROUP_STAGE)
    lateinit var groupStage: GroupStage

    @Relationship(PLAYOFFS_QUOTE_FOR_GROUP, direction = INCOMING)
    lateinit var playoffsQuotes: List<PlayoffsQuoteMatchParticipation>

}