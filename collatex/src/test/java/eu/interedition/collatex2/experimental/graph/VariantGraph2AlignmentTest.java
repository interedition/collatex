package eu.interedition.collatex2.experimental.graph;

import static org.junit.Assert.assertEquals;

import java.util.Iterator;

import junit.framework.Assert;

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
      assertEquals(Boolean.TRUE, graph.isEmpty());
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
      Assert.assertEquals("a", iterator.next().getNormalized());
      Assert.assertEquals("b", iterator.next().getNormalized());
      Assert.assertEquals("a", iterator.next().getNormalized());
      System.out.println(graph.vertexSet().size());
    }

}
