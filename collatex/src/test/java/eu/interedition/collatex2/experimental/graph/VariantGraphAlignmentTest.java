package eu.interedition.collatex2.experimental.graph;

import static org.junit.Assert.assertEquals;

import java.util.List;
import java.util.Set;

import org.junit.BeforeClass;
import org.junit.Test;

import eu.interedition.collatex2.implementation.CollateXEngine;
import eu.interedition.collatex2.interfaces.IWitness;

public class VariantGraphAlignmentTest {
  private static CollateXEngine engine;

  @BeforeClass
  public static void setup() {
    engine = new CollateXEngine();
  }

  /* The unit test below depend on the correct functioning
   * of the GraphIndexMatcher
   */

  @Test
  public void testTwoWitnesses() {
    final IWitness w1 = engine.createWitness("A", "the black cat");
    final IWitness w2 = engine.createWitness("B", "the black cat");
    IVariantGraph graph = VariantGraph.create(w1);
    graph.addWitness(w2);
    final Set<IVariantGraphVertex> vertices = graph.vertexSet();
    assertEquals(5, vertices.size());
    List<IVariantGraphEdge> edges = graph.getEdges();
    assertEquals(4, edges.size());
    assertEquals("# -> the: A, B", edges.get(0).toString());
    assertEquals("the -> black: A, B", edges.get(1).toString());
    assertEquals("black -> cat: A, B", edges.get(2).toString());
    assertEquals("cat -> #: A, B", edges.get(3).toString());
  }

  @Test
  public void testAddition1() {
    final IWitness w1 = engine.createWitness("A", "the black cat");
    final IWitness w2 = engine.createWitness("B", "the white and black cat");
    IVariantGraph graph = VariantGraph.create(w1);
    graph.addWitness(w2);
    final Set<IVariantGraphVertex> vertices = graph.vertexSet();
    assertEquals(7, vertices.size());
    List<IVariantGraphEdge> edges = graph.getEdges();
    assertEquals(7, edges.size());
    assertEquals("# -> the: A, B", edges.get(0).toString());
    assertEquals("the -> black: A", edges.get(1).toString());
    assertEquals("black -> cat: A, B", edges.get(2).toString());
    assertEquals("cat -> #: A, B", edges.get(3).toString());
    assertEquals("the -> white: B", edges.get(4).toString());
    assertEquals("white -> and: B", edges.get(5).toString());
    assertEquals("and -> black: B", edges.get(6).toString());
  }

  @Test
  public void testVariant() {
    final IWitness w1 = engine.createWitness("A", "the black cat");
    final IWitness w2 = engine.createWitness("B", "the white cat");
    final IWitness w3 = engine.createWitness("C", "the green cat");
    final IWitness w4 = engine.createWitness("D", "the red cat");
    final IWitness w5 = engine.createWitness("E", "the yellow cat");
    IVariantGraph graph = VariantGraph.create(w1);
    graph.addWitness(w2);
    graph.addWitness(w3);
    graph.addWitness(w4);
    graph.addWitness(w5);
    final Set<IVariantGraphVertex> vertices = graph.vertexSet();
    assertEquals(9, vertices.size());
    List<IVariantGraphEdge> edges = graph.getEdges();
    assertEquals(12, edges.size());
    assertEquals("# -> the: A, B, C, D, E", edges.get(0).toString());
    assertEquals("the -> black: A", edges.get(1).toString());
    assertEquals("black -> cat: A", edges.get(2).toString());
    assertEquals("cat -> #: A, B, C, D, E", edges.get(3).toString());
    assertEquals("the -> white: B", edges.get(4).toString());
    assertEquals("white -> cat: B", edges.get(5).toString());
    assertEquals("the -> green: C", edges.get(6).toString());
    assertEquals("green -> cat: C", edges.get(7).toString());
    assertEquals("the -> red: D", edges.get(8).toString());
    assertEquals("red -> cat: D", edges.get(9).toString());
    assertEquals("the -> yellow: E", edges.get(10).toString());
    assertEquals("yellow -> cat: E", edges.get(11).toString());
  }

}
