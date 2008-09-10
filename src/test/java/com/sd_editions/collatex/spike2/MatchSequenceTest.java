package com.sd_editions.collatex.spike2;

import java.util.List;

import junit.framework.TestCase;

public class MatchSequenceTest extends TestCase {
  //Note: this test should NOT lead to a transposition
  public void testNotATranspositionBecauseGroupsAreNotContinuous() {
    String base = "The black dog chases a red cat.";
    String witness = "A red cat chases the yellow dog";
    Colors colors = new Colors(base, witness);
    List<MatchSequence> sequences = colors.getMatchSequences(1, 2);
    assertEquals("[(1->5)]", sequences.get(0).toString());
    assertEquals("[(3->7)]", sequences.get(1).toString());
    assertEquals("[(4->4)]", sequences.get(2).toString());
    assertEquals("[(5->1), (6->2), (7->3)]", sequences.get(3).toString());
    assertEquals(4, sequences.size());
  }
}
