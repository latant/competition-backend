package app.model

import org.neo4j.ogm.annotation.NodeEntity
import java.time.LocalDateTime

@NodeEntity
class League(
    override val name: String,
    override val description: String,
    override val logo: ByteArray?,
    override val dateTime: LocalDateTime,
    override val displayColor: String,
    override val participantCount: Int,
) : Competition() {

    val stage get() = stages.single() as LeagueStage

}