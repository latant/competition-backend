package app.test

import app.dao.CompetitionGraph
import app.dto.*
import io.ktor.http.*
import io.ktor.server.testing.*
import org.junit.Before
import org.junit.Test
import java.time.ZonedDateTime

class ApiTests {

    private lateinit var accessToken: AccessToken

    @Before
    fun init() {
        CompetitionGraph.session {
            purgeDatabase()
        }
        withApp {
            handleRequest(HttpMethod.Post, "/register") {
                jsonBody(UserRegistration(name = "Latinovits Antal", email = "latinovitsantal@gmail.com", password = "demo"))
            }
            handleRequest(HttpMethod.Post, "/login") {
                jsonBody(UserLogin(email = "latinovitsantal@gmail.com", password = "demo"))
            }.run {
                accessToken = responseBody()!!
            }
        }
    }

    private fun TestApplicationRequest.authenticate() {
        addHeader(HttpHeaders.Authorization, "Bearer ${accessToken.accessToken}")
    }

    @Test
    fun testLeagueCreation() = withApp {
        handleRequest(HttpMethod.Post, "/competitions") {
            authenticate()
            jsonBody<CompetitionCreation>(CompetitionCreation.League(
                name = "My League",
                dateTime = ZonedDateTime.now(),
                participants = listOf(
                    CompetitionCreation.Participant("Eagles"),
                    CompetitionCreation.Participant("Lions"),
                    CompetitionCreation.Participant("Bulls"),
                    CompetitionCreation.Participant("Hornets"),
                    CompetitionCreation.Participant("Wolves"),
                    CompetitionCreation.Participant("Tigers"),
                    CompetitionCreation.Participant("Hawks"),
                    CompetitionCreation.Participant("Sharks"),
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
            jsonBody<CompetitionCreation>(CompetitionCreation.Cup(
                name = "My Cup",
                dateTime = ZonedDateTime.now(),
                participants = listOf(
                    CompetitionCreation.Participant("Eagles"),
                    CompetitionCreation.Participant("Lions"),
                    CompetitionCreation.Participant("Bulls"),
                    CompetitionCreation.Participant("Hornets"),
                    CompetitionCreation.Participant("Wolves"),
                    CompetitionCreation.Participant("Tigers"),
                    CompetitionCreation.Participant("Hawks"),
                    CompetitionCreation.Participant("Sharks"),
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
            jsonBody<CompetitionCreation>(CompetitionCreation.Tournament(
                name = "My Tournament",
                dateTime = ZonedDateTime.now(),
                groupCount = 2,
                playOffParticipantCount = 4,
                participants = listOf(
                    CompetitionCreation.Participant("Eagles"),
                    CompetitionCreation.Participant("Lions"),
                    CompetitionCreation.Participant("Bulls"),
                    CompetitionCreation.Participant("Hornets"),
                    CompetitionCreation.Participant("Wolves"),
                    CompetitionCreation.Participant("Tigers"),
                    CompetitionCreation.Participant("Hawks"),
                    CompetitionCreation.Participant("Sharks"),
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