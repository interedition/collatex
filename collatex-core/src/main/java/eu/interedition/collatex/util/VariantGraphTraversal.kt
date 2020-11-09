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
package eu.interedition.collatex.util

import eu.interedition.collatex.VariantGraph
import eu.interedition.collatex.Witness

/**
 * @author [Gregor Middell](http://gregor.middell.net/)
 * @author Ronald Haentjens Dekker
 */
class VariantGraphTraversal private constructor(private val graph: VariantGraph, private val witnesses: Set<Witness?>?) : Iterable<VariantGraph.Vertex?> {

    fun topologicallySortedTextNodes(graph: VariantGraph): List<VariantGraph.Vertex> {
        // https://en.wikipedia.org/wiki/Topological_sorting
        // Kahn's algorithm
        val sorted: MutableList<VariantGraph.Vertex> = mutableListOf()
        val todo: MutableSet<VariantGraph.Vertex> = mutableSetOf(graph.start)
        val handledEdges: MutableSet<VariantGraph.Edge> = mutableSetOf()
        while (todo.isNotEmpty()) {
            val node = todo.iterator().next()
            todo.remove(node)
            sorted += node
            for ((targetNode, e) in node.outgoingEdges()) {
                if (e !in handledEdges) {
                    handledEdges += e
                    if (handledEdges.containsAll(targetNode.incomingEdges().values)) {
                        todo += targetNode
                    }
                }
            }
        }
        return if (witnesses==null) {
            sorted
        } else {
            sorted.filter { vertex -> vertex === graph.start || vertex.witnesses().containsAll(witnesses) }
        }
    }

    override fun iterator(): Iterator<VariantGraph.Vertex?> {
        val topologicallySortedTextNodes = topologicallySortedTextNodes(graph)
        return topologicallySortedTextNodes.iterator()
    }

    companion object {
        @JvmStatic
        fun of(graph: VariantGraph, witnesses: Set<Witness>): VariantGraphTraversal {
            return VariantGraphTraversal(graph, witnesses)
        }

        @JvmStatic
        fun of(graph: VariantGraph): VariantGraphTraversal {
            return VariantGraphTraversal(graph, null)
        }
    }
}