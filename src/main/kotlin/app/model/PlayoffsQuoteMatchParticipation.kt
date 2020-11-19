package app.model

import org.neo4j.ogm.annotation.NodeEntity
import org.neo4j.ogm.annotation.Relationship

@NodeEntity
class PlayoffsQuoteMatchParticipation(val groupPlace: Int, val value: Float) : MatchParticipation() {

    @Relationship(PLAYOFFS_QUOTE_FOR_GROUP)
    lateinit var group: Group

}