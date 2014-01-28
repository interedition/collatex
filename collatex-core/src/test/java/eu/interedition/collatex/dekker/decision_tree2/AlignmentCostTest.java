package eu.interedition.collatex.dekker.decision_tree2;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class AlignmentCostTest {
  @Test
  public void testCompare() {
    AlignmentCost c1 = new AlignmentCost(1, 5);
    AlignmentCost c2 = new AlignmentCost(1, 3);
    AlignmentCost c3 = new AlignmentCost(2, 3);
    // c1 has more gaps than c2... compare should return a positive
    assertTrue(c1.compareTo(c2) > 0);
    // c3 has more aligned vectors than c2 compare should return negative
    assertTrue(c2.compareTo(c3) < 0); 
  }
  
  @Test
  public void testPlus() {
    AlignmentCost c1 = new AlignmentCost(1, 2);
    AlignmentCost add = new AlignmentCost(1, 3);
    AlignmentCost expected = new AlignmentCost(2, 5);
    AlignmentCost result = c1.plus(add);
    assertEquals(expected, result);
  }

}
