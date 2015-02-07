/*
 * Copyright (c) 2013 The Interedition Development Group.
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

package eu.interedition.collatex.dekker;

import eu.interedition.collatex.AbstractTest;
import eu.interedition.collatex.VariantGraph;
import eu.interedition.collatex.simple.SimpleWitness;
import org.junit.Test;

/**
 * todo Add test with an addition or omission in between!
 */
public class VariantGraphTest extends AbstractTest {

  @Test
  public void twoWitnesses() {
    final SimpleWitness[] w = createWitnesses("the black cat", "the black cat");
    final VariantGraph graph = collate(w);

    assetGraphSize(graph, 5, 4);

    final VariantGraph.Vertex theVertex = vertexWith(graph, "the", w[0]);
    final VariantGraph.Vertex blackVertex = vertexWith(graph, "black", w[0]);
    final VariantGraph.Vertex catVertex = vertexWith(graph, "cat", w[0]);

    assertHasWitnesses(edgeBetween(graph.getStart(), theVertex), w[0], w[1]);
    assertHasWitnesses(edgeBetween(theVertex, blackVertex), w[0], w[1]);
    assertHasWitnesses(edgeBetween(blackVertex, catVertex), w[0], w[1]);
    assertHasWitnesses(edgeBetween(catVertex, graph.getEnd()), w[0], w[1]);
  }

  @Test
  public void addition1() {
    final SimpleWitness[] w = createWitnesses("the black cat", "the white and black cat");
    final VariantGraph graph = collate(w);

    assetGraphSize(graph, 7, 7);

    final VariantGraph.Vertex theVertex = vertexWith(graph, "the", w[0]);
    final VariantGraph.Vertex whiteVertex = vertexWith(graph, "white", w[1]);
    final VariantGraph.Vertex andVertex = vertexWith(graph, "and", w[1]);
    final VariantGraph.Vertex blackVertex = vertexWith(graph, "black", w[0]);
    final VariantGraph.Vertex catVertex = vertexWith(graph, "cat", w[0]);

    assertHasWitnesses(edgeBetween(graph.getStart(), theVertex), w[0], w[1]);
    assertHasWitnesses(edgeBetween(theVertex, blackVertex), w[0]);
    assertHasWitnesses(edgeBetween(blackVertex, catVertex), w[0], w[1]);
    assertHasWitnesses(edgeBetween(catVertex, graph.getEnd()), w[0], w[1]);
    assertHasWitnesses(edgeBetween(theVertex, whiteVertex), w[1]);
    assertHasWitnesses(edgeBetween(whiteVertex, andVertex), w[1]);
    assertHasWitnesses(edgeBetween(andVertex, blackVertex), w[1]);
  }

  @Test
  public void variant() {
    final SimpleWitness[] w = createWitnesses("the black cat", "the white cat", "the green cat", "the red cat", "the yellow cat");
    final VariantGraph graph = collate(w);

    assetGraphSize(graph, 9, 12);

    final VariantGraph.Vertex theVertex = vertexWith(graph, "the", w[0]);
    final VariantGraph.Vertex blackVertex = vertexWith(graph, "black", w[0]);
    final VariantGraph.Vertex whiteVertex = vertexWith(graph, "white", w[1]);
    final VariantGraph.Vertex greenVertex = vertexWith(graph, "green", w[2]);
    final VariantGraph.Vertex redVertex = vertexWith(graph, "red", w[3]);
    final VariantGraph.Vertex yellowVertex = vertexWith(graph, "yellow", w[4]);
    final VariantGraph.Vertex catVertex = vertexWith(graph, "cat", w[0]);

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
    final SimpleWitness[] w = createWitnesses("a b", "b a");
    final VariantGraph graph = collate(w);

    assertGraphVertices(graph, 5);

    assertHasWitnesses(edgeBetween(vertexWith(graph, "b", w[1]), vertexWith(graph, "a", w[1])), w[1]);
    assertHasWitnesses(edgeBetween(vertexWith(graph, "a", w[0]), vertexWith(graph, "b", w[0])), w[0]);
  }

  @Test
  public void mirroredTranspositionsWithMatchInBetween() {
    final SimpleWitness[] w = createWitnesses("the black and white cat", "the white and black cat");
    final VariantGraph graph = collate(w);

    assertGraphVertices(graph, 9);

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

  //  @Test
  //  public void transpositionJoin() {
  //    final SimpleWitness[] w = createWitnesses("pre a b c d post", "pre c d a b post");
  //    final VariantGraph graph = collate(w).join();
  //    Iterable<VariantGraphVertex> vertices = graph.vertices();
  //    //    assertEquals(2, Iterables.size(vertices));
  //    Iterator<VariantGraphVertex> iterator = vertices.iterator();
  //    for (VariantGraphVertex variantGraphVertex : vertices) {
  //      LOG.debug("{}", variantGraphVertex);
  //    }
  //    assertEquals("[]", iterator.next().toString());
  //    assertEquals("[A:0:'pre', B:0:'pre']", iterator.next().toString());
  //    assertEquals("[B:1:'b']", iterator.next().toString());
  //    assertEquals("[A:1:'a', B:2:'a']", iterator.next().toString());
  //    assertEquals("[A:1:'b']", iterator.next().toString());
  //    assertEquals("[A:0:'post', B:0:'post']", iterator.next().toString());
  //    assertEquals("[]", iterator.next().toString());
  //  }
}
