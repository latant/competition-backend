package app.test

import app.dao.CompetitionGraph
import app.dto.*
import com.github.javafaker.Faker
import io.ktor.http.*
import io.ktor.server.testing.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.contrib.java.lang.system.EnvironmentVariables
import java.time.ZonedDateTime
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class ApiTests {

    @Rule
    fun environmentVariables(): EnvironmentVariables = EnvironmentVariables()
        .set("JWT_SECRET", "sfdsafsadfdsa32r432f")

    private val faker = Faker()
    private lateinit var accessTokenResponse1: AccessTokenResponse
    private lateinit var accessTokenResponse2: AccessTokenResponse

    private fun registerUser(): AccessTokenResponse = withApp {
        val name = faker.name().name()
        val email = "${faker.name().username()}@gmail.com"
        val password = faker.letterify("???????")
        handleRequest(HttpMethod.Post, "/register") {
            jsonBody(UserRegistrationRequest(name, email, password))
        }
        handleRequest(HttpMethod.Post, "/login") {
            jsonBody(UserLoginRequest(email, password))
        }.response.body()!!
    }

    @Before
    fun init() {
        CompetitionGraph.session {
            purgeDatabase()
        }
        accessTokenResponse1 = registerUser()
        accessTokenResponse2 = registerUser()
    }

    private fun TestApplicationRequest.authenticate(accessTokenResponse: AccessTokenResponse) {
        addHeader(HttpHeaders.Authorization, "Bearer ${accessTokenResponse.accessToken}")
    }

    @Test
    fun testLeagueCreation() {
        withApp {
            val postResponse = handleRequest(HttpMethod.Post, "/competitions") {
                authenticate(accessTokenResponse1)
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
    }

    @Test
    fun testCupCreation() {
        withApp {
            val postResponse = handleRequest(HttpMethod.Post, "/competitions") {
                authenticate(accessTokenResponse1)
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
    }

    @Test
    fun testTournamentCreation() {
        withApp {
            val postResponse = handleRequest(HttpMethod.Post, "/competitions") {
                authenticate(accessTokenResponse1)
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
    }

    @Test
    fun testAllMatches() {
        return withApp {
            testLeagueCreation()
            testCupCreation()
            testTournamentCreation()
            val getMatchesResponse = handleRequest(HttpMethod.Get,
                "/matches?startDateTime=${ZonedDateTime.now().minusHours(1).toString().urlEncoded()}").response
            assertEquals(HttpStatusCode.OK, getMatchesResponse.status())
            assertNotNull(getMatchesResponse.body<List<MatchListElementResponse>>())
            assertEquals(28 + 7 + ((6 * 2) + 3), getMatchesResponse.body<List<MatchListElementResponse>>()!!.size)

            val matchIds = getMatchesResponse.body<List<MatchListElementResponse>>()!!.map { it.id }
            matchIds.map { matchId ->
                val getMatchResponse = handleRequest(HttpMethod.Get, "/matches/$matchId").response
                assertEquals(HttpStatusCode.OK, getMatchResponse.status())
                assertNotNull(getMatchResponse.body<MatchResponse>())
                assertEquals(MatchResponse.EditPermission.NONE, getMatchResponse.body<MatchResponse>()!!.editPermission)
            }
        }
    }

    @Test
    fun testMyMatches() {
        withApp {
            testAllMatches()

            val getResponse1 = handleRequest(HttpMethod.Get,
                "/my-matches?startDateTime=${ZonedDateTime.now().minusHours(1).toString().urlEncoded()}") {
                authenticate(accessTokenResponse1)
            }.response
            assertEquals(HttpStatusCode.OK, getResponse1.status())
            assertNotNull(getResponse1.body<List<MatchListElementResponse>>())
            assertEquals(28 + 7 + ((6 * 2) + 3), getResponse1.body<List<MatchListElementResponse>>()!!.size)

            val getResponse2 = handleRequest(HttpMethod.Get,
                "/my-matches?startDateTime=${ZonedDateTime.now().minusHours(1).toString().urlEncoded()}") {
                authenticate(accessTokenResponse2)
            }.response
            assertEquals(HttpStatusCode.OK, getResponse2.status())
            assertNotNull(getResponse2.body<List<MatchListElementResponse>>())
            assertEquals(0, getResponse2.body<List<MatchListElementResponse>>()!!.size)

        }
    }

}