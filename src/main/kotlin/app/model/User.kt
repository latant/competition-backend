package app.model

import org.neo4j.ogm.annotation.NodeEntity

@NodeEntity
class User(
    val name: String,
    val email: String,
    val password: String,
) : Entity()