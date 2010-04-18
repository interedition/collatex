package eu.interedition.collatex2.experimental.graph;

import java.util.List;

import junit.framework.Assert;

import org.junit.BeforeClass;
import org.junit.Test;

import eu.interedition.collatex2.implementation.CollateXEngine;
import eu.interedition.collatex2.interfaces.IWitness;

public class GraphTest {
  private static CollateXEngine engine;
  
  @BeforeClass
  public static void setup() {
    engine = new CollateXEngine();
  }
  
  //TODO: we should add an end node to the graph!
  @Test
  public void testEmptyGraph() {
    IAlignmentGraph graph = AlignmentGraph.create();
    Assert.assertEquals(1, graph.getNodes().size());
    IAlignmentNode startNode = graph.getStartNode();
    Assert.assertEquals("#", startNode.getNormalized());
    Assert.assertEquals(0, graph.getArcs().size());
  }
  
  //TODO: we should add an end node to the graph!
  @Test
  public void testOneWitness() {
    IWitness a = engine.createWitness("A", "only one witness");
    IAlignmentGraph graph = AlignmentGraph.create(a);
    final List<IAlignmentNode> nodes = graph.getNodes();
    Assert.assertEquals(4, nodes.size());
    final IAlignmentNode startNode = nodes.get(0);
    final IAlignmentNode firstNode = nodes.get(1);
    final IAlignmentNode secondNode = nodes.get(2);
    final IAlignmentNode thirdNode = nodes.get(3);
    Assert.assertEquals("#", startNode.getNormalized());
    Assert.assertEquals("only", firstNode.getNormalized());
    Assert.assertEquals("one", secondNode.getNormalized());
    Assert.assertEquals("witness", thirdNode.getNormalized());
    List<IAlignmentArc> arcs = graph.getArcs();
    Assert.assertEquals(3, arcs.size());
    Assert.assertEquals(a, arcs.get(0).getWitnesses().get(0));
    Assert.assertEquals(a, arcs.get(1).getWitnesses().get(0));
    Assert.assertEquals(a, arcs.get(2).getWitnesses().get(0));
    Assert.assertEquals(startNode, arcs.get(0).getBeginNode());
    Assert.assertEquals(firstNode, arcs.get(0).getEndNode());
    Assert.assertEquals(firstNode, arcs.get(1).getBeginNode());
    Assert.assertEquals(secondNode, arcs.get(1).getEndNode());
    Assert.assertEquals(secondNode, arcs.get(2).getBeginNode());
    Assert.assertEquals(thirdNode, arcs.get(2).getEndNode());
  }
  
  @Test
  public void testTwoWitnesses() {
    final IWitness w1 = engine.createWitness("A", "the black cat");
    final IWitness w2 = engine.createWitness("B", "the black cat");
    AlignmentGraph graph = AlignmentGraph.create(w1);
    graph.addWitness(w2);
    final List<IAlignmentNode> nodes = graph.getNodes();
    Assert.assertEquals(4, nodes.size());
    List<IAlignmentArc> arcs = graph.getArcs();
    Assert.assertEquals(3, arcs.size());
    Assert.assertEquals("# -> the: A, B", arcs.get(0).toString());
    Assert.assertEquals("the -> black: A, B", arcs.get(1).toString());
    Assert.assertEquals("black -> cat: A, B", arcs.get(2).toString());
     
    }


}
