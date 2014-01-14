package eu.interedition.collatex.dekker.decision_tree2;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;

import eu.interedition.collatex.simple.SimpleWitness;

public class DecisionTreeTest {
  // No repeated tokens means that there are no alternatives.
  // Islands are sorted on size.
  @Test
  public void testNoRepetition() {
    SimpleWitness a = new SimpleWitness("a", "a b c d");
    SimpleWitness b = new SimpleWitness("b", "a b e d");
    DecisionTreeNode dc = DecisionTreeNode.createDecisionTree(a, b);
    List<DecisionTreeNode> al = dc.calculateAlternatives();
    // select first vector graph
    DecisionTreeNode a1 = al.get(0);
    assertEquals(1, a1.getNumberOfSelectedVectors());
    assertEquals(2, a1.getNumberOfAlignedTokens());
    assertEquals(1, a1.getNumberOfGapTokens());
    // select first vector witness
    DecisionTreeNode a2 = al.get(1);
    assertEquals(1, a2.getNumberOfSelectedVectors());
    assertEquals(2, a2.getNumberOfAlignedTokens());
    assertEquals(1, a2.getNumberOfGapTokens());
    // skip first vector graph
    DecisionTreeNode a3 = al.get(2);
    assertEquals(0, a3.getNumberOfSelectedVectors());
    assertEquals(0, a3.getNumberOfAlignedTokens());
    assertEquals(3, a3.getNumberOfGapTokens());
    // skip first vector witness
    DecisionTreeNode a4 = al.get(3);
    assertEquals(0, a4.getNumberOfSelectedVectors());
    assertEquals(0, a4.getNumberOfAlignedTokens());
    assertEquals(3, a4.getNumberOfGapTokens());
  }
}
