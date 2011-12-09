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
import eu.interedition.collatex.implementation.graph.db.PersistentVariantGraphEdge;
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

  @Test
  public void transpositions1() {
    final PersistentVariantGraph graph = merge("the nice black and white cat", "the friendly white and black cat");
    assertEquals(12, Iterables.size(graph.traverseEdges(null)));
    assertEquals(12, Iterables.size(graph.traverseEdges(null)));
  }

  @Test
  public void transpositions2() {
    final IWitness[] w = createWitnesses("The black dog chases a red cat.", "A red cat chases the black dog.", "A red cat chases the yellow dog");
    final PersistentVariantGraph graph = merge(w);

    final PersistentVariantGraphEdge edge = edgeBetween(vertexWith(graph, "red", w[0]), vertexWith(graph, "cat", w[0]));
    assertHasWitnesses(edge, w[0], w[1], w[2]);

    assertEquals(14, Iterables.size(graph.traverseVertices(null)));
    assertEquals(10, Iterables.size(graph.traverseEdges(null)));
  }

  @Test
  public void joinTwoIdenticalWitnesses() {
    final IWitness[] w = createWitnesses("the black cat", "the black cat");
    final PersistentVariantGraph graph = merge(w).join();

    assertEquals(3, Iterables.size(graph.traverseVertices(null)));
    assertEquals(2, Iterables.size(graph.traverseEdges(null)));

    final PersistentVariantGraphVertex joinedVertex = vertexWith(graph, "the black cat", w[0]);

    assertHasWitnesses(edgeBetween(graph.getStart(), joinedVertex), w[0], w[1]);
    assertHasWitnesses(edgeBetween(graph.getEnd(), joinedVertex), w[0], w[1]);
  }

  @Test
  public void joinTwoDifferentWitnesses() {
    final IWitness[] w = createWitnesses("the nice black cat shared his food", "the bad white cat spilled his food again");
    final PersistentVariantGraph graph = merge(w).join();

    final PersistentVariantGraphVertex theVertex = vertexWith(graph, "the", w[0]);
    final PersistentVariantGraphVertex niceBlackVertex = vertexWith(graph, "nice black", w[0]);
    final PersistentVariantGraphVertex badWhiteVertex = vertexWith(graph, "bad white", w[1]);
    final PersistentVariantGraphVertex catVertex = vertexWith(graph, "cat", w[0]);
    final PersistentVariantGraphVertex sharedVertex = vertexWith(graph, "shared", w[0]);
    final PersistentVariantGraphVertex spilledVertex = vertexWith(graph, "spilled", w[1]);
    final PersistentVariantGraphVertex hisFoodVertex = vertexWith(graph, "his food", w[0]);
    final PersistentVariantGraphVertex againVertex = vertexWith(graph, "again", w[1]);

    assertHasWitnesses(edgeBetween(graph.getStart(), theVertex), w[0], w[1]);
    assertHasWitnesses(edgeBetween(theVertex, niceBlackVertex), w[0]);
    assertHasWitnesses(edgeBetween(niceBlackVertex, catVertex), w[0]);
    assertHasWitnesses(edgeBetween(theVertex, badWhiteVertex), w[1]);
    assertHasWitnesses(edgeBetween(badWhiteVertex, catVertex), w[1]);
    assertHasWitnesses(edgeBetween(catVertex, sharedVertex), w[0]);
    assertHasWitnesses(edgeBetween(sharedVertex, hisFoodVertex), w[0]);
    assertHasWitnesses(edgeBetween(catVertex, spilledVertex), w[1]);
    assertHasWitnesses(edgeBetween(spilledVertex, hisFoodVertex), w[1]);
    assertHasWitnesses(edgeBetween(hisFoodVertex, againVertex), w[1]);
  }

  @Test
  public void joinTwoDifferentWitnesses2() {
    final IWitness[] w = createWitnesses("Blackie, the black cat", "Whitney, the white cat");
    final PersistentVariantGraph graph = merge(w).join();

    final PersistentVariantGraphVertex blackieVertex = vertexWith(graph, "blackie", w[0]);
    final PersistentVariantGraphVertex whitneyVertex = vertexWith(graph, "whitney", w[1]);
    final PersistentVariantGraphVertex theVertex = vertexWith(graph, "the", w[0]);
    final PersistentVariantGraphVertex blackVertex = vertexWith(graph, "black", w[0]);
    final PersistentVariantGraphVertex whiteVertex = vertexWith(graph, "white", w[1]);
    final PersistentVariantGraphVertex catVertex = vertexWith(graph, "cat", w[0]);

    assertHasWitnesses(edgeBetween(graph.getStart(), blackieVertex), w[0]);
    assertHasWitnesses(edgeBetween(blackieVertex, theVertex), w[0]);
    assertHasWitnesses(edgeBetween(graph.getStart(), whitneyVertex), w[1]);
    assertHasWitnesses(edgeBetween(whitneyVertex, theVertex), w[1]);
    assertHasWitnesses(edgeBetween(theVertex, blackVertex), w[0]);
    assertHasWitnesses(edgeBetween(blackVertex, catVertex), w[0]);
    assertHasWitnesses(edgeBetween(theVertex, whiteVertex), w[1]);
    assertHasWitnesses(edgeBetween(whiteVertex, catVertex), w[1]);
  }
}
