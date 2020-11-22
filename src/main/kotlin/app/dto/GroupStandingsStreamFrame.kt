package app.dto

import kotlinx.serialization.Serializable

@Serializable
data class GroupStandingsStreamFrame(
    val name: String,
    val standingsTable: StandingsTable,
)