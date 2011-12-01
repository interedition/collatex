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

package eu.interedition.collatex.implementation.alignment;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import eu.interedition.collatex.AbstractTest;
import eu.interedition.collatex.implementation.graph.db.PersistentVariantGraph;
import eu.interedition.collatex.implementation.graph.db.PersistentVariantGraphVertex;
import eu.interedition.collatex.interfaces.IWitness;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @todo Add test with an addition or omission in between!
 */
public class VariantGraphTest extends AbstractTest {

  @Test
  public void twoWitnesses() {
    final IWitness[] w = createWitnesses("the black cat", "the black cat");
    final PersistentVariantGraph graph = merge(w);

    assertEquals(5, Iterables.size(graph.traverseVertices(null)));
    assertEquals(4, Iterables.size(graph.traverseEdges(null)));

    final PersistentVariantGraphVertex theVertex = vertexWith(graph, "the", w[0]);
    final PersistentVariantGraphVertex blackVertex = vertexWith(graph, "black", w[0]);
    final PersistentVariantGraphVertex catVertex = vertexWith(graph, "cat", w[0]);

    assertHasWitnesses(edgeBetween(graph.getStart(), theVertex), w[0], w[1]);
    assertHasWitnesses(edgeBetween(theVertex, blackVertex), w[0], w[1]);
    assertHasWitnesses(edgeBetween(blackVertex, catVertex), w[0], w[1]);
    assertHasWitnesses(edgeBetween(catVertex, graph.getEnd()), w[0], w[1]);
  }


  @Test
  public void addition1() {
    final IWitness[] w = createWitnesses("the black cat", "the white and black cat");
    final PersistentVariantGraph graph = merge(w);

    assertEquals(7, Lists.newArrayList(graph.traverseVertices(null)).size());
    assertEquals(7, Iterables.size(graph.traverseEdges(null)));

    final PersistentVariantGraphVertex theVertex = vertexWith(graph, "the", w[0]);
    final PersistentVariantGraphVertex whiteVertex = vertexWith(graph, "white", w[1]);
    final PersistentVariantGraphVertex andVertex = vertexWith(graph, "and", w[1]);
    final PersistentVariantGraphVertex blackVertex = vertexWith(graph, "black", w[0]);
    final PersistentVariantGraphVertex catVertex = vertexWith(graph, "cat", w[0]);

    assertHasWitnesses(edgeBetween(graph.getStart(), theVertex), w[0], w[1]);
    assertHasWitnesses(edgeBetween(theVertex, blackVertex), w[0]);
    assertHasWitnesses(edgeBetween(blackVertex, catVertex), w[0], w[1]);
    assertHasWitnesses(edgeBetween(catVertex, graph.getEnd()), w[0], w[1]);
    assertHasWitnesses(edgeBetween(theVertex, whiteVertex), w[1]);
    assertHasWitnesses(edgeBetween(whiteVertex, graph.getEnd()), w[1]);
    assertHasWitnesses(edgeBetween(andVertex, blackVertex), w[1]);
  }

  @Test
  public void variant() {
    final IWitness[] w = createWitnesses("the black cat", "the white cat", "the green cat", "the red cat", "the yellow cat");
    final PersistentVariantGraph graph = merge(w);

    final List<PersistentVariantGraphVertex> vertices = Lists.newArrayList(graph.traverseVertices(null));
    assertEquals(9, vertices.size());
    assertEquals(12, Iterables.size(graph.traverseEdges(null)));

    final PersistentVariantGraphVertex theVertex = vertexWith(graph, "the", w[0]);
    final PersistentVariantGraphVertex blackVertex = vertexWith(graph, "black", w[0]);
    final PersistentVariantGraphVertex whiteVertex = vertexWith(graph, "white", w[1]);
    final PersistentVariantGraphVertex greenVertex = vertexWith(graph, "green", w[2]);
    final PersistentVariantGraphVertex redVertex = vertexWith(graph, "red", w[3]);
    final PersistentVariantGraphVertex yellowVertex = vertexWith(graph, "yellow", w[4]);
    final PersistentVariantGraphVertex catVertex = vertexWith(graph, "cat", w[0]);

    assertHasWitnesses(edgeBetween(graph.getStart(), theVertex), w[0], w[1], w[2], w[3], w[4]);
    assertHasWitnesses(edgeBetween(theVertex, blackVertex), w[0]);
    assertHasWitnesses(edgeBetween(blackVertex, catVertex), w[0]);
    assertHasWitnesses(edgeBetween(catVertex, graph.getEnd()), w[0], w[1], w[2], w[3], w[4]);
    assertHasWitnesses(edgeBetween(theVertex, whiteVertex), w[1]);
    assertHasWitnesses(edgeBetween(whiteVertex, catVertex), w[1]);
    assertHasWitnesses(edgeBetween(theVertex, greenVertex), w[2]);
    assertHasWitnesses(edgeBetween(greenVertex, catVertex), w[2]);
    assertHasWitnesses(edgeBetween(theVertex, redVertex), w[3]);
    assertHasWitnesses(edgeBetween(redVertex, catVertex), w[3]);
    assertHasWitnesses(edgeBetween(theVertex, yellowVertex), w[4]);
    assertHasWitnesses(edgeBetween(yellowVertex, catVertex), w[4]);
  }

  @Test
  public void doubleTransposition2() {
    final IWitness[] w = createWitnesses("a b", "b a");
    final PersistentVariantGraph graph = merge(w);

    assertEquals(5, Iterables.size(graph.traverseVertices(null)));

    assertHasWitnesses(edgeBetween(vertexWith(graph, "b", w[1]), vertexWith(graph, "a", w[1])), w[1]);
    assertHasWitnesses(edgeBetween(vertexWith(graph, "a", w[0]), vertexWith(graph, "b", w[0])), w[0]);
  }

  @Test
  public void mirroredTranspositionsWithMatchInBetween() {
    final IWitness[] w = createWitnesses("the black and white cat", "the white and black cat");
    final PersistentVariantGraph graph = merge(w);

    Assert.assertEquals(9, Iterables.size(graph.traverseVertices(null)));

    // FIXME: find out, how to test this without stable topological order
    /*
    final Iterator<IVariantGraphVertex> iterator = graph.iterator();

    assertEquals("#", iterator.next().getNormalized());
    assertEquals("the", iterator.next().getNormalized());
    assertEquals("black", iterator.next().getNormalized());
    assertEquals("white", iterator.next().getNormalized());
    assertEquals("and", iterator.next().getNormalized());
    assertEquals("white", iterator.next().getNormalized());
    assertEquals("black", iterator.next().getNormalized());
    assertEquals("cat", iterator.next().getNormalized());
    */
  }
}
