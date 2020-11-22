package app.dto

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.time.LocalDateTime

@Serializable
data class ActualMatchesStreamFrame(
    val competitionName: String,
    val matches: List<Match>,
) {

    @Serializable
    data class Match(
        val dateTime: @Contextual LocalDateTime,
        val participants: List<Participant>,
    ) {

        @Serializable
        data class Participant(
            val name: String,
            val score: Double?,
        )
    }
}