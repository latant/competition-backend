package app.model

import org.neo4j.ogm.annotation.NodeEntity
import org.neo4j.ogm.annotation.Relationship

@NodeEntity
class User(
    val name: String,
    val email: String,
    val password: String,
) : Entity() {

    @Relationship(CREATOR_OF_COMPETITION)
    lateinit var createdCompetitions: List<Competition>

}