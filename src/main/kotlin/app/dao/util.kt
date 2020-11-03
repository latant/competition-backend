package app.dao

import org.neo4j.ogm.config.Configuration

fun buildOgmConfiguration(build: Configuration.Builder.() -> Unit): Configuration {
    return Configuration.Builder().apply(build).build()
}