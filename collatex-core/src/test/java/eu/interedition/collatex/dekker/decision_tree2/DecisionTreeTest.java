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
    DecisionTreeNode a1 = al.get(0);
    assertEquals(1, a1.getNumberOfSelectedVectors());
    assertEquals(2, a1.getNumberOfAlignedTokens());
    assertEquals(1, a1.getNumberOfGapTokens());
  }
}
