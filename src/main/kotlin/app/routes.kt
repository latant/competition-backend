package app

import app.dto.*
import app.serialization.JsonConfig.json
import app.service.*
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.html.*
import io.ktor.http.*
import io.ktor.http.HttpStatusCode.Companion.OK
import io.ktor.http.content.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import kotlinx.serialization.encodeToString
import resourceFile
import startOfDay
import toLocalDateTime
import userPrincipal
import utcNow
import java.time.LocalDateTime

fun Routing.configureRoutes() {

    static("/static") {
        files(resourceFile("web"))
    }

    post("/register") {
        val requestBody: UserRegistrationRequest = call.receive()
        requestBody.validate()
        UserService.registerUser(requestBody)
        call.respond(OK)
    }

    post("/login") {
        val requestBody: UserLoginRequest = call.receive()
        val responseBody: AccessTokenResponse = UserService.getAccessToken(requestBody)
        call.respond(responseBody)
    }

    get("/competitions") {
        val (startDateTime, endDateTime) = call.request.startDateTimeAndEndDateTimeLenientRequestParams()
        val responseBody: List<CompetitionListElementResponse> = CompetitionRetrievalService
            .getCompetitionsBetween(startDateTime, endDateTime)
        call.respond(json.encodeToString(responseBody))
    }

    get("/competitions/{id}/stylesheet") {
        val id = call.parameters["id"]!!.toLong()
        val stylesheet = CompetitionRetrievalService.getCompetitionStylesheet(id)
        call.respondText(stylesheet, contentType = ContentType.Text.CSS)
    }

    get("/competitions/{id}/stylesheet-base") {
        val id = call.parameters["id"]!!.toLong()
        val displayColor = CompetitionRetrievalService.getCompetitionDisplayColor(id)
        val stylesheet = "body { background-color: $displayColor }"
        call.respondText(stylesheet, contentType = ContentType.Text.CSS)
    }

    get("/matches/{id}/stream-view") {
        val id = call.parameters["id"]!!.toLong()
        call.respondHtml(OK, StreamingService.getHtmlForMatch(id))
    }

    get("/competitions/{id}/actual-matches/stream-view") {
        val id = call.parameters["id"]!!.toLong()
        call.respondHtml(OK, StreamingService.getHtmlForActualMatches(id))
    }

    get("/competitions/{id}/standings/stream-view") {
        val id = call.parameters["id"]!!.toLong()
        call.respondHtml(OK, StreamingService.getHtmlForCompetitionStandings(id))
    }

    get("/groups/{id}/stream-view") {
        TODO()
    }

    authenticate {

        post("/competitions") {
            val requestBody: CompetitionCreationRequest = call.receive()
            requestBody.validate()
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
            requestBody.validate()
            CompetitionEditorService.updateCompetition(id, requestBody, userPrincipal)
            val responseBody: CompetitionResponse = CompetitionRetrievalService.getCompetition(userPrincipal, id)
            call.respond(responseBody)
        }

        patch("matches/{id}") {
            val id = call.parameters["id"]!!.toLong()
            val userPrincipal = call.userPrincipal!!
            val requestBody: MatchUpdateRequest = call.receive()
            MatchEditorService.updateMatch(id, requestBody, userPrincipal)
            val responseBody: MatchResponse = MatchRetrievalService.getMatch(id, userPrincipal)
            call.respond(responseBody)
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

        patch("groups/{id}") {
            val id = call.parameters["id"]!!.toLong()
            val userPrincipal = call.userPrincipal!!
            val requestBody: GroupUpdateRequest = call.receive()
            requestBody.validate()
            CompetitionEditorService.updateGroup(id, requestBody, userPrincipal)
            val responseBody: GroupResponse = CompetitionRetrievalService.getGroup(id)
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

        get("/matches/{id}") {
            val matchId = call.parameters["id"]!!.toLong()
            val userPrincipal = call.userPrincipal
            val responseBody: MatchResponse = MatchRetrievalService.getMatch(matchId, userPrincipal)
            call.respond(responseBody)
        }

        get("/matches") {
            val (startDateTime, endDateTime) = call.request.startDateTimeAndEndDateTimeLenientRequestParams()
            val userPrincipal = call.userPrincipal
            val responseBody: List<MatchListElementResponse> = MatchRetrievalService
                .getMatchesBetween(startDateTime, endDateTime, userPrincipal)
            call.respond(responseBody)
        }

        get("/competition/{id}/actual-matches") {
            val id = call.parameters["id"]!!.toLong()
            val userPrincipal = call.userPrincipal
            val responseBody: List<MatchListElementResponse> = MatchRetrievalService
                .getActualMatchesOfCompetition(id, userPrincipal)
            call.respond(responseBody)
        }

    }

    get("groups/{id}") {
        val groupId = call.parameters["id"]!!.toLong()
        val responseBody: GroupResponse = CompetitionRetrievalService.getGroup(groupId)
        call.respond(responseBody)
    }

}


fun ApplicationRequest.startDateTimeAndEndDateTimeLenientRequestParams(): Pair<LocalDateTime, LocalDateTime> {
    val minDateTime = queryParameters["minDateTime"]?.toLocalDateTime()
    val maxDateTime = queryParameters["maxDateTime"]?.toLocalDateTime()
    return when {
        minDateTime != null && maxDateTime != null -> minDateTime to maxDateTime
        minDateTime != null -> minDateTime to minDateTime.plusDays(1)
        maxDateTime != null -> maxDateTime.minusDays(1) to maxDateTime
        else -> utcNow().startOfDay().let { it to it.plusDays(1) }
    }
}