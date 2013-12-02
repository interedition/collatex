package eu.interedition.collatex.dekker.decision_tree;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import eu.interedition.collatex.simple.SimpleWitness;

// @author: Ronald Haentjens Dekker
public class DecisionTreeTest {

  @Test
  public void testEmptyDecisionTree() {
    DecisionTree d = new DecisionTree();
    DecisionNode s = d.getStart();
    assertEquals(0, s.linkedTokens);
    assertTrue("startNode", d.containsVertex(s));
    assertEquals(1, d.getVertexCount());
    assertEquals(0, d.getEdgeCount());
  }
  
  @Test
  public void testDecisionTree1() {
    SimpleWitness a = new SimpleWitness("a", "a b c d");
    SimpleWitness b = new SimpleWitness("b", "a b e d");
    DecisionTree d = new DecisionTree(a, b);
    DecisionNode s = d.getStart();
    assertEquals(1, d.getOutEdges(s).size());
  }
}
