package app.service

import app.dao.CompetitionGraph
import app.dto.MatchEditorAdditionRequest
import app.dto.MatchResponse
import app.dto.MatchResponse.EditPermission.*
import app.dto.MatchUpdateRequest
import app.error.RequestError
import app.model.Match
import app.model.Match.State.*
import app.model.User
import app.security.UserPrincipal
import atUTC
import org.neo4j.ogm.cypher.ComparisonOperator
import org.neo4j.ogm.cypher.Filter
import org.neo4j.ogm.session.load
import org.neo4j.ogm.session.loadAll
import utcNow

object MatchEditorService {

    fun editPermissionForUserWithId(match: Match, userId: Long): MatchResponse.EditPermission {
        return when {
            match.competition.creator.id == userId -> FULL
            match.editors?.any { it.id == userId } ?: false -> BASIC
            else -> NONE
        }
    }

    fun addMatchEditor(id: Long, editorRequest: MatchEditorAdditionRequest, userPrincipal: UserPrincipal) {
        CompetitionGraph.readWriteTransaction {
            val editorFilter = Filter(User::email.name, ComparisonOperator.EQUALS, editorRequest.editorEmail)
            val newEditor = loadAll<User>(editorFilter).singleOrNull() ?: RequestError.InvalidMatchEditorEmail()
            val match = load<Match>(id, depth = 2) ?: RequestError.MatchNotFound()
            if (editPermissionForUserWithId(match, userPrincipal.id) != FULL) {
                RequestError.UserCannotEditMatch()
            }
            match.editors = match.editors?.plus(newEditor)?.distinctBy { it.id } ?: listOf(newEditor)
            save(match)
        }
    }

    fun removeMatchEditor(id: Long, editorEmail: String?, userPrincipal: UserPrincipal) {
        CompetitionGraph.readWriteTransaction {
            val match = load<Match>(id, depth = 2) ?: RequestError.MatchNotFound()
            if (editPermissionForUserWithId(match, userPrincipal.id) != FULL) {
                RequestError.UserCannotEditMatch()
            }
            match.editors = match.editors?.filter { it.email != editorEmail }
            save(match)
        }
    }

    fun updateMatch(id: Long, update: MatchUpdateRequest, userPrincipal: UserPrincipal) {
        CompetitionGraph.readWriteTransaction {
            val match = load<Match>(id, depth = 2) ?: RequestError.MatchNotFound()
            val editPermission = editPermissionForUserWithId(match, userPrincipal.id)
            if (editPermission == NONE) RequestError.UserCannotEditMatch()
            update.description?.let { match.description = it }
            update.scores?.let { match.updateScores(it, update.state) }
            update.state?.let { match.updateState(it) }
            update.dateTime?.let {
                if (editPermission != FULL) RequestError.UserCannotEditMatchDateTime
                match.dateTime = it.atUTC()
            }
            save(match)
        }
    }

    private fun Match.updateScores(newScores: List<MatchUpdateRequest.ScoreUpdate>, newState: Match.State?) {
        if (newState == NOT_STARTED_YET || (newState == null && state == NOT_STARTED_YET)) {
            RequestError.UnstartedMatchScoreCannotBeModified()
        }
        val participations = participations.onEach {
            it.competitor ?: RequestError.MatchScoresCannotBeModifiedWhileUnknownParticipant()
        }
        newScores.forEach { u ->
            val p = participations.find { it.competitor!!.id == u.competitorId }
                ?: RequestError.MatchScoreCannotBeModifiedForParticipantNotInMatch()
            p.score = u.score
        }
    }

    private fun Match.updateState(newState: Match.State) {
        when (state) {
            NOT_STARTED_YET -> {
                if (newState == ENDED) RequestError.MatchCannotBeEndedBeforeBeingStarted()
                if (newState == ONGOING) {
                    participations.forEach { it.score = 0.0 }
                }
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
        if (state == ENDED) onEnded()
    }

    private fun Match.onEnded() {
        endDateTime = utcNow()
        proceededMatchParticipation?.let { p ->
            p.competitor = participations.maxByOrNull { it.score ?: Double.MIN_VALUE }!!.competitor!!
        }
        group?.let { g ->
            // Resolves quotes for competitors
            if (g.matches.all { it.state == ENDED }) {
                val standingsTable = CompetitionRetrievalService.standingsTable(g.matches.toSet(), g.competitors)
                g.playoffsQuotes.forEach { q ->
                    val competitorId = standingsTable.records.find { it.place == q.groupPlace }!!.competitorId
                    q.competitor = g.competitors.find { it.id == competitorId }!!
                }
            }
        }
    }

}