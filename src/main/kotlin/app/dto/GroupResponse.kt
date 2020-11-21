package app.dto

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.time.LocalDateTime

@Serializable
data class GroupResponse(
    val id: Long,
    val name: String,
    val matches: List<Match>,
    val competitors: List<Competitor>,
    val standingsTable: StandingsTable,
    val competitionId: Long,
    val competitionName: String,
    val playoffsQuotes: List<PlayoffsQuote>,
) {

    @Serializable
    data class PlayoffsQuote(
        val place: Int,
        val matchId: Long,
    )

    @Serializable
    data class Competitor(
        val id: Long,
        val name: String,
        val description: String,
    )

    @Serializable
    data class Match(
        val id: Long,
        val dateTime: @Contextual LocalDateTime,
        val participants: List<Participant>
    ) {

        @Serializable
        data class Participant(
            val competitorId: Long,
            val score: Double?,
        )
    }
}