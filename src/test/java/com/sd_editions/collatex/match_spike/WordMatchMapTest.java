package com.sd_editions.collatex.match_spike;

import java.util.List;

import junit.framework.TestCase;

import com.google.common.collect.Lists;
import com.sd_editions.collatex.Block.BlockStructure;
import com.sd_editions.collatex.Block.Util;

public class WordMatchMapTest extends TestCase {

  private WordMatchMap testWordMatchMap;

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    List<BlockStructure> witnessList = Lists.newArrayList();
    witnessList.add(Util.string2BlockStructure("The rain in Spain falls mainly on the plain."));
    witnessList.add(Util.string2BlockStructure("Da rain in Spain usually falls on the plains."));
    witnessList.add(Util.string2BlockStructure("When it rains in Spain, get, ööh, away from the plains."));
    testWordMatchMap = new WordMatchMap(witnessList);
  }

  public final void testWords() {
    List<String> words = testWordMatchMap.getWords();
    assertNotNull(words);
    //    System.out.println(words);
    assertEquals(18, words.size()); // 18 unique words
    assertTrue(words.contains("spain"));
  }

  public void testNormalizeWord() throws Exception {
    assertTrue(testWordMatchMap.getWords().contains("ööh"));
  }

  public final void testLevMatches() {
    List<WordCoordinate> levMatches = testWordMatchMap.getLevMatches("rain");
    assertEquals(1, levMatches.size());
    assertEquals(new WordCoordinate(2, 2), levMatches.get(0)); // "rains"
  }

  public final void testExactMatches() {
    List<WordCoordinate> exactMatches = testWordMatchMap.getExactMatches("spain");
    assertEquals(3, exactMatches.size());
  }

  public final void testExactMatchesForWitness() {
    final int[] exactMatchesForWitness0 = testWordMatchMap.getExactMatchesForWitness("the", 0);
    assertEquals(0, exactMatchesForWitness0[0]);
    assertEquals(7, exactMatchesForWitness0[1]);
  }

}
