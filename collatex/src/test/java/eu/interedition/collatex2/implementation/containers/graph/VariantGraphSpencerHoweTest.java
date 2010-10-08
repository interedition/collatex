package eu.interedition.collatex2.implementation.containers.graph;

import static org.junit.Assert.assertEquals;

import java.util.Iterator;
import java.util.Set;

import org.junit.BeforeClass;
import org.junit.Test;

import eu.interedition.collatex2.implementation.CollateXEngine;
import eu.interedition.collatex2.implementation.containers.graph.VariantGraph2Creator;
import eu.interedition.collatex2.interfaces.IVariantGraph;
import eu.interedition.collatex2.interfaces.IVariantGraphEdge;
import eu.interedition.collatex2.interfaces.IVariantGraphVertex;
import eu.interedition.collatex2.interfaces.IWitness;

public class VariantGraphSpencerHoweTest {
  private static CollateXEngine engine;

  @BeforeClass
  public static void setup() {
    engine = new CollateXEngine();
  }

  //Note: this only tests the Graph, not the table!
  @Test
  public void testSimpleSpencerHowe() {
    IWitness w1 = engine.createWitness("A", "a");
    IWitness w2 = engine.createWitness("B", "b");
    IWitness w3 = engine.createWitness("C", "a b");
    IVariantGraph graph = VariantGraph2Creator.create(w1, w2, w3);
    assertEquals(4, graph.vertexSet().size());
    Iterator<IVariantGraphVertex> vertexI = graph.iterator();
    IVariantGraphVertex startVertex = vertexI.next();
    IVariantGraphVertex aVertex = vertexI.next();
    IVariantGraphVertex bVertex = vertexI.next();
    IVariantGraphVertex endVertex = vertexI.next();
    final Set<IVariantGraphEdge> edges = graph.edgeSet();
    assertEquals(5, edges.size());
    assertEquals("# -> a: A, C", graph.getEdge(startVertex, aVertex).toString());
    assertEquals("a -> #: A", graph.getEdge(aVertex, endVertex).toString());
    assertEquals("# -> b: B", graph.getEdge(startVertex, bVertex).toString());
    assertEquals("b -> #: B, C", graph.getEdge(bVertex, endVertex).toString());
    assertEquals("a -> b: C", graph.getEdge(aVertex, bVertex).toString());
  }
  



}
