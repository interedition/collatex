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

import eu.interedition.collatex.AbstractTest;
import eu.interedition.collatex.interfaces.*;
import org.junit.Test;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import static junit.framework.Assert.assertFalse;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class VariantGraphTest extends AbstractTest {

  @Test
  public void emptyGraph() {
    IVariantGraph graph = new VariantGraph();
    assertEquals(2, graph.vertexSet().size());
    assertTrue(graph.isEmpty());

    final IVariantGraphVertex startVertex = graph.getStartVertex();
    assertEquals("#", startVertex.getNormalized());

    final IVariantGraphVertex endVertex = graph.getEndVertex();
    assertEquals("#", endVertex.getNormalized());
    assertEquals(0, graph.edgeSet().size());
    assertEquals(0, graph.getWitnesses().size());
  }

  @Test
  public void getTokens() {
    final IWitness[] w = createWitnesses("a b c d");
    final List<INormalizedToken> tokens = merge(w).getTokens(w[0]);
    assertEquals(4, tokens.size());
    assertEquals("a", tokens.get(0).getNormalized());
    assertEquals("b", tokens.get(1).getNormalized());
    assertEquals("c", tokens.get(2).getNormalized());
    assertEquals("d", tokens.get(3).getNormalized());
  }

  @Test
  public void oneWitness() {
    final IWitness[] w = createWitnesses("only one witness");
    final IVariantGraph graph = merge(w);

    final Set<IVariantGraphVertex> vertices = graph.vertexSet();
    assertEquals(5, vertices.size());

    final Iterator<IVariantGraphVertex> vertexI = graph.iterator();
    final IVariantGraphVertex startVertex = vertexI.next();
    final IVariantGraphVertex firstVertex = vertexI.next();
    final IVariantGraphVertex secondVertex = vertexI.next();
    final IVariantGraphVertex thirdVertex = vertexI.next();
    final IVariantGraphVertex endVertex = vertexI.next();
    assertEquals("#", startVertex.getNormalized());
    assertEquals("only", firstVertex.getNormalized());
    assertEquals("one", secondVertex.getNormalized());
    assertEquals("witness", thirdVertex.getNormalized());
    assertEquals("#", endVertex.getNormalized());

    final Set<IVariantGraphEdge> edges = graph.edgeSet();
    assertEquals(4, edges.size());

    final Iterator<IVariantGraphEdge> edgeIt = edges.iterator();
    assertTrue(edgeIt.next().getWitnesses().contains(w[0]));
    assertTrue(edgeIt.next().getWitnesses().contains(w[0]));
    assertTrue(edgeIt.next().getWitnesses().contains(w[0]));
    assertTrue(edgeIt.next().getWitnesses().contains(w[0]));

    assertTrue(graph.containsEdge(startVertex, firstVertex));
    assertTrue(graph.containsEdge(firstVertex, secondVertex));
    assertTrue(graph.containsEdge(secondVertex, thirdVertex));
    assertTrue(graph.containsEdge(thirdVertex, endVertex));
  }

  @Test
  public void firstWitness() {
    final IWitness[] w = createWitnesses("the first witness");
    final IVariantGraph graph = merge(w);

    final Iterator<IVariantGraphVertex> iterator = graph.iterator();
    final IVariantGraphVertex start = iterator.next();
    final IVariantGraphVertex the = iterator.next();
    final IVariantGraphVertex first = iterator.next();
    final IVariantGraphVertex witness = iterator.next();
    final IVariantGraphVertex end = iterator.next();
    assertFalse(iterator.hasNext());

    assertEquals("#", start.getNormalized());
    assertEquals("the", the.getNormalized());
    assertEquals("first", first.getNormalized());
    assertEquals("witness", witness.getNormalized());
    assertEquals("#", end.getNormalized());

    assertEquals("the", the.getToken(w[0]).getContent());
    assertEquals("first", first.getToken(w[0]).getContent());
    assertEquals("witness", witness.getToken(w[0]).getContent());

    assertTrue(graph.containsEdge(start, the));
    assertTrue(graph.containsEdge(the, first));
    assertTrue(graph.containsEdge(first, witness));
    assertTrue(graph.containsEdge(witness, end));

    for (IVariantGraphEdge edge : graph.edgeSet()) {
      assertTrue("Witness " + w[0].getSigil() + " not present in set!", edge.containsWitness(w[0]));
    }
  }

  @Test
  public void longestPath() {
    // FIXME: can we do without the cast, e.g. is getLongestPath() a core feature of any variant graph
    final VariantGraph graph = (VariantGraph) merge("a", "b", "a b");
    assertEquals(4, graph.vertexSet().size());

    final List<IVariantGraphVertex> longestPath = graph.getLongestPath();
    assertEquals(2, longestPath.size());
    assertEquals("a", longestPath.get(0).getNormalized());
    assertEquals("b", longestPath.get(1).getNormalized());
  }

  @Test
  public void getPathForWitness() {
    final IWitness[] w = createWitnesses("a b c d e f ", "x y z d e", "a b x y z");
    final IVariantGraph graph = merge(w);
    final List<IVariantGraphEdge> path = graph.getPath(w[0]);

    assertEquals(7, path.size());
    assertEquals("#", graph.getEdgeSource(path.get(0)).getNormalized());
    assertEquals("a", graph.getEdgeTarget(path.get(0)).getNormalized());
    assertEquals("b", graph.getEdgeTarget(path.get(1)).getNormalized());
    assertEquals("c", graph.getEdgeTarget(path.get(2)).getNormalized());
    assertEquals("d", graph.getEdgeTarget(path.get(3)).getNormalized());
    assertEquals("e", graph.getEdgeTarget(path.get(4)).getNormalized());
    assertEquals("f", graph.getEdgeTarget(path.get(5)).getNormalized());
    assertEquals("#", graph.getEdgeTarget(path.get(6)).getNormalized());
  }

  @Test
  public void transpositions() {
    final IWitness[] w = createWitnesses("the black and white cat", "the white and black cat", "the black and black cat");
    final IVariantGraph graph = merge(w[0], w[1]);

    assertEquals(2, graph.getTransposedTokens().size());

    merge(graph, w[2]);
    final Map<IVariantGraphVertex, IVariantGraphVertex> transposed = graph.getTransposedTokens();
    assertEquals(2, transposed.size());
    for (Entry<IVariantGraphVertex, IVariantGraphVertex> nodePair : transposed.entrySet()) {
      assertEquals(nodePair.getKey().getNormalized(), nodePair.getValue().getNormalized());
    }
  }
}
