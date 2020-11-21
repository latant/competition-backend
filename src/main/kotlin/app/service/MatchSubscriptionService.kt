package app.service

import app.dao.CompetitionGraph
import app.error.RequestError
import app.model.Match
import kotlinx.coroutines.channels.Channel
import org.neo4j.ogm.session.load
import org.pcollections.HashTreePMap
import org.pcollections.HashTreePSet
import org.pcollections.PMap
import org.pcollections.PSet
import java.util.concurrent.atomic.AtomicReference

object MatchSubscriptionService {

    private val matchSubscriptions = AtomicReference<PMap<Long, PSet<MatchChannel>>>(HashTreePMap.empty())

    private class MatchChannel(private val id: Long, private val channel: Channel<Match>): Channel<Match> by channel {
        override fun close(cause: Throwable?): Boolean {
            matchSubscriptions.getAndUpdate {
                val subscriptions = it[id]?.minus(this) ?: HashTreePSet.empty()
                if (subscriptions.isEmpty()) it.minus(id) else it.plus(id, subscriptions)
            }
            return channel.close()
        }
    }

    suspend fun matchUpdated(match: Match) {
        matchSubscriptions.get()[match.id]?.forEach {
            it.send(match)
        }
    }

    suspend fun subscribe(id: Long, action: suspend (Match) -> Unit) {
        val matchChannel = CompetitionGraph.readOnlyTransaction {
            val match = load<Match>(id, depth = 4) ?: RequestError.MatchNotFound()
            MatchChannel(id, Channel<Match>(1).apply { send(match) })
        }
        matchSubscriptions.getAndUpdate { it.plus(id, (it[id] ?: HashTreePSet.empty()).plus(matchChannel)) }
        try {
            for (m in matchChannel) {
                action(m)
            }
        } finally {
            matchChannel.close()
        }
    }

}