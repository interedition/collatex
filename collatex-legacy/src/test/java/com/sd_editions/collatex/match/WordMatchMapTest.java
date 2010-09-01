/**
 * CollateX - a Java library for collating textual sources,
 * for example, to produce an apparatus.
 *
 * Copyright (C) 2010 ESF COST Action "Interedition".
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.sd_editions.collatex.match;

import java.util.List;
import java.util.Set;

import org.junit.Ignore;

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
    assertEquals(18, words.size()); // 18 unique words
    assertTrue(words.contains("spain"));
  }

  //TODO: escape UTF-8 characters?
  @Ignore
  public void testNormalizeWord() throws Exception {
    assertTrue(testWordMatchMap.getWords().contains("ööh"));
  }

  public final void testLevMatches() {
    Set<WordCoordinate> levMatches = testWordMatchMap.getLevMatches("rain");
    assertEquals(1, levMatches.size());
    assertEquals(new WordCoordinate(2, 2), levMatches.toArray()[0]); // "rains"
  }

  public final void testExactMatches() {
    Set<WordCoordinate> exactMatches = testWordMatchMap.getExactMatches("spain");
    assertEquals(3, exactMatches.size());
  }

  public final void testExactMatchesForWitness() {
    final int[] exactMatchesForWitness0 = testWordMatchMap.getExactMatchesForWitness("the", 0);
    assertEquals(0, exactMatchesForWitness0[0]);
    assertEquals(7, exactMatchesForWitness0[1]);
  }

  public final void testGetColorMatrixPermutations0() {
    Set<ColorMatrix> colorMatrixPermutations = testWordMatchMap.getColorMatrixPermutations();
    //    for (String word : testWordMatchMap.getWords()) {
    //      Util.p(word);
    //      SortedArraySet<WordCoordinate> allmatches = testWordMatchMap.getExactMatches(word);
    //      allmatches.addAll(testWordMatchMap.getLevMatches(word));
    //      Util.p(allmatches);
    //    }
    assertEquals(33, colorMatrixPermutations.size());
    ColorMatrix firstMatrix = colorMatrixPermutations.iterator().next();
    assertEquals(3, firstMatrix.getHeight());
    assertEquals(11, firstMatrix.getWidth());
    assertEquals(1, firstMatrix.getCell(0, 0));
    printMatrices(colorMatrixPermutations);
  }

  //  public final void testGetColorMatrixPermutations1() {
  //    Set<ColorMatrix> colorMatrixPermutations = makePermutations(new String[] { "A black cat.", "A black dog", "One white dog" });
  //    assertEquals(1, colorMatrixPermutations.size());
  //    ColorMatrix cm1 = new ColorMatrix(new int[][] { { 1, 2, 3 }, { 1, 2, 4 }, { 5, 6, 4 } });
  //    assertEquals(cm1, colorMatrixPermutations.iterator().next());
  //  }
  //
  //  public final void testGetColorMatrixPermutations2() {
  //    Set<ColorMatrix> colorMatrixPermutations = makePermutations(new String[] { "A black cat.", "A black block" });
  //    printMatrices(colorMatrixPermutations);
  //    assertEquals(2, colorMatrixPermutations.size());
  //    ColorMatrix cm1 = new ColorMatrix(new int[][] { { 1, 2, 3 }, { 1, 2, 4 } });
  //    ColorMatrix cm2 = new ColorMatrix(new int[][] { { 1, 2, 3 }, { 1, 4, 2 } });
  //    assertEquals(cm1, colorMatrixPermutations.iterator().next());
  //    assertEquals(cm2, colorMatrixPermutations.iterator().next());
  //    assertTrue(colorMatrixPermutations.contains(cm1));
  //    assertTrue(colorMatrixPermutations.contains(cm2));
  //  }

  private void printMatrices(Set<ColorMatrix> colorMatrixPermutations) {
    int i = 1;
    for (ColorMatrix colormatrix : colorMatrixPermutations) {
      Util.p(i++ + "\n" + colormatrix);
    }
  }

}
