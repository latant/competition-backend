package app.dto

import app.model.Match
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.time.LocalDateTime

@Serializable
data class MatchUpdateRequest(
    val dateTime: @Contextual LocalDateTime? = null,
    val description: String? = null,
    val state: Match.State? = null,
    val scores: List<ScoreUpdate>? = null,
) {
    @Serializable
    data class ScoreUpdate(val competitorId: Long, val score: Double)
}