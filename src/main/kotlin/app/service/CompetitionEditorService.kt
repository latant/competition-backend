package app.service

import app.dao.CompetitionGraph
import app.dto.CompetitionUpdateRequest
import app.dto.GroupUpdateRequest
import app.error.RequestError
import app.model.Competition
import app.model.Group
import app.security.UserPrincipal
import atUTC
import org.neo4j.ogm.session.load

object CompetitionEditorService {

    fun updateCompetition(id: Long, update: CompetitionUpdateRequest, principal: UserPrincipal) {
        CompetitionGraph.readWriteTransaction {
            val competition = load<Competition>(id, depth = 4) ?: RequestError.CompetitionNotFound()
            if (competition.creator.id != principal.id) {
                RequestError.UserCannotEditCompetition()
            }
            update.name?.let { competition.name = it }
            update.startDateTime?.let { competition.startDateTime = it.atUTC() }
            update.endDateTime?.let { competition.endDateTime = it.atUTC() }
            update.description?.let { competition.description = it }
            update.displayColor?.let { competition.displayColor = it }
            update.logo?.let { competition.logo = it }
            update.styleSheet?.let { competition.styleSheet = it }
            save(competition)
        }
    }

    fun updateGroup(id: Long, update: GroupUpdateRequest, principal: UserPrincipal) {
        CompetitionGraph.readWriteTransaction {
            val group = load<Group>(id, depth = 3) ?: RequestError.GroupNotFound()
            if (group.groupStage.competition.creator.id != principal.id) {
                RequestError.UserCannotEditGroup()
            }
            update.name?.let { group.name = it }
            save(group)
        }
    }

}