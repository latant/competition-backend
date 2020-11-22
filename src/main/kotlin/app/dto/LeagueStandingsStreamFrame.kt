package app.dto

import kotlinx.serialization.Serializable

@Serializable
data class LeagueStandingsStreamFrame(
    val name: String,
    val standingsTable: StandingsTable,
)