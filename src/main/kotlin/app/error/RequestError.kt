package app.error

import io.ktor.http.*
import io.ktor.http.HttpStatusCode.Companion.BadRequest
import io.ktor.http.HttpStatusCode.Companion.Forbidden
import io.ktor.http.HttpStatusCode.Companion.NotFound
import io.ktor.http.HttpStatusCode.Companion.Unauthorized

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

    ;

    operator fun invoke(): Nothing = throw RequestErrorException(this)
}