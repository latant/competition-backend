package app.concurrency

import kotlinx.coroutines.channels.Channel
import org.pcollections.HashTreePMap
import org.pcollections.HashTreePSet
import org.pcollections.PMap
import org.pcollections.PSet
import java.util.concurrent.atomic.AtomicReference

// A thread-safe multi-map that provides channels for for specific keys. Closed channels are automatically removed.
class ChannelMap<K, E> {

    private val channels = AtomicReference<PMap<K, PSet<Channel<E>>>>(HashTreePMap.empty())

    inner class Entry(private val key: K, private val channel: Channel<E>): Channel<E> by channel {
        override fun close(cause: Throwable?): Boolean {
            channels.getAndUpdate {
                val subscriptions = it[key]?.minus(this) ?: HashTreePSet.empty()
                if (subscriptions.isEmpty()) it.minus(key) else it.plus(key, subscriptions)
            }
            return channel.close()
        }
    }

    fun createChannel(key: K): Entry {
        val entry = Entry(key, Channel(1))
        channels.getAndUpdate { it.plus(key, (it[key] ?: HashTreePSet.empty()).plus(entry)) }
        return entry
    }

    suspend fun send(key: K, element: E) {
        channels.get()[key]?.forEach {
            it.send(element)
        }
    }

}