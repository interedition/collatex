package eu.interedition.collatex2.implementation.containers.graph;

import static org.junit.Assert.assertEquals;

import java.util.Iterator;
import java.util.Set;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import eu.interedition.collatex2.implementation.CollateXEngine;
import eu.interedition.collatex2.implementation.containers.graph.VariantGraph2Creator;
import eu.interedition.collatex2.interfaces.IVariantGraph;
import eu.interedition.collatex2.interfaces.IVariantGraphEdge;
import eu.interedition.collatex2.interfaces.IVariantGraphVertex;
import eu.interedition.collatex2.interfaces.IWitness;

public class VariantGraph2CreatorTest {
    private static CollateXEngine engine;

    @BeforeClass
    public static void setup() {
      engine = new CollateXEngine();
    }


    /* The unit test below depend on the correct functioning
     * of the GraphIndexMatcher
     */
    
    // NOTE: these tests test the VariantGraph2Creator class

    @Test
    public void testTwoWitnesses() {
      final IWitness w1 = engine.createWitness("A", "the black cat");
      final IWitness w2 = engine.createWitness("B", "the black cat");
      IVariantGraph graph = VariantGraph2Creator.create(w1, w2);
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
      assertEquals("# -> the: A, B", graph.getEdge(startVertex, theVertex).toString());
      assertEquals("the -> black: A, B", graph.getEdge(theVertex, blackVertex).toString());
      assertEquals("black -> cat: A, B", graph.getEdge(blackVertex, catVertex).toString());
      assertEquals("cat -> #: A, B", graph.getEdge(catVertex, endVertex).toString());
    }


    @Test
    public void testAddition1() {
      final IWitness w1 = engine.createWitness("A", "the black cat");
      final IWitness w2 = engine.createWitness("B", "the white and black cat");
      IVariantGraph graph = VariantGraph2Creator.create(w1, w2);
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
      assertEquals("# -> the: A, B", graph.getEdge(startVertex, theVertex).toString());
      assertEquals("the -> black: A", graph.getEdge(theVertex, blackVertex).toString());
      assertEquals("black -> cat: A, B", graph.getEdge(blackVertex, catVertex).toString());
      assertEquals("cat -> #: A, B", graph.getEdge(catVertex, endVertex).toString());
      assertEquals("the -> white: B", graph.getEdge(theVertex, whiteVertex).toString());
      assertEquals("white -> and: B", graph.getEdge(whiteVertex, andVertex).toString());
      assertEquals("and -> black: B", graph.getEdge(andVertex, blackVertex).toString());
    }
    
    @Test
    public void testVariant() {
      final IWitness w1 = engine.createWitness("A", "the black cat");
      final IWitness w2 = engine.createWitness("B", "the white cat");
      final IWitness w3 = engine.createWitness("C", "the green cat");
      final IWitness w4 = engine.createWitness("D", "the red cat");
      final IWitness w5 = engine.createWitness("E", "the yellow cat");
      IVariantGraph graph = VariantGraph2Creator.create(w1, w2, w3, w4, w5);
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
      assertEquals("# -> the: A, B, C, D, E", graph.getEdge(startVertex, theVertex).toString());
      assertEquals("the -> black: A", graph.getEdge(theVertex, blackVertex).toString());
      assertEquals("black -> cat: A", graph.getEdge(blackVertex, catVertex).toString());
      assertEquals("cat -> #: A, B, C, D, E", graph.getEdge(catVertex, endVertex).toString());
      assertEquals("the -> white: B", graph.getEdge(theVertex, whiteVertex).toString());
      assertEquals("white -> cat: B", graph.getEdge(whiteVertex, catVertex).toString());
      assertEquals("the -> green: C", graph.getEdge(theVertex, greenVertex).toString());
      assertEquals("green -> cat: C", graph.getEdge(greenVertex, catVertex).toString());
      assertEquals("the -> red: D", graph.getEdge(theVertex, redVertex).toString());
      assertEquals("red -> cat: D", graph.getEdge(redVertex, catVertex).toString());
      assertEquals("the -> yellow: E", graph.getEdge(theVertex, yellowVertex).toString());
      assertEquals("yellow -> cat: E", graph.getEdge(yellowVertex, catVertex).toString());
   }


    //NOTE: test taken from AlignmentTableTranspositionTest
    @Ignore
    @Test
    public void testDoubleTransposition2() {
      IWitness a = engine.createWitness("A", "a b");
      IWitness b = engine.createWitness("B", "b a");
      IVariantGraph graph = VariantGraph2Creator.create(a, b);
      Iterator<IVariantGraphVertex> iterator = graph.iterator();
      assertEquals("a", iterator.next().getNormalized());
      assertEquals("b", iterator.next().getNormalized());
      assertEquals("a", iterator.next().getNormalized());
      System.out.println(graph.vertexSet().size());
    }

}
