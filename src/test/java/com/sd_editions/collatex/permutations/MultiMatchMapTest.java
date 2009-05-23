package com.sd_editions.collatex.permutations;

import junit.framework.TestCase;

import org.junit.Test;

import eu.interedition.collatex.input.Witness;
import eu.interedition.collatex.input.WitnessBuilder;

public class MultiMatchMapTest extends TestCase {

  private WitnessBuilder builder;

  @Override
  protected void setUp() throws Exception {
    builder = new WitnessBuilder();
    super.setUp();
  }

  @Test
  public void testDetermineBase() {
    Witness witness1 = builder.build("The Black Cat");
    Witness witness2 = builder.build("The Cat and the Dog");
    Witness witness3 = builder.build("The White Cat");
    MultiMatchMap mmm = new MultiMatchMap(witness1, witness2, witness3);
    assertEquals(2, mmm.keySet().size());
    assertTrue(mmm.containsKey("the"));
    assertTrue(mmm.containsKey("cat"));

    MultiMatch theMultiMatch = mmm.get("the");
    // 'the' occurs once in the 1st witness
    assertEquals(1, theMultiMatch.getOccurancesInWitness(witness1.id).size());
    // 'the' occurs twice in the 2nd witness
    assertEquals(2, theMultiMatch.getOccurancesInWitness(witness2.id).size());
    // 'the' occurs once in the 3rd witness
    assertEquals(1, theMultiMatch.getOccurancesInWitness(witness3.id).size());
    assertEquals("the cat", mmm.getNormalizedMatchSentence());
  }

  @Test
  public void testMultiMatchShrinksAtThirdWitness() {
    Witness witness1 = builder.build("The Black Cat");
    Witness witness2 = builder.build("The black dog and white cat");
    Witness witness3 = builder.build("The White Cat");
    MultiMatchMap mmm = new MultiMatchMap(witness1, witness2, witness3);
    assertEquals(2, mmm.keySet().size());
    assertTrue(mmm.containsKey("the"));
    assertTrue(mmm.containsKey("cat"));

    MultiMatch theMultiMatch = mmm.get("the");
    assertEquals(1, theMultiMatch.getOccurancesInWitness(witness1.id).size());
    assertEquals(1, theMultiMatch.getOccurancesInWitness(witness2.id).size());
    assertEquals(1, theMultiMatch.getOccurancesInWitness(witness3.id).size());
  }

}
