package eu.interedition.collatex2.experimental.graph;

import java.util.List;

import junit.framework.Assert;

import org.junit.BeforeClass;
import org.junit.Test;

import eu.interedition.collatex2.implementation.CollateXEngine;
import eu.interedition.collatex2.interfaces.IWitness;

public class GraphTest {
  private static CollateXEngine collatex;
  
  @BeforeClass
  public static void setup() {
    collatex = new CollateXEngine();
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
    IWitness a = collatex.createWitness("A", "only one witness");
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
    Assert.assertEquals(a, arcs.get(0).getWitness());
    Assert.assertEquals(a, arcs.get(1).getWitness());
    Assert.assertEquals(a, arcs.get(2).getWitness());
    Assert.assertEquals(startNode, arcs.get(0).getBeginNode());
    Assert.assertEquals(firstNode, arcs.get(0).getEndNode());
    Assert.assertEquals(firstNode, arcs.get(1).getBeginNode());
    Assert.assertEquals(secondNode, arcs.get(1).getEndNode());
    Assert.assertEquals(secondNode, arcs.get(2).getBeginNode());
    Assert.assertEquals(thirdNode, arcs.get(2).getEndNode());
  }
}
