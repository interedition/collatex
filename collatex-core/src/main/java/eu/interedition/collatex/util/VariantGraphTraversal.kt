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
import java.util.*

/**
 * @author [Gregor Middell](http://gregor.middell.net/)
 */
class VariantGraphTraversal private constructor(private val graph: VariantGraph, private val witnesses: Set<Witness?>?) : Iterable<VariantGraph.Vertex?> {
    override fun iterator(): Iterator<VariantGraph.Vertex?> {
        return object : Iterator<VariantGraph.Vertex?> {
            private val encountered: MutableMap<VariantGraph.Vertex, Long> = HashMap()
            private val queue: Queue<VariantGraph.Vertex> = ArrayDeque()
            private var next = Optional.of(graph.start)
//            private var counter = 0
            override fun hasNext(): Boolean {
                return next.isPresent
            }

            override fun next(): VariantGraph.Vertex {
//                counter++
                val next = next.get()
                for (edge in next.outgoing().entries) {
                    if (witnesses != null && edge.value.stream().noneMatch { o: Witness? -> witnesses.contains(o) }) {
                        continue
                    }
                    val end = edge.key
                    val endEncountered = Optional.ofNullable(encountered[end]).orElse(0L)
                    val endIncoming = end.incoming().entries.stream() //
                        .filter { e: Map.Entry<VariantGraph.Vertex?, Set<Witness?>> -> witnesses == null || e.value.stream().anyMatch { o: Witness? -> witnesses.contains(o) } } //
                        .count()
                    check(endIncoming != endEncountered) { String.format("Encountered cycle traversing %s to %s", edge, end) }
                    if (endIncoming - endEncountered == 1L) {
                        queue.add(end)
                    }
                    encountered[end] = endEncountered + 1
                }
                this.next = Optional.ofNullable(queue.poll())
//                if (!this.next.isPresent) {
//                    println(counter)
//                }
                return next
            }
        }
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