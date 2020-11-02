package app

import app.dto.AccessTokenResponse
import app.dto.IdResponse
import app.dto.UserLoginRequest
import app.dto.UserRegistrationRequest
import app.service.CompetitionCreatorService
import app.service.UserService
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import userPrincipal

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

    authenticate {

        post("/competitions") {
            val id = CompetitionCreatorService.createCompetition(call.userPrincipal!!, call.receive())
            val responseBody = IdResponse(id)
            call.respond(responseBody)
        }

    }

}