package app.model

import org.neo4j.ogm.annotation.NodeEntity
import java.time.LocalDateTime

@NodeEntity
class League(
    override var name: String,
    override var description: String,
    override var logo: String?,
    override var dateTime: LocalDateTime,
    override var displayColor: String,
    override val participantCount: Int,
) : Competition() {

    val stage get() = stages.single() as LeagueStage

}