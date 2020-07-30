package eu.interedition.collatex.dekker.token_index

import eu.interedition.collatex.Token
import eu.interedition.collatex.Witness
import eu.interedition.collatex.simple.SimpleToken
import java.util.*
import java.util.stream.IntStream

class Block {
    // every Block has a token index as a parent
    private val tokenIndex: TokenIndex

    // length = number of tokens in this block of text
    @JvmField
    val length: Int

    // start = start position in suffix array
    @JvmField
    val start: Int

    // end = end position in suffix array
    val end: Int

    // depth = number of witnesses this block of text occurs in
    // Note: depth is lazy initialized
    private var depth: Int?

    // For building blocks only
    constructor(tokenIndex: TokenIndex, suffix_start_position: Int, length: Int) {
        this.tokenIndex = tokenIndex
        start = suffix_start_position
        this.length = length
        end = 0
        depth = 0
    }

    constructor(tokenIndex: TokenIndex, start: Int, end: Int, length: Int) {
        this.tokenIndex = tokenIndex
        this.start = start
        this.end = end
        this.length = length
        depth = null
    }

    fun getDepth(): Int {
        if (depth == null) {
            depth = calculateDepth()
        }
        return depth!!
    }

    // frequency = number of times this block of text occurs in complete witness set
    val frequency: Int
        get() {
            check(end != 0) { "LCP interval is unclosed!" }
            return end - start + 1
        }

    // every i is one occurrence
    val allInstances: List<Instance>
        get() {
            val instances: MutableList<Instance> = ArrayList()
            for (i in start..end) {
                // every i is one occurrence
                val token_position = tokenIndex.suffix_array!![i]
                val instance = Instance(token_position, this)
                instances.add(instance)
            }
            return instances
        }// every i is one occurrence// with/or without end

    // transform lcp interval into int stream range
    val allOccurrencesAsRanges: IntStream
        get() {
            var result = IntStream.empty()
            // with/or without end
            for (i in start until end) {
                // every i is one occurrence
                val token_position = tokenIndex.suffix_array!![i]
                val range = IntStream.range(token_position, token_position + length)
                result = IntStream.concat(result, range)
            }
            return result
        }

    override fun toString(): String {
        return if (end == 0) {
            "Unclosed LCP interval start at: $start,  length: $length"
        } else "LCP interval start at: " + start + ", depth: " + getDepth() + ", length: " + length + " getFrequency:" + frequency
    }

    private fun calculateDepth(): Int {
        // the same block can occur multiple times in one witness
        val witnesses: MutableSet<Witness> = HashSet()
        for (instance in allInstances) {
            witnesses.add(instance.witness)
        }
        return witnesses.size
    }

    class Instance(// position in token array
        val start_token: Int, val block: Block) {
        fun length(): Int {
            return block.length
        }

        fun asRange(): IntStream {
            return IntStream.range(start_token, start_token + length())
        }

        override fun toString(): String {
            val tokens = tokens
            val normalized = StringBuilder()
            for (t in tokens) {
                val st = t as SimpleToken
                if (normalized.length > 0) {
                    normalized.append(" ")
                }
                normalized.append(st.normalized)
            }
            return normalized.toString()
        }

        //
        val tokens: List<Token>
            get() {
                val tokens: MutableList<Token> = ArrayList()
                tokens.addAll(Arrays.asList(*block.tokenIndex.token_array!!) //
                    .subList(start_token, start_token + length()))
                return tokens
            }
        val witness: Witness
            get() {
                val startToken = block.tokenIndex.token_array!![start_token]
                return startToken.witness
            }
    }
}