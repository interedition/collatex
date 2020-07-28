/*
 * Copyright (c) 2015 The Interedition Development Group.
 *
 * This file is part of CollateX.
 *
 * CollateX is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * CollateX is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with CollateX.  If not, see <http://www.gnu.org/licenses/>.
 */
package eu.interedition.collatex.dekker

import eu.interedition.collatex.CollationAlgorithm
import eu.interedition.collatex.Token
import eu.interedition.collatex.VariantGraph
import eu.interedition.collatex.Witness
import eu.interedition.collatex.dekker.island.Island
import eu.interedition.collatex.dekker.island.IslandCollection
import eu.interedition.collatex.dekker.island.IslandConflictResolver
import eu.interedition.collatex.dekker.token_index.TokenIndex
import eu.interedition.collatex.dekker.token_index.TokenIndexToMatches
import eu.interedition.collatex.matching.EqualityTokenComparator
import eu.interedition.collatex.util.StreamUtil
import eu.interedition.collatex.util.VariantGraphRanking
import java.util.*
import java.util.logging.Level
import java.util.stream.Collectors

class DekkerAlgorithm @JvmOverloads constructor(private val comparator: Comparator<Token> = EqualityTokenComparator()) : CollationAlgorithm.Base(), InspectableCollationAlgorithm {
    var tokenIndex: TokenIndex? = null

    // tokens are mapped to vertices by their position in the token array
    @JvmField
    var vertex_array: Array<VariantGraph.Vertex?>? = null
    private val phraseMatchDetector: PhraseMatchDetector
    private val transpositionDetector: TranspositionDetector

    // for debugging purposes only
    private var allPossibleIslands: Set<Island>? = null
    private var preferredIslands: List<Island>? = null
    private var phraseMatches: List<List<Match>>? = null
    private var transpositions: MutableList<List<Match>>? = null
    private var mergeTranspositions = false

    // The algorithm contains two phases:
    // 1) Matching phase
    // This phase is implemented using a token array -> suffix array -> LCP array -> LCP intervals
    //
    // 2) Alignment phase
    // This phase uses a priority queue and looks at overlap between possible matches to find the optimal alignment and moves
    override fun collate(graph: VariantGraph, witnesses: List<Iterable<Token>>) {
        // phase 1: matching phase
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("Building token index from the tokens of all witnesses")
        }
        tokenIndex = TokenIndex(comparator, witnesses)
        tokenIndex!!.prepare()

        // phase 2: alignment phase
        vertex_array = arrayOfNulls(tokenIndex!!.token_array.size)
        var firstWitness = true
        for (tokens in witnesses) {
            val witness = StreamUtil.stream(tokens)
                .findFirst()
                .map { obj: Token -> obj.witness }
                .orElseThrow { IllegalArgumentException("Empty witness") }

            // first witness has a fast path
            if (firstWitness) {
                super.merge(graph, tokens, emptyMap())
                updateTokenToVertexArray(tokens, witness)
                firstWitness = false
                continue
            }

            // align second, third, fourth witness etc.
            if (LOG.isLoggable(Level.FINER)) {
                LOG.log(Level.FINER, "{0} + {1}: {2} vs. {3}", arrayOf(graph, witness, graph.vertices(), tokens))
            }

            // Phase 2a: Gather matches from the token index
            if (LOG.isLoggable(Level.FINE)) {
                LOG.log(Level.FINE, "{0} + {1}: Gather matches between variant graph and witness from token index", arrayOf(graph, witness))
            }
            allPossibleIslands = TokenIndexToMatches.createMatches(tokenIndex, vertex_array, graph, tokens)
            if (LOG.isLoggable(Level.FINE)) {
                LOG.log(Level.FINE, "{0} + {1}: Aligning witness and graph", arrayOf(graph, witness))
            }

            // Phase 2b: do the actual alignment
            val resolver = IslandConflictResolver(IslandCollection(allPossibleIslands))
            preferredIslands = resolver.createNonConflictingVersion().islands

            // we need to convert the islands into Map<Token, Vertex> for further processing
            val alignments: MutableMap<Token, VariantGraph.Vertex> = HashMap()
            for (island in preferredIslands!!.listIterator()) {
                for (c in island) {
                    alignments[c.match.token] = c.match.vertex
                }
            }
            if (LOG.isLoggable(Level.FINER)) {
                for ((key, value) in alignments) {
                    LOG.log(Level.FINER, "{0} + {1}: Aligned token (incl transposed): {2} = {3}", arrayOf(graph, witness, value, key))
                }
            }
            if (LOG.isLoggable(Level.FINE)) {
                LOG.log(Level.FINE, "{0} + {1}: Detect phrase matches", arrayOf(graph, witness))
            }

            // Phase 2c: detect phrases and transpositions
            phraseMatches = phraseMatchDetector.detect(alignments, graph, tokens)
            if (LOG.isLoggable(Level.FINER)) {
                for (phraseMatch in phraseMatches!!.listIterator()) {
                    LOG.log(Level.FINER, "{0} + {1}: Phrase match: {2}", arrayOf(graph, witness, phraseMatch))
                }
            }
            if (LOG.isLoggable(Level.FINE)) {
                LOG.log(Level.FINE, "{0} + {1}: Detect transpositions", arrayOf(graph, witness))
            }
            transpositions = transpositionDetector.detect(phraseMatches, graph)
            if (LOG.isLoggable(Level.FINE)) {
                LOG.log(Level.FINE, "transpositions:{0}", transpositions)
            }
            if (LOG.isLoggable(Level.FINER)) {
                for (transposition in transpositions!!.listIterator()) {
                    LOG.log(Level.FINER, "{0} + {1}: Transposition: {2}", arrayOf(graph, witness, transposition))
                }
            }
            if (LOG.isLoggable(Level.FINE)) {
                LOG.log(Level.FINE, "{0} + {1}: Determine aligned tokens by filtering transpositions", arrayOf(graph, witness))
            }

            // Filter out transposed tokens from aligned tokens
            for (transposedPhrase in transpositions!!.listIterator()) {
                for (match in transposedPhrase) {
                    alignments.remove(match.token)
                }
            }
            if (LOG.isLoggable(Level.FINER)) {
                for ((key, value) in alignments) {
                    LOG.log(Level.FINER, "{0} + {1}: Alignment: {2} = {3}", arrayOf(graph, witness, value, key))
                }
            }

            // Phase 2d: and merge
            merge(graph, tokens, alignments)

            // we filter out small transposed phrases over large distances
            val falseTranspositions: MutableList<List<Match>> = ArrayList()

            // rank the variant graph
            val ranking = VariantGraphRanking.of(graph)
            for (transposedPhrase in transpositions!!.listIterator()) {
                val match = transposedPhrase[0]
                val v1 = witnessTokenVertices[match.token]
                val v2 = match.vertex
                val distance = Math.abs(ranking.apply(v1) - ranking.apply(v2)) - 1
                if (distance > transposedPhrase.size * 3) {
                    falseTranspositions.add(transposedPhrase)
                }
            }
            transpositions!!.removeAll(falseTranspositions)

            // merge transpositions
            if (mergeTranspositions) {
                mergeTranspositions(graph, transpositions)
            }
            updateTokenToVertexArray(tokens, witness)
            if (LOG.isLoggable(Level.FINER)) {
                LOG.log(Level.FINER, "!{0}: {1}", arrayOf(graph, StreamUtil.stream(graph.vertices()).map { obj: VariantGraph.Vertex -> obj.toString() }.collect(Collectors.joining(", "))))
            }
        }
    }

    private fun updateTokenToVertexArray(tokens: Iterable<Token>, witness: Witness) {
        // we need to update the token -> vertex map
        // that information is stored in protected map
        var tokenPosition = tokenIndex!!.getStartTokenPositionForWitness(witness)
        for (token in tokens) {
            val vertex = witnessTokenVertices[token]
            vertex_array!![tokenPosition] = vertex
            tokenPosition++
        }
    }

    override fun collate(graph: VariantGraph, tokens: Iterable<Token>) {
        throw RuntimeException("Progressive alignment is not supported!")
    }

    override fun getPhraseMatches(): List<List<Match>> {
        return Collections.unmodifiableList(phraseMatches)
    }

    override fun getTranspositions(): List<List<Match>> {
        return Collections.unmodifiableList(transpositions)
    }

    fun getAllPossibleIslands(): Set<Island> {
        return Collections.unmodifiableSet(allPossibleIslands)
    }

    fun getPreferredIslands(): List<Island> {
        return Collections.unmodifiableList(preferredIslands)
    }

    /*
     * This check disables transposition rendering in the variant
     * graph when the variant graph contains more then two witnesses.
     * Transposition detection is done in a progressive manner
     * (witness by witness). When viewing the resulting graph
     * containing the variation for all witnesses
     * the detected transpositions can look strange, since segments
     * may have split into smaller or larger parts.
     */
    override fun setMergeTranspositions(b: Boolean) {
        mergeTranspositions = b
    }

    init {
        phraseMatchDetector = PhraseMatchDetector()
        transpositionDetector = TranspositionDetector()
    }
}