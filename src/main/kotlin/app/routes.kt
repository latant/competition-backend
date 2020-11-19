package app

import app.dto.*
import app.serialization.JsonConfig.json
import app.service.*
import atUTC
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.http.*
import io.ktor.http.HttpStatusCode.Companion.OK
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.websocket.*
import kotlinx.serialization.encodeToString
import startOfDay
import toZonedDateTime
import userPrincipal
import utcNow
import java.time.LocalDateTime

fun Routing.configureRoutes() {

    post("/register") {
        val requestBody: UserRegistrationRequest = call.receive()
        UserService.registerUser(requestBody)
        call.respond(OK)
    }

    post("/login") {
        val requestBody: UserLoginRequest = call.receive()
        val responseBody: AccessTokenResponse = UserService.getAccessToken(requestBody)
        call.respond(responseBody)
    }

    get("/matches") {
        val (startDateTime, endDateTime) = call.request.startDateTimeAndEndDateTimeLenientRequestParams()
        val responseBody: List<MatchListElementResponse> = MatchRetrievalService.getMatchesBetween(startDateTime, endDateTime)
        call.respond(responseBody)
    }

    get("/matches/{id}/stream-view") {

    }

    webSocket("/matches/{id}") {

    }

    get("/competitions/{id}/matches-stream-view") {

    }

    webSocket("/competitions/{id}/matches") {

    }

    get("/competitions/{id}/standings-stream-view") {

    }

    webSocket("/competitions/{id}/standings") {

    }

    get("/groups/{id}/stream-view") {

    }

    webSocket("/groups/{id}") {

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
            val userPrincipal = call.userPrincipal!!
            val responseBody: List<MatchListElementResponse> = MatchRetrievalService
                .getUsersMatchesBetween(startDateTime, endDateTime, userPrincipal)
            call.respond(responseBody)
        }

        patch("/competitions/{id}") {
            val id = call.parameters["id"]!!.toLong()
            val userPrincipal = call.userPrincipal!!
            val requestBody: CompetitionUpdateRequest = call.receive()
            CompetitionEditorService.updateCompetition(id, requestBody, userPrincipal)
            call.respond(OK)
        }

        patch("matches/{id}") {
            val id = call.parameters["id"]!!.toLong()
            val userPrincipal = call.userPrincipal!!
            val requestBody: MatchUpdateRequest = call.receive()
            MatchEditorService.updateMatch(id, requestBody, userPrincipal)
            call.respond(OK)
        }

        post("matches/{id}/editors") {
            val id = call.parameters["id"]!!.toLong()
            val userPrincipal = call.userPrincipal!!
            val requestBody: MatchEditorAdditionRequest = call.receive()
            MatchEditorService.addMatchEditor(id, requestBody, userPrincipal)
            call.respond(OK)
        }

        delete("matches/{id}/editors/{email}") {
            val id = call.parameters["id"]!!.toLong()
            val editorEmail = call.parameters["email"]
            val userPrincipal = call.userPrincipal!!
            MatchEditorService.removeMatchEditor(id, editorEmail, userPrincipal)
            call.respond(OK)
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

        get("/matches/{id}") {
            val matchId = call.parameters["id"]!!.toLong()
            val userPrincipal = call.userPrincipal
            val responseBody: MatchResponse = MatchRetrievalService.getMatch(matchId, userPrincipal)
            call.respond(responseBody)
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