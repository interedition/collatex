package eu.interedition.collatex.input;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.Test;

public class WitnessTest {

  @Test
  public void testNullWitness() {

    try {
      new Segment(null);
      fail();
    } catch (IllegalArgumentException e) {
      assertEquals(e.getMessage(), "List of words cannot be null.");
    }
  }

  @Test
  public void testEmptyWitness() {
    Segment witness = new Segment(new Word[0]);
    assertEquals(witness.getWords().size(), 0);
  }

}
