package app.error

import io.ktor.http.*
import io.ktor.http.HttpStatusCode.Companion.BadRequest
import io.ktor.http.HttpStatusCode.Companion.Forbidden
import io.ktor.http.HttpStatusCode.Companion.NotFound
import io.ktor.http.HttpStatusCode.Companion.Unauthorized
import org.slf4j.Logger
import org.slf4j.LoggerFactory

enum class RequestError(val statusCode: HttpStatusCode, val message: String = "") {

    EmailAlreadyUsed(BadRequest),
    InvalidUsernameOrPassword(Unauthorized),
    CompetitionNotFound(NotFound),
    MatchNotFound(NotFound),
    UserCannotEditCompetition(Forbidden),
    UserCannotEditMatch(Forbidden),
    UserCannotEditMatchDateTime(Forbidden),
    MatchScoresCannotBeModifiedWhileUnknownParticipant(BadRequest),
    MatchScoreCannotBeModifiedForParticipantNotInMatch(BadRequest),
    MatchCannotBeEndedBeforeBeingStarted(BadRequest),
    MatchCannotBeRevivedFromEndedState(BadRequest),
    InvalidMatchEditorEmail(BadRequest),
    UnstartedMatchScoreCannotBeModified(BadRequest),
    GroupNotFound(NotFound),
    UserCannotEditGroup(Forbidden),
    LeagueNotFound(NotFound),
    InvalidRequestBody(BadRequest),

    ;

    operator fun invoke(message: String? = null): Nothing = throw RequestErrorException(this, message)

    companion object {
        val logger: Logger = LoggerFactory.getLogger(RequestError::class.java)
    }
}