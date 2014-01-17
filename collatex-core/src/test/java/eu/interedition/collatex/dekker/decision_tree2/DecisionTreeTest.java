package eu.interedition.collatex.dekker.decision_tree2;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.List;

import org.junit.Test;

import eu.interedition.collatex.AbstractTest;
import eu.interedition.collatex.simple.SimpleWitness;

/*
 * Test the alternatives in the decision tree
 * and the cost function.
 * 
 * @author: Ronald Haentjens Dekker
 */
public class DecisionTreeTest extends AbstractTest {
  
  // No repeated tokens means that there are no alternatives.
  // Islands are sorted on size.
  @Test
  public void testNoRepetition1() {
    SimpleWitness a = new SimpleWitness("a", "a b c d");
    SimpleWitness b = new SimpleWitness("b", "a b e d");

    collationAlgorithm = new DekkerDecisionTreeAlgorithm();
    collate(a, b);
    
    DekkerDecisionTreeAlgorithm dtalgo = (DekkerDecisionTreeAlgorithm) collationAlgorithm;
    DecisionTreeNode root = dtalgo.getRoot();
    
    // assert first level of children
    List<DecisionTreeNode> al = root.getChildNodes();
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
  
  // assert second level of children (16 alternatives)
  @Test
  public void testNoRepetition2() {
    SimpleWitness a = new SimpleWitness("a", "a b c d");
    SimpleWitness b = new SimpleWitness("b", "a b e d");

    collationAlgorithm = new DekkerDecisionTreeAlgorithm();
    collate(a, b);
    
    DekkerDecisionTreeAlgorithm dtalgo = (DekkerDecisionTreeAlgorithm) collationAlgorithm;
    DecisionTreeNode root = dtalgo.getRoot();
    
    // assert second level of children
    List<DecisionTreeNode> al = root.getChildNodes();
    List<DecisionTreeNode> al2 = al.get(0).getChildNodes();
    // select first vector graph
    DecisionTreeNode a21 = al2.get(0);
    assertEquals(2, a21.getNumberOfSelectedVectors());
    assertEquals(3, a21.getNumberOfAlignedTokens());
    assertEquals(1, a21.getNumberOfGapTokens());
    // skip first vector graph
    DecisionTreeNode a23 = al2.get(2);
    assertEquals(1, a23.getNumberOfSelectedVectors());
    assertEquals(2, a23.getNumberOfAlignedTokens());
    assertEquals(2, a23.getNumberOfGapTokens());
  }

 // Token a is repeated
 // Decision tree should contain alternatives
 @Test
 public void testSingleRepeatedPhrase() {
   SimpleWitness a = new SimpleWitness("a", "a x b a");
   SimpleWitness b = new SimpleWitness("b", "a y b");

   collationAlgorithm = new DekkerDecisionTreeAlgorithm();
   collate(a, b);
   
   DekkerDecisionTreeAlgorithm dtalgo = (DekkerDecisionTreeAlgorithm) collationAlgorithm;
   DecisionTreeNode root = dtalgo.getRoot();
   //TODO: add more asserts!
   //fail();
 }
}
