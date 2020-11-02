package app

import app.dto.AccessToken
import app.dto.IdResponse
import app.dto.UserLogin
import app.dto.UserRegistration
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
        val requestBody: UserRegistration = call.receive()
        UserService.registerUser(requestBody)
        call.respond(HttpStatusCode.OK)
    }

    post("/login") {
        val requestBody: UserLogin = call.receive()
        val responseBody: AccessToken = UserService.getAccessToken(requestBody)
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