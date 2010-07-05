package eu.interedition.collatex2.experimental.graph;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Iterator;
import java.util.Set;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import eu.interedition.collatex2.implementation.CollateXEngine;
import eu.interedition.collatex2.interfaces.IWitness;

public class VariantGraph2AlignmentTest {
    private static CollateXEngine engine;

    @BeforeClass
    public static void setup() {
      engine = new CollateXEngine();
    }

    @Test
    public void testEmptyGraph() {
      IVariantGraph graph = VariantGraph2.create();
      assertEquals(2, graph.vertexSet().size());
      IVariantGraphVertex startVertex = graph.getStartVertex();
      assertEquals("#", startVertex.getNormalized());
      IVariantGraphVertex endVertex = graph.getEndVertex();
      assertEquals("#", endVertex.getNormalized());
      assertEquals(0, graph.edgeSet().size());
      assertEquals(0, graph.getWitnesses().size());
      assertTrue(graph.isEmpty());
    }
    
    @Test
    public void testOneWitness() {
      IWitness a = engine.createWitness("A", "only one witness");   
      IVariantGraph graph = VariantGraph2.create(a);
      final Set<IVariantGraphVertex> vertices = graph.vertexSet();
      assertEquals(5, vertices.size());
      Iterator<IVariantGraphVertex> vertexI = graph.iterator();
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
      Set<IVariantGraphEdge> edges = graph.edgeSet();
      assertEquals(4, edges.size());
      Iterator<IVariantGraphEdge> edgeI = edges.iterator();
      assertTrue(edgeI.next().getWitnesses().contains(a));
      assertTrue(edgeI.next().getWitnesses().contains(a));
      assertTrue(edgeI.next().getWitnesses().contains(a));
      assertTrue(edgeI.next().getWitnesses().contains(a));
      assertTrue(graph.containsEdge(startVertex, firstVertex));
      assertTrue(graph.containsEdge(firstVertex, secondVertex));
      assertTrue(graph.containsEdge(secondVertex, thirdVertex));
      assertTrue(graph.containsEdge(thirdVertex, endVertex));
    }
    


    @Ignore
    @Test
    public void testSimpleTranspositionAB() {
      IWitness a = engine.createWitness("A", "a b");
      IWitness b = engine.createWitness("B", "b a");
      IVariantGraph graph = VariantGraph2.create();
      graph.addWitness(a);
      graph.addWitness(b);
      Iterator<IVariantGraphVertex> iterator = graph.iterator();
      assertEquals("a", iterator.next().getNormalized());
      assertEquals("b", iterator.next().getNormalized());
      assertEquals("a", iterator.next().getNormalized());
      System.out.println(graph.vertexSet().size());
    }

}
