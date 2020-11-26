package app.dto

import app.validation.*
import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.time.ZonedDateTime

@Serializable
sealed class CompetitionCreationRequest {

    abstract fun validate()

    @Serializable
    data class Competitor(val name: String, val description: String = "")

    @Serializable
    @SerialName("League")
    data class League(
        val name: String,
        val dateTime: @Contextual ZonedDateTime,
        val displayColor: String = "#0000ff",
        val description: String = "",
        val competitors: List<Competitor>,
        val roundCount: Int? = null,
        val styleSheet: String = "",
    ) : CompetitionCreationRequest() {
        override fun validate() = validations {
            name.requireNotBlank { "Name must not be blank" }
            displayColor.requireValidCssHexColor { "The display color must be a valid css color in hex format" }
            competitors.requireMultiple { "There must be at least 2 competitors" }
            competitors.forEach { it.name.requireNotBlank { "Competitors name must not be blank" } }
            roundCount?.requirePositive { "Round count must be positive" }
        }
    }

    @Serializable
    @SerialName("Cup")
    data class Cup(
        val name: String,
        val dateTime: @Contextual ZonedDateTime,
        val displayColor: String = "#0000ff",
        val description: String = "",
        val competitors: List<Competitor>,
        val styleSheet: String = "",
    ) : CompetitionCreationRequest() {
        override fun validate() = validations {
            name.requireNotBlank { "Name must not be blank" }
            displayColor.requireValidCssHexColor { "The display color must be a valid css color in hex format" }
            competitors.requireMultiple { "There must be at least 2 competitors" }
            competitors.forEach { it.name.requireNotBlank { "Competitors name must not be blank" } }
        }
    }

    @Serializable
    @SerialName("Tournament")
    data class Tournament(
        val name: String,
        val dateTime: @Contextual ZonedDateTime,
        val displayColor: String = "#0000ff",
        val description: String = "",
        val competitors: List<Competitor>,
        val groupCount: Int,
        val playoffsCompetitorCount: Int,
        val styleSheet: String = "",
    ) : CompetitionCreationRequest() {
        override fun validate() = validations {
            name.requireNotBlank { "Name must not be blank" }
            displayColor.requireValidCssHexColor { "The display color must be a valid css color in hex format" }
            competitors.requireMultiple { "There must be at least 2 competitors" }
            competitors.forEach { it.name.requireNotBlank { "Competitors name must not be blank" } }
            groupCount.requirePositive { "Group count must be positive" }
            playoffsCompetitorCount.requireMultiple { "There must be multiple competitors in the playoffs" }
            require(playoffsCompetitorCount < competitors.size) { "There must be playoff competitors then all competitors" }
            require(groupCount < competitors.size) { "There must be fewer groups then competitors" }
        }
    }

}