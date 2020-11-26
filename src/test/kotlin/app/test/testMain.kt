package app.test

import app.configureApplication
import app.dto.*
import app.model.Match
import app.serialization.JsonConfig.json
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import java.security.SecureRandom
import java.time.ZonedDateTime

fun main() {
    embeddedServer(Netty, port = 8080, host = "127.0.0.1") {
        configureApplication()
    }.start(wait = false)

    Thread.sleep(2000)

    val startDateTime = ZonedDateTime.now().minusYears(1).toString().urlEncoded()
    val endDateTime = ZonedDateTime.now().plusYears(1).toString().urlEncoded()

    val client = HttpClient {}

    runBlocking {
        val accessToken = json.decodeFromString<AccessTokenResponse>(
            client.post("http://localhost:8080/login") {
                header(HttpHeaders.ContentType, ContentType.Application.Json)
                body = json.encodeToString(UserLoginRequest("demo1@demo.com", "demo"))
            }
        )
        val competitions = json.decodeFromString<List<CompetitionListElementResponse>>(
            client.get("http://127.0.0.1:8080/competitions?minDateTime=${startDateTime}&maxDateTime=${endDateTime}"))
        println(accessToken)
        println(competitions)
        val leagueListElement = competitions.filterIsInstance<CompetitionListElementResponse.League>().first()
        val league = json.decodeFromString<CompetitionResponse>(
            client.get("http://localhost:8080/competitions/${leagueListElement.id}")
        ) as CompetitionResponse.League

        val random = SecureRandom()

        while (true) try {
            delay(100)
            val match = league.matches.random()
            val participant = match.participants.random() as CompetitionResponse.Match.Participant.Fix
            val score = random.nextInt() % 10 + 10

            val scoreUpdate = MatchUpdateRequest.ScoreUpdate(participant.competitorId, score.toDouble())
            val requestBody = MatchUpdateRequest(state = Match.State.ONGOING, scores = listOf(scoreUpdate))
            println(requestBody)

            client.patch<String>("http://localhost:8080/matches/${match.id}") {
                header(HttpHeaders.ContentType, ContentType.Application.Json)
                header(HttpHeaders.Authorization, "Bearer ${accessToken.accessToken}")
                body = json.encodeToString(requestBody)
            }.let { println(it) }
        } catch (e: Throwable) {
            e.printStackTrace()
        }

    }

}