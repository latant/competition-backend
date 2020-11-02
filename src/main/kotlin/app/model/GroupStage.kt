package app.model

import org.neo4j.ogm.annotation.NodeEntity
import org.neo4j.ogm.annotation.Relationship

@NodeEntity
class GroupStage(override val roundCount: Int) : CompetitionStage() {

    val tournament get() = competition as Tournament

    @Relationship(GROUP_OF_GROUP_STAGE, direction = INCOMING)
    lateinit var groups: List<Group>

}