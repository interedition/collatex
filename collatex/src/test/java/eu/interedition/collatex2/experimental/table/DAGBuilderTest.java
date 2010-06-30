package eu.interedition.collatex2.experimental.table;

import java.util.Iterator;
import java.util.Set;

import junit.framework.Assert;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import eu.interedition.collatex2.experimental.graph.IVariantGraph;
import eu.interedition.collatex2.experimental.graph.VariantGraph;
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
      IVariantGraph graph = VariantGraph.create();
      graph.addWitness(a);
      DAGBuilder builder = new DAGBuilder();
      DAVariantGraph dag = builder.buildDAG(graph);
      // vertices
      Iterator<CollateXVertex> iterator = dag.iterator();
      CollateXVertex start = iterator.next(); 
      CollateXVertex the = iterator.next(); 
      CollateXVertex first = iterator.next(); 
      CollateXVertex witness = iterator.next(); 
      CollateXVertex end = iterator.next(); 
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
      Assert.assertTrue(dag.containsEdge(start, the));
      Assert.assertTrue(dag.containsEdge(the, first));
      Assert.assertTrue(dag.containsEdge(first, witness));
      Assert.assertTrue(dag.containsEdge(witness, end));
      // witnesses on edges
      Set<CollateXEdge> edgeSet = dag.edgeSet();
      for (CollateXEdge edge : edgeSet) {
        Assert.assertTrue("Witness "+a.getSigil()+" not present in set!", edge.containsWitness(a));
      }
  }
  
  @Ignore
  @Test
  public void testSimpleTranspositionAB() {
    IWitness a = engine.createWitness("A", "a b");
    IWitness b = engine.createWitness("B", "b a");
    IVariantGraph graph = VariantGraph.create();
    graph.addWitness(a);
    graph.addWitness(b);
    DAGBuilder builder = new DAGBuilder();
    DAVariantGraph dag = builder.buildDAG(graph);
    System.out.println(dag.vertexSet().size());
  }
}
