package com.sd_editions.collatex.permutations;

import junit.framework.TestCase;

import org.junit.Test;

public class CollateCoreTest extends TestCase {
  @Test
  public void testDetermineBase() {
    CollateCore collateCore = new CollateCore("The Black Cat", "The Cat and the Dog", "The White Cat");
    String witnessId1 = collateCore.getWitness(1).id;
    String witnessId2 = collateCore.getWitness(2).id;
    String witnessId3 = collateCore.getWitness(3).id;
    MultiMatchMap mmm = collateCore.getMultiMatchMap();
    assertEquals(2, mmm.keySet().size());
    assertTrue(mmm.containsKey("the"));
    assertTrue(mmm.containsKey("cat"));

    MultiMatch theMultiMatch = mmm.get("the");
    // 'the' occurs once in the 1st witness
    assertEquals(1, theMultiMatch.getOccurancesInWitness(witnessId1).size());
    // 'the' occurs twice in the 2nd witness
    assertEquals(2, theMultiMatch.getOccurancesInWitness(witnessId2).size());
    // 'the' occurs once in the 3rd witness
    assertEquals(1, theMultiMatch.getOccurancesInWitness(witnessId3).size());
    assertEquals("the cat", mmm.getNormalizedMatchSentence());
  }

  @Test
  public void testMultiMatchShrinksAtThirdWitness() {
    CollateCore collateCore = new CollateCore("The Black Cat", "The black dog and white cat", "The White Cat");
    String witnessId1 = collateCore.getWitness(1).id;
    String witnessId2 = collateCore.getWitness(2).id;
    String witnessId3 = collateCore.getWitness(3).id;
    MultiMatchMap mmm = collateCore.getMultiMatchMap();
    assertEquals(2, mmm.keySet().size());
    assertTrue(mmm.containsKey("the"));
    assertTrue(mmm.containsKey("cat"));

    MultiMatch theMultiMatch = mmm.get("the");
    assertEquals(1, theMultiMatch.getOccurancesInWitness(witnessId1).size());
    assertEquals(1, theMultiMatch.getOccurancesInWitness(witnessId2).size());
    assertEquals(1, theMultiMatch.getOccurancesInWitness(witnessId3).size());
  }

}
