package com.sd_editions.collatex.permutations;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.Test;

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
