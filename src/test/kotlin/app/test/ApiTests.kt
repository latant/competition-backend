package app.test

import app.dao.CompetitionGraph
import app.dto.*
import io.ktor.http.*
import io.ktor.server.testing.*
import org.junit.Before
import org.junit.Test
import java.time.ZonedDateTime

class ApiTests {

    private lateinit var accessTokenResponse: AccessTokenResponse

    @Before
    fun init() {
        CompetitionGraph.session {
            purgeDatabase()
        }
        withApp {
            handleRequest(HttpMethod.Post, "/register") {
                jsonBody(UserRegistrationRequest(name = "Latinovits Antal", email = "latinovitsantal@gmail.com", password = "demo"))
            }
            handleRequest(HttpMethod.Post, "/login") {
                jsonBody(UserLoginRequest(email = "latinovitsantal@gmail.com", password = "demo"))
            }.run {
                accessTokenResponse = responseBody()!!
            }
        }
    }

    private fun TestApplicationRequest.authenticate() {
        addHeader(HttpHeaders.Authorization, "Bearer ${accessTokenResponse.accessToken}")
    }

    @Test
    fun testLeagueCreation() = withApp {
        handleRequest(HttpMethod.Post, "/competitions") {
            authenticate()
            jsonBody<CompetitionCreationRequest>(CompetitionCreationRequest.League(
                name = "My League",
                dateTime = ZonedDateTime.now(),
                participants = listOf(
                    CompetitionCreationRequest.Participant("Eagles"),
                    CompetitionCreationRequest.Participant("Lions"),
                    CompetitionCreationRequest.Participant("Bulls"),
                    CompetitionCreationRequest.Participant("Hornets"),
                    CompetitionCreationRequest.Participant("Wolves"),
                    CompetitionCreationRequest.Participant("Tigers"),
                    CompetitionCreationRequest.Participant("Hawks"),
                    CompetitionCreationRequest.Participant("Sharks"),
                )
            ))
        }.run {
            assert(responseBody<IdResponse>() != null)
        }
    }

    @Test
    fun testCupCreation() = withApp {
        handleRequest(HttpMethod.Post, "/competitions") {
            authenticate()
            jsonBody<CompetitionCreationRequest>(CompetitionCreationRequest.Cup(
                name = "My Cup",
                dateTime = ZonedDateTime.now(),
                participants = listOf(
                    CompetitionCreationRequest.Participant("Eagles"),
                    CompetitionCreationRequest.Participant("Lions"),
                    CompetitionCreationRequest.Participant("Bulls"),
                    CompetitionCreationRequest.Participant("Hornets"),
                    CompetitionCreationRequest.Participant("Wolves"),
                    CompetitionCreationRequest.Participant("Tigers"),
                    CompetitionCreationRequest.Participant("Hawks"),
                    CompetitionCreationRequest.Participant("Sharks"),
                )
            ))
        }.run {
            assert(responseBody<IdResponse>() != null)
        }
    }

    @Test
    fun testTournamentCreation() = withApp {
        handleRequest(HttpMethod.Post, "/competitions") {
            authenticate()
            jsonBody<CompetitionCreationRequest>(CompetitionCreationRequest.Tournament(
                name = "My Tournament",
                dateTime = ZonedDateTime.now(),
                groupCount = 2,
                playOffParticipantCount = 4,
                participants = listOf(
                    CompetitionCreationRequest.Participant("Eagles"),
                    CompetitionCreationRequest.Participant("Lions"),
                    CompetitionCreationRequest.Participant("Bulls"),
                    CompetitionCreationRequest.Participant("Hornets"),
                    CompetitionCreationRequest.Participant("Wolves"),
                    CompetitionCreationRequest.Participant("Tigers"),
                    CompetitionCreationRequest.Participant("Hawks"),
                    CompetitionCreationRequest.Participant("Sharks"),
                )
            ))
        }.run {
            assert(responseBody<IdResponse>() != null)
        }
    }

    @Test
    fun testCompetitionCreations() {
        testLeagueCreation()
        testCupCreation()
        testTournamentCreation()
    }

}