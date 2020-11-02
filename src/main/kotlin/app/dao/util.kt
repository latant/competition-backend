package app.dao

import org.neo4j.ogm.config.Configuration
import org.neo4j.ogm.cypher.Filter
import org.neo4j.ogm.session.Session

fun buildOgmConfiguration(build: Configuration.Builder.() -> Unit): Configuration {
    return Configuration.Builder().apply(build).build()
}

inline fun <reified E> Session.load(id: Long, depth: Int): E? = load(E::class.java, id, depth)
inline fun <reified E> Session.loadAll(filter: Filter, depth: Int): Collection<E> = loadAll(E::class.java, filter, depth)