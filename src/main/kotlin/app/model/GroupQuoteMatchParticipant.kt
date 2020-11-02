package app.model

import org.neo4j.ogm.annotation.NodeEntity
import org.neo4j.ogm.annotation.Relationship

@NodeEntity
class PlayoffsQuoteMatchParticipant(val place: Int, val value: Float) : MatchParticipant() {

    @Relationship(PLAYOFFS_QUOTE_FOR_GROUP)
    lateinit var group: Group

}