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

package eu.interedition.collatex2.implementation.vg_alignment;

import static org.junit.Assert.assertEquals;

import java.util.Iterator;
import java.util.Set;

import org.junit.BeforeClass;
import org.junit.Test;

import eu.interedition.collatex2.implementation.CollateXEngine;
import eu.interedition.collatex2.interfaces.IVariantGraph;
import eu.interedition.collatex2.interfaces.IVariantGraphEdge;
import eu.interedition.collatex2.interfaces.IVariantGraphVertex;
import eu.interedition.collatex2.interfaces.IWitness;

public class VariantGraphAlignerTest {
    private static CollateXEngine engine;

    @BeforeClass
    public static void setup() {
      engine = new CollateXEngine();
    }


    /* NOTE: these tests test the VariantGraphAligner class
     * The unit test below depend on the correct functioning
     * of the GraphIndexMatcher
     */
    
    @Test
    public void testTwoWitnesses() {
      final IWitness w1 = engine.createWitness("A", "the black cat");
      final IWitness w2 = engine.createWitness("B", "the black cat");
      IVariantGraph graph = engine.graph(w1, w2);
      final Set<IVariantGraphVertex> vertices = graph.vertexSet();
      assertEquals(5, vertices.size());
      Iterator<IVariantGraphVertex> vertexI = graph.iterator();
      final IVariantGraphVertex startVertex = vertexI.next();
      final IVariantGraphVertex theVertex = vertexI.next();
      final IVariantGraphVertex blackVertex = vertexI.next();
      final IVariantGraphVertex catVertex = vertexI.next();
      final IVariantGraphVertex endVertex = vertexI.next();
      Set<IVariantGraphEdge> edges = graph.edgeSet();
      assertEquals(4, edges.size());
      assertEquals(": A, B", graph.getEdge(startVertex, theVertex).toString());
      assertEquals(": A, B", graph.getEdge(theVertex, blackVertex).toString());
      assertEquals(": A, B", graph.getEdge(blackVertex, catVertex).toString());
      assertEquals(": A, B", graph.getEdge(catVertex, endVertex).toString());
    }


    @Test
    public void testAddition1() {
      final IWitness w1 = engine.createWitness("A", "the black cat");
      final IWitness w2 = engine.createWitness("B", "the white and black cat");
      IVariantGraph graph = engine.graph(w1, w2);
      final Set<IVariantGraphVertex> vertices = graph.vertexSet();
      assertEquals(7, vertices.size());
      Iterator<IVariantGraphVertex> vertexI = graph.iterator();
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
      Set<IVariantGraphEdge> edges = graph.edgeSet();
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
    public void testVariant() {
      final IWitness w1 = engine.createWitness("A", "the black cat");
      final IWitness w2 = engine.createWitness("B", "the white cat");
      final IWitness w3 = engine.createWitness("C", "the green cat");
      final IWitness w4 = engine.createWitness("D", "the red cat");
      final IWitness w5 = engine.createWitness("E", "the yellow cat");
      IVariantGraph graph = engine.graph(w1, w2, w3, w4, w5);
      final Set<IVariantGraphVertex> vertices = graph.vertexSet();
      assertEquals(9, vertices.size());
      Iterator<IVariantGraphVertex> vertexI = graph.iterator();
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
      Set<IVariantGraphEdge> edges = graph.edgeSet();
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


    //NOTE: test taken from AlignmentTableTranspositionTest
    @Test
    public void testDoubleTransposition2() {
      IWitness a = engine.createWitness("A", "a b");
      IWitness b = engine.createWitness("B", "b a");
      IVariantGraph graph = engine.graph(a, b);
      Iterator<IVariantGraphVertex> iterator = graph.iterator();
      assertEquals("#", iterator.next().getNormalized());
      assertEquals("b", iterator.next().getNormalized());
      assertEquals("a", iterator.next().getNormalized());
      assertEquals("b", iterator.next().getNormalized());
      assertEquals("#", iterator.next().getNormalized());
      assertEquals(5, graph.vertexSet().size());
    }

    //TODO: add test with an addition or omission in between!
    
    //NOTE: test taken from AlignmentTableTranspositionTest
    @Test
    public void testMirroredTranspositionsWithMatchInBetween() {
      final IWitness a = engine.createWitness("A", "the black and white cat");
      final IWitness b = engine.createWitness("B", "the white and black cat");
      IVariantGraph graph = engine.graph(a, b);
      Iterator<IVariantGraphVertex> iterator = graph.iterator();
      assertEquals("#", iterator.next().getNormalized());
      assertEquals("the", iterator.next().getNormalized());
      assertEquals("black", iterator.next().getNormalized());
      assertEquals("white", iterator.next().getNormalized());
      assertEquals("and", iterator.next().getNormalized());
      assertEquals("white", iterator.next().getNormalized());
      assertEquals("black", iterator.next().getNormalized());
      assertEquals("cat", iterator.next().getNormalized());
//       final IAlignmentTable alignmentTable = engine.align(a, b);
//      String expected = "A: the|black|and|white|cat\n";
//      expected += "B: the|white|and|black|cat\n";
//      assertEquals(expected, alignmentTable.toString());
    }

}
