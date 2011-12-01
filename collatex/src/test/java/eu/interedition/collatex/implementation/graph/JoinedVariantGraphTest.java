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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.Iterables;
import eu.interedition.collatex.AbstractTest;
import eu.interedition.collatex.implementation.graph.db.PersistentVariantGraph;
import eu.interedition.collatex.implementation.graph.db.PersistentVariantGraphVertex;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.interedition.collatex.interfaces.IWitness;

public class JoinedVariantGraphTest extends AbstractTest {
  private static final Logger LOG = LoggerFactory.getLogger(JoinedVariantGraphTest.class);

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
    final PersistentVariantGraphVertex badWhiteVertex = vertexWith(graph, "bad white", w[0]);
    final PersistentVariantGraphVertex catVertex = vertexWith(graph, "cat", w[0]);
    final PersistentVariantGraphVertex sharedVertex = vertexWith(graph, "shared", w[0]);
    final PersistentVariantGraphVertex spilledVertex = vertexWith(graph, "spilled", w[1]);
    final PersistentVariantGraphVertex hisFoodVertex = vertexWith(graph, "his food", w[0]);
    final PersistentVariantGraphVertex againVertex = vertexWith(graph, "again", w[1]);

    assertHasWitnesses(edgeBetween(graph.getStart(), theVertex), w[0], w[1]);
    assertHasWitnesses(edgeBetween(theVertex, niceBlackVertex), w[0]);
    assertHasWitnesses(edgeBetween(niceBlackVertex, catVertex), w[0]);
    assertHasWitnesses(edgeBetween(theVertex, badWhiteVertex), w[1]);
    assertHasWitnesses(edgeBetween(badWhiteVertex, catVertex), w[0]);
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
    final PersistentVariantGraphVertex whitneyVertex = vertexWith(graph, "whitney", w[0]);
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
