package com.sd_editions.collatex.permutations;

import java.util.HashMap;
import java.util.List;

import junit.framework.TestCase;

import org.junit.Test;

public class CollateCoreTest extends TestCase {

  private WitnessBuilder builder;

  @Override
  protected void setUp() throws Exception {
    builder = new WitnessBuilder();
    super.setUp();
  }

  @Test
  public void testDetermineBase() {
    CollateCore collateCore = new CollateCore(builder.buildWitnesses("The Black Cat", "The Cat and the Dog", "The White Cat"));
    String witnessId1 = collateCore.getWitness(1).id;
    String witnessId2 = collateCore.getWitness(2).id;
    String witnessId3 = collateCore.getWitness(3).id;
    HashMap<String, MultiMatch> generatedBase = collateCore.generateBase();
    assertEquals(2, generatedBase.keySet().size());
    assertTrue(generatedBase.containsKey("the"));
    assertTrue(generatedBase.containsKey("cat"));

    MultiMatch theMultiMatch = generatedBase.get("the");
    // 'the' occurs once in the 1st witness
    assertEquals(1, theMultiMatch.getOccurancesInWitness(witnessId1).size());
    // 'the' occurs twice in the 2nd witness
    assertEquals(2, theMultiMatch.getOccurancesInWitness(witnessId2).size());
    // 'the' occurs once in the 3rd witness
    assertEquals(1, theMultiMatch.getOccurancesInWitness(witnessId3).size());
  }

  @Test
  public void testMultiMatchShrinksAtThirdWitness() {
    CollateCore collateCore = new CollateCore(builder.buildWitnesses("The Black Cat", "The black dog and white cat", "The White Cat"));
    String witnessId1 = collateCore.getWitness(1).id;
    String witnessId2 = collateCore.getWitness(2).id;
    String witnessId3 = collateCore.getWitness(3).id;
    HashMap<String, MultiMatch> generatedBase = collateCore.generateBase();
    assertEquals(2, generatedBase.keySet().size());
    assertTrue(generatedBase.containsKey("the"));
    assertTrue(generatedBase.containsKey("cat"));

    MultiMatch theMultiMatch = generatedBase.get("the");
    assertEquals(1, theMultiMatch.getOccurancesInWitness(witnessId1).size());
    assertEquals(1, theMultiMatch.getOccurancesInWitness(witnessId2).size());
    assertEquals(1, theMultiMatch.getOccurancesInWitness(witnessId3).size());
  }

  @Test
  public void testSortByVariation() {
    CollateCore collateCore = new CollateCore(builder.buildWitnesses("I bought this glass, because it matches those dinner plates.", "I bought those glasses."));
    List<MatchUnmatch> matchUnmatchList = collateCore.doCompareWitnesses(collateCore.getWitness(1), collateCore.getWitness(2));
    collateCore.sortPermutationsByVariation(matchUnmatchList);
    assertEquals("[(1->1), (2->2), (3->3), (4->4)]", matchUnmatchList.get(0).getPermutation().toString());
  }
}
