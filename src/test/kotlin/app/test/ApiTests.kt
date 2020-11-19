package app.test

import app.dao.CompetitionGraph
import app.dto.*
import com.github.javafaker.Faker
import io.ktor.http.*
import io.ktor.http.HttpMethod.Companion.Get
import io.ktor.http.HttpMethod.Companion.Patch
import io.ktor.http.HttpMethod.Companion.Post
import io.ktor.http.HttpStatusCode.Companion.Forbidden
import io.ktor.http.HttpStatusCode.Companion.OK
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
    private lateinit var accessToken1: AccessTokenResponse
    private lateinit var accesstoken2: AccessTokenResponse
    private var leagueId = 0L
    private var cupId = 0L
    private var tournamentId = 0L

    private fun registerUser(): AccessTokenResponse = withApp {
        val name = faker.name().name()
        val email = "${faker.name().username()}@gmail.com"
        val password = faker.letterify("???????")
        handleRequest(Post, "/register") {
            jsonBody(UserRegistrationRequest(name, email, password))
        }
        handleRequest(Post, "/login") {
            jsonBody(UserLoginRequest(email, password))
        }.response.body()!!
    }

    @Before
    fun init() {
        CompetitionGraph.session {
            purgeDatabase()
        }
        accessToken1 = registerUser()
        accesstoken2 = registerUser()
    }

    private fun TestApplicationRequest.authenticate(accessTokenResponse: AccessTokenResponse) {
        addHeader(HttpHeaders.Authorization, "Bearer ${accessTokenResponse.accessToken}")
    }

    @Test
    fun testLeagueCreation() {
        withApp {
            val postResponse = handleRequest(Post, "/competitions") {
                authenticate(accessToken1)
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
            assertEquals(OK, postResponse.status())
            val idResponse = postResponse.body<IdResponse>().also(::assertNotNull)!!
            val getResponse = handleRequest(Get, "/competitions/${idResponse.id}").response
            assertEquals(OK, getResponse.status())
            assertNotNull(getResponse.body<CompetitionResponse>())
            assert(getResponse.body<CompetitionResponse>() is CompetitionResponse.League)

            leagueId = idResponse.id
        }
    }

    @Test
    fun testCupCreation() {
        withApp {
            val postResponse = handleRequest(Post, "/competitions") {
                authenticate(accessToken1)
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
            assertEquals(OK, postResponse.status())
            val idResponse = postResponse.body<IdResponse>().also(::assertNotNull)!!
            val getResponse = handleRequest(Get, "/competitions/${idResponse.id}").response
            assertEquals(OK, getResponse.status())
            assertNotNull(getResponse.body<CompetitionResponse>())
            assert(getResponse.body<CompetitionResponse>() is CompetitionResponse.Cup)

            cupId = idResponse.id
        }
    }

    @Test
    fun testTournamentCreation() {
        withApp {
            val postResponse = handleRequest(Post, "/competitions") {
                authenticate(accessToken1)
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
            assertEquals(OK, postResponse.status())
            val idResponse = postResponse.body<IdResponse>().also(::assertNotNull)!!
            val getResponse = handleRequest(Get, "/competitions/${idResponse.id}").response
            assertEquals(OK, getResponse.status())
            assertNotNull(getResponse.body<CompetitionResponse>())
            assert(getResponse.body<CompetitionResponse>() is CompetitionResponse.Tournament)

            tournamentId = idResponse.id
        }
    }

    @Test
    fun testAllMatches() {
        return withApp {
            testLeagueCreation()
            testCupCreation()
            testTournamentCreation()
            val getMatchesResponse = handleRequest(Get,
                "/matches?startDateTime=${ZonedDateTime.now().minusHours(1).toString().urlEncoded()}").response
            assertEquals(OK, getMatchesResponse.status())
            assertNotNull(getMatchesResponse.body<List<MatchListElementResponse>>())
            assertEquals(28 + 7 + ((6 * 2) + 3), getMatchesResponse.body<List<MatchListElementResponse>>()!!.size)

            val matchIds = getMatchesResponse.body<List<MatchListElementResponse>>()!!.map { it.id }
            matchIds.map { matchId ->
                val getMatchResponse = handleRequest(Get, "/matches/$matchId").response
                assertEquals(OK, getMatchResponse.status())
                assertNotNull(getMatchResponse.body<MatchResponse>())
                assertEquals(MatchResponse.EditPermission.NONE, getMatchResponse.body<MatchResponse>()!!.editPermission)
            }
        }
    }

    @Test
    fun testMyMatches() {
        withApp {
            testAllMatches()

            val getResponse1 = handleRequest(Get,
                "/my-matches?startDateTime=${ZonedDateTime.now().minusHours(1).toString().urlEncoded()}") {
                authenticate(accessToken1)
            }.response
            assertEquals(OK, getResponse1.status())
            assertNotNull(getResponse1.body<List<MatchListElementResponse>>())
            assertEquals(28 + 7 + ((6 * 2) + 3), getResponse1.body<List<MatchListElementResponse>>()!!.size)

            val getResponse2 = handleRequest(Get,
                "/my-matches?startDateTime=${ZonedDateTime.now().minusHours(1).toString().urlEncoded()}") {
                authenticate(accesstoken2)
            }.response
            assertEquals(OK, getResponse2.status())
            assertNotNull(getResponse2.body<List<MatchListElementResponse>>())
            assertEquals(0, getResponse2.body<List<MatchListElementResponse>>()!!.size)

        }
    }

    @Test
    fun testCompetitionUpdate() {
        withApp {
            testAllMatches()

            val forbiddenResponse = handleRequest(Patch, "competitions/$cupId") {
                authenticate(accesstoken2)
                jsonBody(CompetitionUpdateRequest())
            }.response
            assertEquals(Forbidden, forbiddenResponse.status())

            val okResponse = handleRequest(Patch, "competitions/$cupId") {
                authenticate(accessToken1)
                jsonBody(CompetitionUpdateRequest(name = "My new Cup name"))
            }.response
            assertEquals(OK, okResponse.status())

            val cup = handleRequest(Get, "competitions/$cupId").response.body<CompetitionResponse>()!!
            cup as CompetitionResponse.Cup
            assertEquals("My new Cup name", cup.name)
        }
    }

}