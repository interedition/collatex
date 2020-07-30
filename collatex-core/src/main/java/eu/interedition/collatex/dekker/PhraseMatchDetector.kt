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

import eu.interedition.collatex.Token
import eu.interedition.collatex.VariantGraph
import java.util.*

/**
 * @author Ronald Haentjens Dekker
 * @author Bram Buitendijk
 */
class PhraseMatchDetector {
    fun detect(linkedTokens: Map<Token, VariantGraph.Vertex>, base: VariantGraph, tokens: Iterable<Token>): List<List<Match>> {
        val phraseMatches: MutableList<List<Match>> = ArrayList()
        val basePhrase: MutableList<VariantGraph.Vertex> = ArrayList()
        val witnessPhrase: MutableList<Token> = ArrayList()
        var previous = base.start
        for (token in tokens) {
            if (!linkedTokens.containsKey(token)) {
                addNewPhraseMatchAndClearBuffer(phraseMatches, basePhrase, witnessPhrase)
                continue
            }
            val baseVertex = linkedTokens[token]
            // requirements:
            // - previous and base vertex should have the same witnesses
            // - previous and base vertex should either be in the same transposition(s) or both aren't in any transpositions
            // - there should be a directed edge between previous and base vertex
            // - there may not be a longer path between previous and base vertex
            val sameTranspositions = HashSet(previous!!.transpositions()) == HashSet(baseVertex!!.transpositions())
            val sameWitnesses = previous.witnesses() == baseVertex.witnesses()
            val directedEdge = previous.outgoing().containsKey(baseVertex)
            val isNear = sameTranspositions && sameWitnesses && directedEdge && (previous.outgoing().size == 1 || baseVertex.incoming().size == 1)
            if (!isNear) {
                addNewPhraseMatchAndClearBuffer(phraseMatches, basePhrase, witnessPhrase)
            }
            basePhrase.add(baseVertex)
            witnessPhrase.add(token)
            previous = baseVertex
        }
        if (!basePhrase.isEmpty()) {
            phraseMatches.add(Match.createPhraseMatch(basePhrase, witnessPhrase))
        }
        return phraseMatches
    }

    private fun addNewPhraseMatchAndClearBuffer(phraseMatches: MutableList<List<Match>>, basePhrase: MutableList<VariantGraph.Vertex>, witnessPhrase: MutableList<Token>) {
        if (!basePhrase.isEmpty()) {
            phraseMatches.add(Match.createPhraseMatch(basePhrase, witnessPhrase))
            basePhrase.clear()
            witnessPhrase.clear()
        }
    }
}