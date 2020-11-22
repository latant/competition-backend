package app.service

import app.concurrency.ChannelMap
import app.dao.CompetitionGraph
import app.error.RequestError
import app.model.Group
import app.model.League
import app.model.Match
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.consumeAsFlow
import org.neo4j.ogm.session.load

object SubscriptionService {

    private val matchChannels = ChannelMap<Long, Match>()
    private val groupChannels = ChannelMap<Long, Group>()
    private val leagueChannels = ChannelMap<Long, League>()
    private val actualMatchesChannels = ChannelMap<Long, List<Match>>()

    suspend fun matchUpdated(match: Match) {
        matchChannels.send(match.id!!, match)
    }

    suspend fun groupUpdated(group: Group) {
        groupChannels.send(group.id!!, group)
    }

    suspend fun leagueUpdated(league: League) {
        leagueChannels.send(league.id!!, league)
    }

    suspend fun subscribeForMatch(id: Long, action: suspend (Match) -> Unit) {
        val match = CompetitionGraph.readOnlyTransaction {
            load<Match>(id, depth = 4) ?: RequestError.MatchNotFound()
        }
        val matchChannel = matchChannels.createChannel(id).apply { send(match) }
        matchChannel.consumeAsFlow().collect(action)
    }

    suspend fun subscribeForGroup(id: Long, action: suspend (Group) -> Unit) {
        val group = CompetitionGraph.readOnlyTransaction {
            load<Group>(id, depth = 3) ?: RequestError.GroupNotFound()
        }
        val groupChannel = groupChannels.createChannel(id).apply { send(group) }
        groupChannel.consumeAsFlow().collect(action)
    }

    suspend fun subscribeForLeague(id: Long, action: suspend (League) -> Unit) {
        val league = CompetitionGraph.readOnlyTransaction {
            load<League>(id, depth = 4) ?: RequestError.LeagueNotFound()
        }
        val leagueChannel = leagueChannels.createChannel(id).apply { send(league) }
        leagueChannel.consumeAsFlow().collect(action)
    }

}