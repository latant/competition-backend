package app.test

import app.dao.CompetitionGraph
import app.dto.*
import io.ktor.http.*
import io.ktor.server.testing.*
import io.ktor.util.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.contrib.java.lang.system.EnvironmentVariables
import java.time.ZonedDateTime
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class ApiTests {

    @Rule
    fun environmentVariables(): EnvironmentVariables = EnvironmentVariables()
        .set("JWT_SECRET", "sfdsafsadfdsa32r432f")

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
                accessTokenResponse = response.body()!!
            }
        }
    }

    private fun TestApplicationRequest.authenticate() {
        addHeader(HttpHeaders.Authorization, "Bearer ${accessTokenResponse.accessToken}")
    }

    @Test
    fun testLeagueCreation() = withApp {
        val postResponse = handleRequest(HttpMethod.Post, "/competitions") {
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
        }.response
        assertEquals(HttpStatusCode.OK, postResponse.status())
        val idResponse = postResponse.body<IdResponse>().also(::assertNotNull)!!
        val getResponse = handleRequest(HttpMethod.Get, "/competitions/${idResponse.id}").response
        assertEquals(HttpStatusCode.OK, getResponse.status())
        assertNotNull(getResponse.body<CompetitionResponse>())
        assert(getResponse.body<CompetitionResponse>() is CompetitionResponse.League)
    }

    @Test
    fun testCupCreation() = withApp {
        val postResponse = handleRequest(HttpMethod.Post, "/competitions") {
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
        }.response
        assertEquals(HttpStatusCode.OK, postResponse.status())
        val idResponse = postResponse.body<IdResponse>().also(::assertNotNull)!!
        val getResponse = handleRequest(HttpMethod.Get, "/competitions/${idResponse.id}").response
        assertEquals(HttpStatusCode.OK, getResponse.status())
        assertNotNull(getResponse.body<CompetitionResponse>())
        assert(getResponse.body<CompetitionResponse>() is CompetitionResponse.Cup)
    }

    @Test
    fun testTournamentCreation() = withApp {
        val postResponse = handleRequest(HttpMethod.Post, "/competitions") {
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
        }.response
        assertEquals(HttpStatusCode.OK, postResponse.status())
        val idResponse = postResponse.body<IdResponse>().also(::assertNotNull)!!
        val getResponse = handleRequest(HttpMethod.Get, "/competitions/${idResponse.id}").response
        assertEquals(HttpStatusCode.OK, getResponse.status())
        assertNotNull(getResponse.body<CompetitionResponse>())
        assert(getResponse.body<CompetitionResponse>() is CompetitionResponse.Tournament)
    }

    @Test
    fun testAllMatches() = withApp {
        testLeagueCreation()
        testCupCreation()
        testTournamentCreation()
        val getResponse = handleRequest(HttpMethod.Get,
            "/matches?startDateTime=${ZonedDateTime.now().minusHours(1).toString().urlEncoded()}").response
        assertEquals(HttpStatusCode.OK, getResponse.status())
        assertNotNull(getResponse.body<List<MatchListElementResponse>>())
        assertEquals(28 + 7 + ((6 * 2) + 3), getResponse.body<List<MatchListElementResponse>>()!!.size)
    }

    @Test
    fun testMyMatches() = withApp {
        testAllMatches()
        val getResponse = handleRequest(HttpMethod.Get,
            "/my-matches?startDateTime=${ZonedDateTime.now().minusHours(1).toString().urlEncoded()}") {
            authenticate()
        }.response
        assertEquals(HttpStatusCode.OK, getResponse.status())
        assertNotNull(getResponse.body<List<MatchResponse>>())
        assertEquals(28 + 7 + ((6 * 2) + 3), getResponse.body<List<MatchResponse>>()!!.size)
    }

}