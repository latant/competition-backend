package app.model

import org.neo4j.ogm.annotation.NodeEntity
import java.time.LocalDateTime

@NodeEntity
class Tournament(
    override var name: String,
    override var description: String,
    override var logo: String?,
    override var startDateTime: LocalDateTime,
    override var endDateTime: LocalDateTime,
    override var displayColor: String,
    override val competitorCount: Int,
    override var styleSheet: String,
) : Competition() {

    val playoffsStage get() = stages.filterIsInstance<PlayoffsStage>().single()
    val groupStage get() = stages.filterIsInstance<GroupStage>().single()

}