package eu.interedition.collatex.input;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.Test;

import eu.interedition.collatex.input.Witness;
import eu.interedition.collatex.input.Word;

public class WitnessTest {

  @Test
  public void testNullWitness() {

    try {
      new Witness(null);
      fail();
    } catch (IllegalArgumentException e) {
      assertEquals(e.getMessage(), "List of words cannot be null.");
    }
  }

  @Test
  public void testEmptyWitness() {
    Witness witness = new Witness(new Word[0]);
    assertEquals(witness.sentence, "");
    assertEquals(witness.getWords().size(), 0);
  }

}
