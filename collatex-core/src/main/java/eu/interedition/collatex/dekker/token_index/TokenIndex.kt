package eu.interedition.collatex.dekker.token_index

import eu.interedition.collatex.Token
import eu.interedition.collatex.Witness
import eu.interedition.collatex.suffixarray.SAIS
import eu.interedition.collatex.suffixarray.SuffixArrays
import eu.interedition.collatex.util.StreamUtil
import java.util.*
import java.util.function.Function

/**
 * Created by ronald on 4/20/15.
 */
class TokenIndex(comparator: Comparator<Token>, private val witnesses: List<Iterable<Token>>) {
    private val comparator: Comparator<Token>

    //TODO: not sure this functionality should be in this class or in a separate class
    private var witnessToStartToken: MutableMap<Witness, Int>? = null
    private var witnessToEndToken: MutableMap<Witness, Int>? = null
    @JvmField
    var token_array: Array<Token>? = null

    //END witness data
    @JvmField
    var suffix_array: IntArray? = null
    @JvmField
    var LCP_array: IntArray? = null
    var blocks: List<Block>? = null
    private var witnessToBlockInstances: MutableMap<Witness, MutableList<Block.Instance>>? = null

    constructor(comparator: Comparator<Token>, vararg witness: Iterable<Token>?) : this(comparator, Arrays.asList(*witness) as List<Iterable<Token>>) {}

    fun getStartTokenPositionForWitness(witness: Witness): Int {
        return witnessToStartToken!![witness]!!
    }

    // 1. prepare token array
    // 2. derive the suffix array
    // 3. derive LCP array
    // 4. derive LCP intervals
    // TODO: we do not have to store witnesses!
    fun prepare() {
        token_array = prepareTokenArray()
        val suffixData = SuffixArrays.createWithLCP(token_array, SAIS(), comparator)
        suffix_array = suffixData.suffixArray
        LCP_array = suffixData.lcp
        blocks = splitLCP_ArrayIntoIntervals()
        constructWitnessToBlockInstancesMap()
    }

    private fun prepareTokenArray(): Array<Token> {
        val tempTokenList: MutableList<Token> = ArrayList()
        var counter = 0
        witnessToStartToken = HashMap()
        witnessToEndToken = HashMap()
        for (tokens in witnesses) {
            val witness = StreamUtil.stream(tokens)
                .findFirst()
                .map { obj: Token -> obj.witness }
                .orElseThrow { IllegalArgumentException("Empty witness") }
            witnessToStartToken!![witness] = counter
            for (t in tokens) {
                tempTokenList.add(t)
                counter++
            }
            witnessToEndToken!![witness] = counter
            tempTokenList.add(MarkerToken(witnessToStartToken!!.size))
            counter++
        }
        return tempTokenList.toTypedArray()
    }

    class MarkerToken(val witnessIdentifier: Int) : Token {
        override fun toString(): String {
            return "$$witnessIdentifier"
        }

        override fun getWitness(): Witness {
            throw RuntimeException("A marker token is not part of any witness! The call to this method should never have happened!")
        }
    }

    internal class MarkerTokenComparator(private val delegate: Comparator<Token>) : Comparator<Token> {
        override fun compare(o1: Token, o2: Token): Int {
            val o1isMarker = o1 is MarkerToken
            val o2isMarker = o2 is MarkerToken
            if (o1isMarker) {
                // Both o1 and o2 could be marker tokens
                if (o2isMarker) {
                    val mt1 = o1 as MarkerToken
                    val mt2 = o2 as MarkerToken
                    // sort marker tokens from low to high
                    return mt1.witnessIdentifier - mt2.witnessIdentifier
                }
                // or one of them could be a marker token
                // always put the marker token before the content
                return -1
            }
            // or one of them could be a marker token
            // always put the content after the marker token
            return if (o2isMarker) {
                1
            } else delegate.compare(o1, o2)
            // Not a marker token; call delegate
        }
    }

    fun splitLCP_ArrayIntoIntervals(): List<Block> {
        val closedIntervals: MutableList<Block> = ArrayList()
        var previousLCP_value = 0
        val openIntervals = Stack<Block>()
        for (idx in LCP_array!!.indices) {
            val lcp_value = LCP_array!![idx]
            if (lcp_value > previousLCP_value) {
                openIntervals.push(Block(this, idx - 1, lcp_value))
                previousLCP_value = lcp_value
            } else if (lcp_value < previousLCP_value) {
                // close open intervals that are larger than current LCP value
                while (!openIntervals.isEmpty() && openIntervals.peek().length > lcp_value) {
                    val a = openIntervals.pop()
                    closedIntervals.add(Block(this, a.start, idx - 1, a.length))
                }
                // then: open a new interval starting with filtered intervals
                if (lcp_value > 0) {
                    val start = closedIntervals[closedIntervals.size - 1].start
                    openIntervals.push(Block(this, start, lcp_value))
                }
                previousLCP_value = lcp_value
            }
        }
        // add all the open intervals to the result
        for (interval in openIntervals) {
            if (interval.length > 0) {
                closedIntervals.add(Block(this, interval.start, LCP_array!!.size - 1, interval.length))
            }
        }
        return closedIntervals
    }

    private fun constructWitnessToBlockInstancesMap() {
        witnessToBlockInstances = HashMap()
        for (interval in blocks!!) {
            for (instance in interval.allInstances) {
                val w = instance.witness
                val instances = witnessToBlockInstances!!.computeIfAbsent(w, Function<Witness, MutableList<Block.Instance>> { v: Witness? -> ArrayList() })
                instances.add(instance)
            }
        }
    }

    //NOTE: An empty list is returned when there are no instances for the specified witness
    fun getBlockInstancesForWitness(w: Witness): List<Block.Instance> {
        return witnessToBlockInstances!!.computeIfAbsent(w, { a -> emptyList<Block.Instance>().toMutableList() })
    }

    fun size(): Int {
        return token_array!!.size
    }

    init {
        this.comparator = MarkerTokenComparator(comparator)
    }
}