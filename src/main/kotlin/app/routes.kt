package app

import app.dto.*
import app.error.RequestError
import app.serialization.JsonConfig.json
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
import kotlinx.serialization.encodeToString
import startOfDay
import toZonedDateTime
import userPrincipal
import utcNow
import java.time.LocalDateTime
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
        val (startDateTime, endDateTime) = call.request.startDateTimeAndEndDateTimeLenientRequestParams()
        val responseBody: List<MatchListElementResponse> = MatchService.getMatchesBetween(startDateTime, endDateTime)
        call.respond(responseBody)
    }

    authenticate {

        post("/competitions") {
            val requestBody: CompetitionCreationRequest = call.receive()
            val id = CompetitionCreatorService.createCompetition(call.userPrincipal!!, requestBody)
            val responseBody = IdResponse(id)
            call.respond(responseBody)
        }

        get("/my-matches") {
            val (startDateTime, endDateTime) = call.request.startDateTimeAndEndDateTimeLenientRequestParams()
            val responseBody: List<MatchResponse> = MatchService
                .getUsersMatchesBetween(startDateTime, endDateTime, call.userPrincipal!!)
            call.respond(responseBody)
        }

    }

    authenticate(optional = true) {

        get("/competitions/{id}") {
            val competitionId = call.parameters["id"]!!.toLong()
            val userPrincipal = call.userPrincipal
            val responseBody: CompetitionResponse = CompetitionRetrievalService
                .getCompetition(userPrincipal, competitionId)
            call.respond(json.encodeToString(responseBody))
        }

    }

}


fun ApplicationRequest.startDateTimeAndEndDateTimeLenientRequestParams(): Pair<LocalDateTime, LocalDateTime> {
    val startDateTime = queryParameters["startDateTime"]?.toZonedDateTime()?.atUTC()
    val endDateTime = queryParameters["endDateTime"]?.toZonedDateTime()?.atUTC()
    return when {
        startDateTime != null && endDateTime != null -> startDateTime to endDateTime
        startDateTime != null -> startDateTime to startDateTime.plusDays(1)
        endDateTime != null -> endDateTime.minusDays(1) to endDateTime
        else -> utcNow().startOfDay().let { it to it.plusDays(1) }
    }
}