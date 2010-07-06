package eu.interedition.collatex2.experimental.table;

import java.util.Iterator;
import java.util.Set;

import junit.framework.Assert;

import org.junit.BeforeClass;
import org.junit.Test;

import eu.interedition.collatex2.experimental.graph.IVariantGraph;
import eu.interedition.collatex2.experimental.graph.IVariantGraphEdge;
import eu.interedition.collatex2.experimental.graph.IVariantGraphVertex;
import eu.interedition.collatex2.experimental.graph.VariantGraph2;
import eu.interedition.collatex2.implementation.CollateXEngine;
import eu.interedition.collatex2.interfaces.IWitness;

public class DAGBuilderTest {
  private static CollateXEngine engine;

  @BeforeClass
  public static void setup() {
    engine = new CollateXEngine();
  }

  @Test
  public void testSimpleVariantGraphToDAG() {
      IWitness a = engine.createWitness("A", "the first witness");
      IVariantGraph graph = VariantGraph2.create();
      graph.addWitness(a);
      // vertices
      Iterator<IVariantGraphVertex> iterator = graph.iterator();
      IVariantGraphVertex start = iterator.next(); 
      IVariantGraphVertex the = iterator.next(); 
      IVariantGraphVertex first = iterator.next(); 
      IVariantGraphVertex witness = iterator.next(); 
      IVariantGraphVertex end = iterator.next(); 
      Assert.assertFalse(iterator.hasNext());
      Assert.assertEquals("#", start.getNormalized());       
      Assert.assertEquals("the", the.getNormalized());
      Assert.assertEquals("first", first.getNormalized());
      Assert.assertEquals("witness", witness.getNormalized());
      Assert.assertEquals("#", end.getNormalized());       
      // tokens on vertices
      Assert.assertEquals("the", the.getToken(a).getContent());
      Assert.assertEquals("first", first.getToken(a).getContent());
      Assert.assertEquals("witness", witness.getToken(a).getContent());
      // edges
      Assert.assertTrue(graph.containsEdge(start, the));
      Assert.assertTrue(graph.containsEdge(the, first));
      Assert.assertTrue(graph.containsEdge(first, witness));
      Assert.assertTrue(graph.containsEdge(witness, end));
      // witnesses on edges
      Set<IVariantGraphEdge> edgeSet = graph.edgeSet();
      for (IVariantGraphEdge edge : edgeSet) {
        Assert.assertTrue("Witness "+a.getSigil()+" not present in set!", edge.containsWitness(a));
      }
  }
  
}
