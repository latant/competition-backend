package app.service

import app.dao.CompetitionGraph
import app.dto.MatchResponse
import app.dto.MatchResponse.EditPermission.*
import app.dto.MatchUpdateRequest
import app.error.RequestError
import app.model.Match
import app.model.Match.State.*
import app.security.UserPrincipal
import atUTC
import org.neo4j.ogm.session.load

object MatchEditorService {

    fun updateMatch(id: Long, update: MatchUpdateRequest, userPrincipal: UserPrincipal) {
        CompetitionGraph.readWriteTransaction {
            val match = load<Match>(id, depth = 2) ?: RequestError.MatchNotFound()
            val editPermission = editPermissionForUserWithId(match, userPrincipal.id)
            if (editPermission == NONE) RequestError.UserCannotEditMatch()
            update.description?.let { match.description = it }
            update.scores?.let { match.updateScores(it) }
            update.state?.let { match.updateState(it) }
            update.dateTime?.let {
                if (editPermission != FULL) RequestError.UserCannotEditMatchDateTime
                match.dateTime = it.atUTC()
            }
        }
    }

    private fun Match.updateScores(newScores: List<MatchUpdateRequest.ScoreUpdate>) {
        val participations = participations.onEach {
            it.competitionParticipant ?: RequestError.MatchScoresCannotBeModifiedWhileUnknownParticipant()
        }
        newScores.forEach { u ->
            val p = participations.find { it.competitionParticipant!!.id == u.participantId }
                ?: RequestError.MatchScoreCannotBeModifiedForParticipantNotInMatch()
            p.score = u.score
        }
    }

    private fun Match.updateState(newState: Match.State) {
        when (state) {
            NOT_STARTED_YET -> {
                if (newState == ENDED) RequestError.MatchCannotBeEndedBeforeBeingStarted()
                state = newState
            }
            ONGOING -> {
                if (newState == NOT_STARTED_YET) {
                    participations.forEach { it.score = null }
                }
                state = newState
            }
            ENDED -> {
                if (newState != ENDED) RequestError.MatchCannotBeRevivedFromEndedState()
            }
        }
    }

    fun editPermissionForUserWithId(match: Match, userId: Long): MatchResponse.EditPermission {
        return when {
            match.competition.creator.id == userId -> MatchResponse.EditPermission.FULL
            match.editors?.any { it.id == userId } ?: false -> MatchResponse.EditPermission.BASIC
            else -> NONE
        }
    }

}