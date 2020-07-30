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
import java.util.function.Function
import java.util.function.Predicate
import java.util.stream.Collectors

/**
 * @author [Gregor Middell](http://gregor.middell.net/)
 */
class Match(val vertex: VariantGraph.Vertex, val token: Token?) {

    override fun hashCode(): Int {
        return Objects.hash(vertex, token)
    }

    override fun equals(obj: Any?): Boolean {
        if (obj != null && obj is Match) {
            val other = obj
            return vertex == other.vertex && token == other.token
        }
        return super.equals(obj)
    }

    override fun toString(): String {
        return "{$vertex; $token}"
    }

    companion object {
        fun createPhraseMatch(vertices: List<VariantGraph.Vertex>, tokens: List<Token>): List<Match> {
            val phraseMatch: MutableList<Match> = ArrayList(vertices.size)
            val vertexIt = vertices.iterator()
            val tokenIt = tokens.iterator()
            while (vertexIt.hasNext() && tokenIt.hasNext()) {
                phraseMatch.add(Match(vertexIt.next(), tokenIt.next()))
            }
            return phraseMatch
        }

        fun createNoBoundaryMatchPredicate(graph: VariantGraph): Predicate<Match> {
            return Predicate { input: Match -> input.vertex != graph.start && input.vertex != graph.end }
        }

        @JvmField
        val PHRASE_MATCH_TO_TOKENS =  //
            Function { input: List<Match> -> input.stream().map { m: Match -> m.token }.collect(Collectors.toList()) }
    }
}