package app.dto

import kotlinx.serialization.Serializable

@Serializable
data class MatchStreamFrame(val participants: List<Participant>) {

    @Serializable
    data class Participant(
        val name: String?,
        val score: Double?,
    )
}