package app

import app.dto.*
import app.error.RequestError
import app.service.CompetitionCreatorService
import app.service.CompetitionRetrievalService
import app.service.MatchService
import app.service.UserService
import atUTC
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import startOfDay
import toZonedDateTime
import userPrincipal
import utcNow
import java.time.ZonedDateTime

fun Routing.configureRoutes() {

    post("/register") {
        val requestBody: UserRegistrationRequest = call.receive()
        UserService.registerUser(requestBody)
        call.respond(HttpStatusCode.OK)
    }

    post("/login") {
        val requestBody: UserLoginRequest = call.receive()
        val responseBody: AccessTokenResponse = UserService.getAccessToken(requestBody)
        call.respond(responseBody)
    }

    get("/matches") {
        val queryParams = call.request.queryParameters
        val startDateTime = queryParams["startTime"]?.toZonedDateTime()?.atUTC()
        val endDateTime = queryParams["endTime"]?.toZonedDateTime()?.atUTC()
        val (startTime, endTime) = when {
            startDateTime != null && endDateTime != null -> startDateTime to endDateTime
            startDateTime != null -> startDateTime to startDateTime.plusDays(1)
            endDateTime != null -> endDateTime.minusDays(1) to endDateTime
            else -> utcNow().startOfDay().let { it to it.plusDays(1) }
        }
        val responseBody: List<MatchListElementResponse> = MatchService.getMatchesBetween(startTime, endTime)
        call.respond(responseBody)
    }

    authenticate {

        post("/competitions") {
            val requestBody: CompetitionCreationRequest = call.receive()
            val id = CompetitionCreatorService.createCompetition(call.userPrincipal!!, requestBody)
            val responseBody = IdResponse(id)
            call.respond(responseBody)
        }



        get("my-matches") {

        }

    }

    authenticate(optional = true) {

        get("/competitions/{id}") {
            val competitionId = call.parameters["id"]!!.toLong()
            val userPrincipal = call.userPrincipal
            val responseBody: CompetitionResponse = CompetitionRetrievalService
                .getCompetition(userPrincipal, competitionId)
            call.respond(responseBody)
        }

    }

}