package app.model

import org.neo4j.ogm.annotation.NodeEntity
import java.time.LocalDateTime

@NodeEntity
class Tournament(
    override val name: String,
    override val description: String,
    override val logo: ByteArray?,
    override val dateTime: LocalDateTime,
    override val displayColor: String,
    override val participantCount: Int
) : Competition() {

    val playoffsStage get() = stages.filterIsInstance<PlayoffsStage>().single()
    val groupStage get() = stages.filterIsInstance<GroupStage>().single()

}