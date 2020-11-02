package app.service

import length
import repeat
import repeatEveryNth
import kotlin.math.max

object CompetitionAlgorithmService {

    // Based on the algorithm by Richard Schurig
    fun leagueScheduling(participants: Int): List<List<Pair<Int, Int>>> {
        val n = participants + participants % 2
        val m = (n / 2) * (n - 1)
        val ids = 0 until (n - 1)
        val t1 = ids.asSequence().repeat().take(m).toList()
        val t2 = ids.asSequence().repeat().repeatEveryNth(n / 2).take(m).toList().asReversed()
        val ms = t1.zip(t2).chunked(n / 2)
        val last = participants - 1
        return if (last % 2 == 0)
            ms.map { it.drop(1) }
        else
            ms.mapIndexed { i, r ->
                listOf(if (i % 2 == 0) (r.first().first to last) else (last to r.first().second))
                    .plus(r.drop(1))
            }
    }

    fun <P> groupsWithCount(list: List<P>, count: Int) = list.groupsWithMinSize(list.size / count)

    private fun <P> List<P>.groupsWithMinSize(minGroupSize: Int): List<List<P>> = shuffled().run {
        val groups = chunked(minGroupSize).map { it.toMutableList() }.toMutableList()
        val lastGroup = groups.last()
        if (lastGroup.size < minGroupSize) {
            groups.removeLast()
            lastGroup.forEachIndexed { i, p -> groups[i % groups.size].add(p) }
        }
        groups.shuffled()
    }

    private fun <P> List<P>.groupsWithMaxSize(maxGroupSize: Int): List<List<P>> = shuffled().run {
        val groups = chunked(maxGroupSize).map { it.toMutableList() }.toMutableList()
        val lastGroup = groups.last()
        val firstGroup = groups.first()
        var i = groups.size - 2
        while (lastGroup.size < firstGroup.size - 1) {
            lastGroup.add(groups[i].removeLast())
            i = (i - 1) % (groups.size - 1)
        }
        groups.shuffled()
    }


    sealed class CupMatchSource {
        abstract val roundOrdinal: Int

        class Match(val home: CupMatchSource, val away: CupMatchSource) : CupMatchSource() {
            override val roundOrdinal by lazy { max(home.roundOrdinal, away.roundOrdinal) + 1 }
        }

        class Participant(val index: Int) : CupMatchSource() {
            override val roundOrdinal get() = 0
        }
    }

    fun cupMatchSource(participants: IntRange) : CupMatchSource  = participants.run {
        require(length > 0)
        return when (length) {
            1 -> CupMatchSource.Participant(first)
            else -> {
                val med = first + (last - first) / 2
                CupMatchSource.Match(cupMatchSource(first..med), cupMatchSource((med + 1)..last))
            }
        }
    }

    private interface PlayoffMatchSourceCandidate {
        fun toMatchSource(): PlayoffMatchSource
    }

    private class PlayoffMatchCandidate : PlayoffMatchSourceCandidate {
        private val sources = mutableListOf<PlayoffMatchSourceCandidate>()
        fun add(vararg candidate: PlayoffMatchSourceCandidate) { sources.addAll(candidate) }
        fun fill() = 0.until(2 - sources.size).map { PlayoffMatchCandidate() }.also(sources::addAll)
        override fun toMatchSource() = when (sources.size) {
            1 -> sources.first().toMatchSource()
            2 -> PlayoffMatchSource.Match(sources[0].toMatchSource(), sources[1].toMatchSource())
            else -> error("invalid playoff match sources number!")
        }
        override fun toString() = sources.toString()
    }

    sealed class PlayoffMatchSource {
        abstract val roundOrdinal: Int

        class Match(val home: PlayoffMatchSource, val away: PlayoffMatchSource) : PlayoffMatchSource() {
            override val roundOrdinal by lazy { max(home.roundOrdinal, away.roundOrdinal) + 1 }
        }

        class Quote(val group: Group, val place: Int) : PlayoffMatchSourceCandidate, PlayoffMatchSource() {
            override val roundOrdinal get() = 0
            val value = group.size.toFloat() / place
            override fun toMatchSource() = this
        }

        class Group(val index: Int, val size: Int) {
            private val _quotes = mutableListOf<Quote>()
            val quotes: List<Quote> get() = _quotes
            fun addQuote() { _quotes.add(Quote(this, _quotes.size + 1)) }
        }
    }

    fun <P> playOffs(groupStruct: List<List<P>>, teamCount: Int): PlayoffMatchSource {
        val groups = groupStruct.mapIndexed { i, l -> PlayoffMatchSource.Group(i, l.size) }
        repeat(teamCount) {
            groups.minByOrNull { it.quotes.size.toFloat() / it.size.toFloat() }!!.addQuote()
        }
        val quotes = groups.flatMap { it.quotes }.sortedBy { it.value }.toMutableList()
        val final = PlayoffMatchCandidate()
        var matches = listOf(final)
        while (matches.isNotEmpty()) {
            var quotesNeeded = matches.size * 2
            if (quotesNeeded < quotes.size) {
                matches = matches.flatMap { it.fill() }
                continue
            }
            while (quotes.size != quotesNeeded) {
                val m = matches.last()
                m.add(quotes.removeLast())
                matches = matches.dropLast(1)
                quotesNeeded -= 2
            }
            quotes.sortBy { it.group.index }
            matches.forEachIndexed { i, m -> m.add(quotes[i], quotes[quotes.lastIndex - i]) }
            matches = emptyList()
        }
        return final.toMatchSource()
    }

}