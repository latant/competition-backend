package app.service

import app.dao.CompetitionGraph
import app.dto.CompetitionUpdateRequest
import app.error.RequestError
import app.model.Competition
import app.security.UserPrincipal
import atUTC
import org.neo4j.ogm.session.load

object CompetitionEditorService {

    fun updateCompetition(id: Long, update: CompetitionUpdateRequest, principal: UserPrincipal) {
        CompetitionGraph.readWriteTransaction {
            val competition = load<Competition>(id, depth = 1) ?: RequestError.CompetitionNotFound()
            if (competition.creator.id != principal.id) {
                RequestError.UserCannotEditCompetition()
            }
            update.name?.let { competition.name = it }
            update.dateTime?.let { competition.dateTime = it.atUTC() }
            update.description?.let { competition.description = it }
            update.displayColor?.let { competition.displayColor = it }
            update.logo?.let { competition.logo = it }
            save(competition)
        }
    }

}