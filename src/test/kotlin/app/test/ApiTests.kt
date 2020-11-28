package app.test

import app.dao.CompetitionGraph
import app.dto.*
import app.model.Match
import com.github.javafaker.Faker
import io.ktor.http.*
import io.ktor.http.HttpMethod.Companion.Get
import io.ktor.http.HttpMethod.Companion.Patch
import io.ktor.http.HttpMethod.Companion.Post
import io.ktor.http.HttpStatusCode.Companion.Forbidden
import io.ktor.http.HttpStatusCode.Companion.OK
import io.ktor.server.testing.*
import org.junit.Before
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import org.junit.contrib.java.lang.system.EnvironmentVariables
import resourceFileText
import java.security.SecureRandom
import java.time.LocalDateTime
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

    private fun registerUser(
        name: String = faker.name().name(),
        email: String = "${faker.name().username()}@gmail.com",
        password: String = faker.letterify("???????"),
    ): AccessTokenResponse = withApp {
        val registrationResponse = handleRequest(Post, "/register") {
            jsonBody(UserRegistrationRequest(name, email, password))
        }.response
        assertEquals(OK, registrationResponse.status())
        handleRequest(Post, "/login") {
            jsonBody(UserLoginRequest(email, password))
        }.response.body()!!
    }

    @Before
    fun init() {
        CompetitionGraph.session {
            purgeDatabase()
        }
        accessToken1 = registerUser(email = "demo1@demo.com", password = "demo")
        accesstoken2 = registerUser(email = "demo2@demo.com", password = "demo")
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
                    startDateTime = ZonedDateTime.now(),
                    endDateTime = ZonedDateTime.now().plusDays(2),
                    competitors = listOf(
                        CompetitionCreationRequest.Competitor("Eagles"),
                        CompetitionCreationRequest.Competitor("Lions"),
                        CompetitionCreationRequest.Competitor("Bulls"),
                        CompetitionCreationRequest.Competitor("Hornets"),
                        CompetitionCreationRequest.Competitor("Wolves"),
                        CompetitionCreationRequest.Competitor("Tigers"),
                        CompetitionCreationRequest.Competitor("Hawks"),
                        CompetitionCreationRequest.Competitor("Sharks"),
                    ),
                    displayColor = "#aaaaaa",
                    styleSheet = ".match-participant-name { background-color: #000 }"
                ))
            }.response
            assertEquals(OK, postResponse.status())
            val idResponse = postResponse.body<IdResponse>().also(::assertNotNull)!!
            val getResponse = handleRequest(Get, "/competitions/${idResponse.id}").response
            handleRequest(Patch, "/competitions/${idResponse.id}") {
                authenticate(accessToken1)
                jsonBody(CompetitionUpdateRequest(logo = resourceFileText("logo.txt")))
            }.response.let { assertEquals(OK, it.status()) }
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
                    startDateTime = ZonedDateTime.now(),
                    endDateTime = ZonedDateTime.now().plusDays(2),
                    competitors = listOf(
                        CompetitionCreationRequest.Competitor("Eagles"),
                        CompetitionCreationRequest.Competitor("Lions"),
                        CompetitionCreationRequest.Competitor("Bulls"),
                        CompetitionCreationRequest.Competitor("Hornets"),
                        CompetitionCreationRequest.Competitor("Wolves"),
                        CompetitionCreationRequest.Competitor("Tigers"),
                        CompetitionCreationRequest.Competitor("Hawks"),
                        CompetitionCreationRequest.Competitor("Sharks"),
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
                    startDateTime = ZonedDateTime.now(),
                    endDateTime = ZonedDateTime.now().plusDays(2),
                    groupCount = 2,
                    playoffsCompetitorCount = 4,
                    competitors = listOf(
                        CompetitionCreationRequest.Competitor("Eagles"),
                        CompetitionCreationRequest.Competitor("Lions"),
                        CompetitionCreationRequest.Competitor("Bulls"),
                        CompetitionCreationRequest.Competitor("Hornets"),
                        CompetitionCreationRequest.Competitor("Wolves"),
                        CompetitionCreationRequest.Competitor("Tigers"),
                        CompetitionCreationRequest.Competitor("Hawks"),
                        CompetitionCreationRequest.Competitor("Sharks"),
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
                "/matches?startDateTime=${LocalDateTime.now().minusHours(1).toString().urlEncoded()}").response
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
                "/my-matches?startDateTime=${LocalDateTime.now().minusHours(1).toString().urlEncoded()}") {
                authenticate(accessToken1)
            }.response
            assertEquals(OK, getResponse1.status())
            assertNotNull(getResponse1.body<List<MatchListElementResponse>>())
            assertEquals(28 + 7 + ((6 * 2) + 3), getResponse1.body<List<MatchListElementResponse>>()!!.size)

            val getResponse2 = handleRequest(Get,
                "/my-matches?startDateTime=${LocalDateTime.now().minusHours(1).toString().urlEncoded()}") {
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

    @Test
    @Ignore
    fun testPeriodicMatchUpdate() {
        withApp {
            testAllMatches()

            val league = handleRequest(Get, "competitions/$leagueId") {}.response.body<CompetitionResponse>()
                    as CompetitionResponse.League

            val random = SecureRandom()

            while (true) {
                Thread.sleep(100)

                val match = league.matches.random()
                val participant = match.participants.random() as CompetitionResponse.Match.Participant.Fix
                val score = random.nextInt() % 10 + 10

                val scoreUpdate = MatchUpdateRequest.ScoreUpdate(participant.competitorId, score.toDouble())
                val requestBody = MatchUpdateRequest(state = Match.State.ONGOING, scores = listOf(scoreUpdate))
                println(requestBody)

                val resp = handleRequest(Patch, "matches/${match.id}") {
                    authenticate(accessToken1)
                    jsonBody(requestBody)
                }.response

                assertEquals(OK, resp.status())

            }
        }
    }

}