package app.model

import org.neo4j.ogm.annotation.NodeEntity
import java.time.LocalDateTime

@NodeEntity
class Cup(
    override var name: String,
    override var description: String,
    override var logo: String?,
    override var startDateTime: LocalDateTime,
    override var endDateTime: LocalDateTime,
    override var displayColor: String,
    override val competitorCount: Int,
    override var styleSheet: String,
) : Competition() {

    val stage get() = stages.single() as PlayoffsStage

}