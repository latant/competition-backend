package app.dto

import kotlinx.serialization.Serializable

@Serializable
data class StandingsTable(val records: List<Record>) {

    @Serializable
    data class Record(
        val place: Int,
        val competitorId: Long,
        val competitorName: String,
        val wins: Int,
        val scores: Double,
        val matchesCount: Int,
        val matchesPlayed: Int,
    )

}