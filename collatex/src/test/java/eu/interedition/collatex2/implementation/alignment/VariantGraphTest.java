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

package eu.interedition.collatex2.implementation.alignment;

import eu.interedition.collatex2.AbstractTest;
import eu.interedition.collatex2.interfaces.IVariantGraph;
import eu.interedition.collatex2.interfaces.IVariantGraphEdge;
import eu.interedition.collatex2.interfaces.IVariantGraphVertex;
import org.junit.Test;

import java.util.Iterator;
import java.util.Set;

import static org.junit.Assert.assertEquals;

/**
 * @todo Add test with an addition or omission in between!
 */
public class VariantGraphTest extends AbstractTest {

  @Test
  public void twoWitnesses() {
    final IVariantGraph graph = merge("the black cat", "the black cat");

    final Set<IVariantGraphVertex> vertices = graph.vertexSet();
    assertEquals(5, vertices.size());

    final Iterator<IVariantGraphVertex> vertexI = graph.iterator();
    final IVariantGraphVertex startVertex = vertexI.next();
    final IVariantGraphVertex theVertex = vertexI.next();
    final IVariantGraphVertex blackVertex = vertexI.next();
    final IVariantGraphVertex catVertex = vertexI.next();
    final IVariantGraphVertex endVertex = vertexI.next();

    final Set<IVariantGraphEdge> edges = graph.edgeSet();
    assertEquals(4, edges.size());

    assertEquals(": A, B", graph.getEdge(startVertex, theVertex).toString());
    assertEquals(": A, B", graph.getEdge(theVertex, blackVertex).toString());
    assertEquals(": A, B", graph.getEdge(blackVertex, catVertex).toString());
    assertEquals(": A, B", graph.getEdge(catVertex, endVertex).toString());
  }


  @Test
  public void addition1() {
    final IVariantGraph graph = merge("the black cat", "the white and black cat");

    final Set<IVariantGraphVertex> vertices = graph.vertexSet();
    assertEquals(7, vertices.size());

    final Iterator<IVariantGraphVertex> vertexI = graph.iterator();
    final IVariantGraphVertex startVertex = vertexI.next();
    final IVariantGraphVertex theVertex = vertexI.next();
    final IVariantGraphVertex whiteVertex = vertexI.next();
    final IVariantGraphVertex andVertex = vertexI.next();
    final IVariantGraphVertex blackVertex = vertexI.next();
    final IVariantGraphVertex catVertex = vertexI.next();
    final IVariantGraphVertex endVertex = vertexI.next();

    assertEquals("#", startVertex.getNormalized());
    assertEquals("the", theVertex.getNormalized());
    assertEquals("white", whiteVertex.getNormalized());
    assertEquals("and", andVertex.getNormalized());
    assertEquals("black", blackVertex.getNormalized());
    assertEquals("cat", catVertex.getNormalized());
    assertEquals("#", endVertex.getNormalized());

    final Set<IVariantGraphEdge> edges = graph.edgeSet();
    assertEquals(7, edges.size());

    assertEquals(": A, B", graph.getEdge(startVertex, theVertex).toString());
    assertEquals(": A", graph.getEdge(theVertex, blackVertex).toString());
    assertEquals(": A, B", graph.getEdge(blackVertex, catVertex).toString());
    assertEquals(": A, B", graph.getEdge(catVertex, endVertex).toString());
    assertEquals(": B", graph.getEdge(theVertex, whiteVertex).toString());
    assertEquals(": B", graph.getEdge(whiteVertex, andVertex).toString());
    assertEquals(": B", graph.getEdge(andVertex, blackVertex).toString());
  }

  @Test
  public void variant() {
    final IVariantGraph graph = merge("the black cat", "the white cat", "the green cat", "the red cat", "the yellow cat");

    final Set<IVariantGraphVertex> vertices = graph.vertexSet();
    assertEquals(9, vertices.size());

    final Iterator<IVariantGraphVertex> vertexI = graph.iterator();
    final IVariantGraphVertex startVertex = vertexI.next();
    final IVariantGraphVertex theVertex = vertexI.next();
    final IVariantGraphVertex blackVertex = vertexI.next();
    final IVariantGraphVertex whiteVertex = vertexI.next();
    final IVariantGraphVertex greenVertex = vertexI.next();
    final IVariantGraphVertex redVertex = vertexI.next();
    final IVariantGraphVertex yellowVertex = vertexI.next();
    final IVariantGraphVertex catVertex = vertexI.next();
    final IVariantGraphVertex endVertex = vertexI.next();

    assertEquals("#", startVertex.getNormalized());
    assertEquals("the", theVertex.getNormalized());
    assertEquals("black", blackVertex.getNormalized());
    assertEquals("white", whiteVertex.getNormalized());
    assertEquals("green", greenVertex.getNormalized());
    assertEquals("red", redVertex.getNormalized());
    assertEquals("yellow", yellowVertex.getNormalized());
    assertEquals("cat", catVertex.getNormalized());
    assertEquals("#", endVertex.getNormalized());

    final Set<IVariantGraphEdge> edges = graph.edgeSet();
    assertEquals(12, edges.size());

    assertEquals(": A, B, C, D, E", graph.getEdge(startVertex, theVertex).toString());
    assertEquals(": A", graph.getEdge(theVertex, blackVertex).toString());
    assertEquals(": A", graph.getEdge(blackVertex, catVertex).toString());
    assertEquals(": A, B, C, D, E", graph.getEdge(catVertex, endVertex).toString());
    assertEquals(": B", graph.getEdge(theVertex, whiteVertex).toString());
    assertEquals(": B", graph.getEdge(whiteVertex, catVertex).toString());
    assertEquals(": C", graph.getEdge(theVertex, greenVertex).toString());
    assertEquals(": C", graph.getEdge(greenVertex, catVertex).toString());
    assertEquals(": D", graph.getEdge(theVertex, redVertex).toString());
    assertEquals(": D", graph.getEdge(redVertex, catVertex).toString());
    assertEquals(": E", graph.getEdge(theVertex, yellowVertex).toString());
    assertEquals(": E", graph.getEdge(yellowVertex, catVertex).toString());
  }

  @Test
  public void doubleTransposition2() {
    final IVariantGraph graph = merge("a b", "b a");
    assertEquals(5, graph.vertexSet().size());

    final Iterator<IVariantGraphVertex> iterator = graph.iterator();
    assertEquals("#", iterator.next().getNormalized());
    assertEquals("b", iterator.next().getNormalized());
    assertEquals("a", iterator.next().getNormalized());
    assertEquals("b", iterator.next().getNormalized());
    assertEquals("#", iterator.next().getNormalized());
  }

  @Test
  public void mirroredTranspositionsWithMatchInBetween() {
    final IVariantGraph graph = merge("the black and white cat", "the white and black cat");
    final Iterator<IVariantGraphVertex> iterator = graph.iterator();

    assertEquals("#", iterator.next().getNormalized());
    assertEquals("the", iterator.next().getNormalized());
    assertEquals("black", iterator.next().getNormalized());
    assertEquals("white", iterator.next().getNormalized());
    assertEquals("and", iterator.next().getNormalized());
    assertEquals("white", iterator.next().getNormalized());
    assertEquals("black", iterator.next().getNormalized());
    assertEquals("cat", iterator.next().getNormalized());
  }
}
