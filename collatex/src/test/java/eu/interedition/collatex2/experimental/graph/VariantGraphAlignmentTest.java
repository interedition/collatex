package eu.interedition.collatex2.experimental.graph;

import static org.junit.Assert.assertEquals;

import java.util.List;

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

  // TODO: we should add an end node to the graph!
  @Test
  public void testEmptyGraph() {
    IVariantGraph graph = VariantGraph.create();
    assertEquals(1, graph.getNodes().size());
    IVariantGraphNode startNode = graph.getStartNode();
    assertEquals("#", startNode.getNormalized());
    assertEquals(0, graph.getEdges().size());
  }

  // TODO: we should add an end node to the graph!
  @Test
  public void testOneWitness() {
    IWitness a = engine.createWitness("A", "only one witness");
    IVariantGraph graph = VariantGraph.create(a);
    final List<IVariantGraphNode> nodes = graph.getNodes();
    assertEquals(4, nodes.size());
    final IVariantGraphNode startNode = nodes.get(0);
    final IVariantGraphNode firstNode = nodes.get(1);
    final IVariantGraphNode secondNode = nodes.get(2);
    final IVariantGraphNode thirdNode = nodes.get(3);
    assertEquals("#", startNode.getNormalized());
    assertEquals("only", firstNode.getNormalized());
    assertEquals("one", secondNode.getNormalized());
    assertEquals("witness", thirdNode.getNormalized());
    List<IVariantGraphEdge> arcs = graph.getEdges();
    assertEquals(3, arcs.size());
    assert(arcs.get(0).getWitnesses().contains(a));
    assert(arcs.get(1).getWitnesses().contains(a));
    assert(arcs.get(2).getWitnesses().contains(a));
    assertEquals(startNode, arcs.get(0).getBeginNode());
    assertEquals(firstNode, arcs.get(0).getEndNode());
    assertEquals(firstNode, arcs.get(1).getBeginNode());
    assertEquals(secondNode, arcs.get(1).getEndNode());
    assertEquals(secondNode, arcs.get(2).getBeginNode());
    assertEquals(thirdNode, arcs.get(2).getEndNode());
  }

  @Test
  public void testTwoWitnesses() {
    final IWitness w1 = engine.createWitness("A", "the black cat");
    final IWitness w2 = engine.createWitness("B", "the black cat");
    VariantGraph graph = VariantGraph.create(w1);
    graph.addWitness(w2);
    final List<IVariantGraphNode> nodes = graph.getNodes();
    assertEquals(4, nodes.size());
    List<IVariantGraphEdge> arcs = graph.getEdges();
    assertEquals(3, arcs.size());
    assertEquals("# -> the: A, B", arcs.get(0).toString());
    assertEquals("the -> black: A, B", arcs.get(1).toString());
    assertEquals("black -> cat: A, B", arcs.get(2).toString());
  }

  @Test
  public void testAddition1() {
    final IWitness w1 = engine.createWitness("A", "the black cat");
    final IWitness w2 = engine.createWitness("B", "the white and black cat");
    VariantGraph graph = VariantGraph.create(w1);
    graph.addWitness(w2);
    final List<IVariantGraphNode> nodes = graph.getNodes();
    assertEquals(6, nodes.size());
    List<IVariantGraphEdge> arcs = graph.getEdges();
    assertEquals(6, arcs.size());
    assertEquals("# -> the: A, B", arcs.get(0).toString());
    assertEquals("the -> black: A", arcs.get(1).toString());
    assertEquals("the -> white: B", arcs.get(2).toString());
    assertEquals("black -> cat: A, B", arcs.get(3).toString());
    assertEquals("white -> and: B", arcs.get(4).toString());
    assertEquals("and -> black: B", arcs.get(5).toString());
  }

  @Test
  public void testVariant() {
    final IWitness w1 = engine.createWitness("A", "the black cat");
    final IWitness w2 = engine.createWitness("B", "the white cat");
    final IWitness w3 = engine.createWitness("C", "the green cat");
    final IWitness w4 = engine.createWitness("D", "the red cat");
    final IWitness w5 = engine.createWitness("E", "the yellow cat");
    VariantGraph graph = VariantGraph.create(w1);
    graph.addWitness(w2);
    graph.addWitness(w3);
    graph.addWitness(w4);
    graph.addWitness(w5);
    final List<IVariantGraphNode> nodes = graph.getNodes();
    assertEquals(8, nodes.size());
    List<IVariantGraphEdge> arcs = graph.getEdges();
    assertEquals(11, arcs.size());
    assertEquals("# -> the: A, B, C, D, E", arcs.get(0).toString());
    assertEquals("the -> black: A", arcs.get(1).toString());
    assertEquals("the -> white: B", arcs.get(2).toString());
    assertEquals("the -> green: C", arcs.get(3).toString());
    assertEquals("the -> red: D", arcs.get(4).toString());
    assertEquals("the -> yellow: E", arcs.get(5).toString());
    assertEquals("black -> cat: A", arcs.get(6).toString());
    assertEquals("white -> cat: B", arcs.get(7).toString());
    assertEquals("green -> cat: C", arcs.get(8).toString());
    assertEquals("red -> cat: D", arcs.get(9).toString());
    assertEquals("yellow -> cat: E", arcs.get(10).toString());
  }

}
