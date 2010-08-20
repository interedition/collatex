package eu.interedition.collatex2.implementation.containers.graph;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

import junit.framework.Assert;

import org.junit.BeforeClass;
import org.junit.Test;

import eu.interedition.collatex2.implementation.CollateXEngine;
import eu.interedition.collatex2.implementation.containers.graph.VariantGraph2;
import eu.interedition.collatex2.implementation.containers.graph.VariantGraph2Creator;
import eu.interedition.collatex2.interfaces.IVariantGraph;
import eu.interedition.collatex2.interfaces.IVariantGraphEdge;
import eu.interedition.collatex2.interfaces.IVariantGraphVertex;
import eu.interedition.collatex2.interfaces.IWitness;

public class VariantGraph2Test {
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
  
  @Test
  public void testFirstWitness() {
    IWitness a = engine.createWitness("A", "the first witness");
    IVariantGraph graph = VariantGraph2.create(a);
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

  @Test
  public void testRepeatingTokensWithOneWitness() {
    final IWitness witness = engine.createWitness("a", "a c a t g c a");
    final IVariantGraph graph = engine.graph(witness);
    final List<String> repeatingTokens = graph.getRepeatedTokens();
    assertEquals(2, repeatingTokens.size());
    assertTrue(repeatingTokens.contains("a"));
    assertTrue(repeatingTokens.contains("c"));
    assertFalse(repeatingTokens.contains("t"));
    assertFalse(repeatingTokens.contains("g"));
  }

  @Test
  public void testRepeatingTokensWithMultipleWitnesses() {
    final IWitness witnessA = engine.createWitness("a", "a c a t g c a");
    final IWitness witnessB = engine.createWitness("b", "a c a t t c a");
    final IVariantGraph graph = engine.graph(witnessA, witnessB);
    final List<String> repeatingTokens = graph.getRepeatedTokens();
    assertEquals(3, repeatingTokens.size());
    assertTrue(repeatingTokens.contains("a"));
    assertTrue(repeatingTokens.contains("c"));
    assertTrue(repeatingTokens.contains("t"));
    assertFalse(repeatingTokens.contains("g"));
  }

  @Test
  public void testRepeatingTokensWithMultipleWitnesses2() {
    final IWitness witnessA = engine.createWitness("A", "everything is unique should be no problem");
    final IWitness witnessB = engine.createWitness("B", "this one very different");
    final IVariantGraph graph = engine.graph(witnessA, witnessB);
    final List<String> repeatingTokens = graph.getRepeatedTokens();
    assertEquals(0, repeatingTokens.size());
  }


  @Test
  public void testLongestPath() {
    IWitness w1 = engine.createWitness("A", "a");
    IWitness w2 = engine.createWitness("B", "b");
    IWitness w3 = engine.createWitness("C", "a b");
    IVariantGraph graph = VariantGraph2Creator.create(w1, w2, w3);
    assertEquals(4, graph.vertexSet().size());
    List<IVariantGraphVertex> longestPath = graph.getLongestPath();
//    for (CollateXVertex v: longestPath) {
//      System.out.println(v.getNormalized());
//    }
    assertEquals("a", longestPath.get(0).getNormalized());
    assertEquals("b", longestPath.get(1).getNormalized());
    assertEquals(2, longestPath.size());
  }

  @Test
  public void testGetPathForWitness() {
    final IWitness w1 = engine.createWitness("V", "a b c d e f ");
    final IWitness w2 = engine.createWitness("W", "x y z d e");
    final IWitness w3 = engine.createWitness("X", "a b x y z");
    IVariantGraph graph = VariantGraph2Creator.create(w1, w2, w3);
    List<IVariantGraphVertex> path = graph.getPath(w1);
    assertEquals("a", path.get(0).getNormalized());
    assertEquals("b", path.get(1).getNormalized());
    assertEquals("c", path.get(2).getNormalized());
    assertEquals("d", path.get(3).getNormalized());
    assertEquals("e", path.get(4).getNormalized());
    assertEquals("f", path.get(5).getNormalized());
    assertEquals(6, path.size());
  }

}
