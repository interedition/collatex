/**
 * CollateX - a Java library for collating textual sources,
 * for example, to produce an apparatus.
 *
 * Copyright (C) 2010 ESF COST Action "Interedition".
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package eu.interedition.collatex.implementation.graph;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import eu.interedition.collatex.AbstractTest;
import eu.interedition.collatex.implementation.graph.db.PersistentVariantGraph;
import eu.interedition.collatex.implementation.graph.db.PersistentVariantGraphTransposition;
import eu.interedition.collatex.implementation.graph.db.PersistentVariantGraphVertex;
import eu.interedition.collatex.interfaces.*;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;

import static junit.framework.Assert.assertFalse;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class VariantGraphTest extends AbstractTest {

  @Test
  public void emptyGraph() {
    final PersistentVariantGraph graph = merge(createWitnesses());
    assertEquals(0, graph.getWitnesses().size());
    assertEquals(2, Iterables.size(graph.traverseVertices(null)));
    assertEquals(1, Iterables.size(graph.traverseEdges(null)));
  }

  @Test
  public void getTokens() {
    final IWitness[] w = createWitnesses("a b c d");
    final PersistentVariantGraph graph = merge(w);
    final List<PersistentVariantGraphVertex> vertices = Lists.newArrayList(graph.traverseVertices(Sets.newTreeSet(Arrays.asList(w))));
    assertEquals(6, vertices.size());
    assertEquals(graph.getStart(), vertices.get(0));
    assertEquals("a", vertices.get(1).getTokens(null).first().getNormalized());
    assertEquals("b", vertices.get(2).getTokens(null).first().getNormalized());
    assertEquals("c", vertices.get(3).getTokens(null).first().getNormalized());
    assertEquals("d", vertices.get(4).getTokens(null).first().getNormalized());
    assertEquals(graph.getEnd(), vertices.get(5));
  }

  @Test
  public void oneWitness() {
    final IWitness[] w = createWitnesses("only one witness");
    final PersistentVariantGraph graph = merge(w);

    assertEquals(5, Iterables.size(graph.traverseVertices(null)));
    assertEquals(4, Iterables.size(graph.traverseEdges(null)));

    final PersistentVariantGraphVertex firstVertex = vertexWith(graph, "only", w[0]);
    final PersistentVariantGraphVertex secondVertex = vertexWith(graph, "one", w[0]);
    final PersistentVariantGraphVertex thirdVertex = vertexWith(graph, "witness", w[0]);

    assertHasWitnesses(edgeBetween(graph.getStart(), firstVertex), w[0]);
    assertHasWitnesses(edgeBetween(firstVertex, secondVertex), w[0]);
    assertHasWitnesses(edgeBetween(secondVertex, thirdVertex), w[0]);
    assertHasWitnesses(edgeBetween(thirdVertex, graph.getEnd()), w[0]);
  }

  @Test
  @Ignore("Longest Path not yet implemented")
  public void longestPath() {
    final IWitness[] w = createWitnesses("a", "b", "a b");
    final PersistentVariantGraph graph = merge(w);
    assertEquals(4, Iterables.size(graph.traverseEdges(null)));

    final List<PersistentVariantGraphVertex> longestPath = Lists.newArrayList(graph.findLongestPath());
    assertEquals(2, longestPath.size());
    assertEquals("a", longestPath.get(0).getTokens(Sets.newTreeSet(Collections.singleton(w[2]))).first().getNormalized());
    assertEquals("b", longestPath.get(0).getTokens(Sets.newTreeSet(Collections.singleton(w[2]))).first().getNormalized());
  }

  @Test
  public void getPathForWitness() {
    final IWitness[] w = createWitnesses("a b c d e f ", "x y z d e", "a b x y z");
    final PersistentVariantGraph graph = merge(w);
    final SortedSet<IWitness> witnessSet = Sets.newTreeSet(Collections.singleton(w[0]));
    final List<PersistentVariantGraphVertex> path = Lists.newArrayList(graph.traverseVertices(witnessSet));

    assertEquals(8, path.size());
    assertEquals(graph.getStart(), path.get(0));
    assertEquals("a", path.get(1).getTokens(witnessSet).first().getNormalized());
    assertEquals("b", path.get(2).getTokens(witnessSet).first().getNormalized());
    assertEquals("c", path.get(3).getTokens(witnessSet).first().getNormalized());
    assertEquals("d", path.get(4).getTokens(witnessSet).first().getNormalized());
    assertEquals("e", path.get(5).getTokens(witnessSet).first().getNormalized());
    assertEquals("f", path.get(6).getTokens(witnessSet).first().getNormalized());
    assertEquals(graph.getEnd(), path.get(7));
  }

  @Test
  public void transpositions() {
    final IWitness[] w = createWitnesses("the black and white cat", "the white and black cat", "the black and black cat");
    final PersistentVariantGraph graph = merge(w[0], w[1]);

    assertEquals(2, graph.getTranspositions().size());

    merge(graph, w[2]);
    final Set<PersistentVariantGraphTransposition> transposed = graph.getTranspositions();
    assertEquals(2, transposed.size());
  }
}
