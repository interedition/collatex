package eu.interedition.collatex.dekker.decision_tree;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.neo4j.helpers.collection.Iterables;

import eu.interedition.collatex.dekker.matrix.Island;
import eu.interedition.collatex.simple.SimpleWitness;

// @author: Ronald Haentjens Dekker
public class DecisionTreeTest {

  // coordinates start at 0
  private void assertIsland(Island i, int column, int row, int size) {
    if (i==null) {
      throw new RuntimeException("Island is null!");
    }
    assertEquals(column-1, i.getLeftEnd().getColumn());
    assertEquals(row-1, i.getLeftEnd().getRow());
    assertEquals(size, i.size());
  }

  @Test
  public void testEmptyDecisionTree() {
    DecisionTree d = new DecisionTree();
    DecisionNode s = d.getStart();
    assertEquals(0, s.linkedTokens);
    assertTrue("startNode", d.containsVertex(s));
    assertEquals(1, d.getVertexCount());
    assertEquals(0, d.getEdgeCount());
  }
  
  // No repeated tokens means that there are no alternatives.
  // Islands are sorted on size.
  @Test
  public void testNoRepetition() {
    SimpleWitness a = new SimpleWitness("a", "a b c d");
    SimpleWitness b = new SimpleWitness("b", "a b e d");
    DecisionTree dt = new DecisionTree(a, b);
    // start node
    DecisionNode s = dt.getStart();
    assertEquals(1, dt.getOutEdges(s).size());
    AlternativeEdge ab = Iterables.first(dt.getOutEdges(s));
    Island i1 = ab.getIsland();
    assertIsland(i1, 1, 1, 2);
    // decisions level 1 (no alternatives here)
    DecisionNode n1 = dt.getDest(ab);
    assertEquals(1, dt.getOutEdges(n1).size());
    AlternativeEdge d = Iterables.first(dt.getOutEdges(n1));
    Island i2 = d.getIsland();
    assertIsland(i2, 4, 4, 1);
    // general tree
    assertEquals(3, dt.getVertexCount());
    assertEquals(2, dt.getEdgeCount());
  }
}
