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
  
  //TODO: we should add a start and an end node to the graph!
  @Test
  public void testOneWitness() {
    IWitness a = collatex.createWitness("A", "only one witness");
    IAlignmentGraph graph = AlignmentGraph.create(a);
    final List<IAlignmentNode> nodes = graph.getNodes();
    Assert.assertEquals(3, nodes.size());
    Assert.assertEquals("only", nodes.get(0).getNormalized());
    Assert.assertEquals("one", nodes.get(1).getNormalized());
    Assert.assertEquals("witness", nodes.get(2).getNormalized());
  }
}
