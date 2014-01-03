package eu.interedition.collatex.dekker.decision_tree;

import static eu.interedition.collatex.dekker.decision_tree.DecisionTreeCreator.createDecisionTree;
import static eu.interedition.collatex.dekker.decision_tree.DecisionTreeTraversal.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Collection;

import junit.framework.Assert;

import org.junit.Test;
import org.neo4j.helpers.collection.Iterables;

import com.google.common.base.Function;

import eu.interedition.collatex.dekker.matrix.Island;
import eu.interedition.collatex.simple.SimpleWitness;

// @author: Ronald Haentjens Dekker
public class DecisionTreeTest {

  private void assertContains(Collection<AlternativeEdge> edges, AlternativeEdge e1) {
    boolean found = false;
    for (AlternativeEdge e : edges) {
      if (e.getSource().equals(e1.getSource()) && e.getCost().equals(e1.getCost())) {
        found = true;
        break;
      }
    }
    if (!found) {
      Assert.fail("Edge "+e1+" not found, found: "+edges);
    }
  }

  // coordinates start at 0
  private void assertIsland(Island i, int column, int row, int size) {
    if (i==null) {
      throw new RuntimeException("Island is null!");
    }
    assertEquals(column-1, i.getLeftEnd().getColumn());
    assertEquals(row-1, i.getLeftEnd().getRow());
    assertEquals(size, i.size());
  }

  // for debug reasons we show the edges here
  private void debugTree(DecisionTree dt) {
    //System.out.println(dt.toString());
    //System.out.println(dt.getVertices());
    //System.out.println(dt.getEdges());
    DecisionNode start = dt.getStart();
    System.out.println("Start: "+start);
    Function<AlternativeEdge, Void> f = new Function<AlternativeEdge, Void>() {
      @Override
      public Void apply(AlternativeEdge edge) {
        System.out.println(edge);
        return null;
      }
    };
    traverseTree(dt, f);
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
    DecisionTree dt = createDecisionTree(a, b);
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
  
  // Phrase a a is repeated
  // Decision tree should contain alternatives
  // Phrase a a is longer than b so should be first decision in tree
  @Test
  public void testSingleRepeatedPhrase() {
    SimpleWitness a = new SimpleWitness("a", "a a x b a a");
    SimpleWitness b = new SimpleWitness("b", "a a y b");
    DecisionTree dt = createDecisionTree(a, b);
    // start node
    DecisionNode s = dt.getStart();
    assertEquals(2, dt.getOutEdges(s).size());
    // I want to test the edges
    // Edges have a cost associated to them.
    // Also edges have a parent node
    // Parent nodes have a rank
    AlternativeEdge e1 = new AlternativeEdge(new DecisionNode(0), new Cost(0.0));
    AlternativeEdge e2 = new AlternativeEdge(new DecisionNode(0), new Cost(2.2));
    AlternativeEdge e3 = new AlternativeEdge(new DecisionNode(1), new Cost(0.0));
    assertContains(dt.getEdges(), e1);
    //TODO: problem is cost is a double with lots of numbers
    //TODO: maybe hamcrest helps here
    // assertContains(dt.getEdges(), e2);
    assertContains(dt.getEdges(), e3);
    assertEquals(3, dt.getEdges().size());
  }

}
