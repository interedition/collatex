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

package com.sd_editions.collatex.permutations;

import junit.framework.TestCase;

public class ColorsTest extends TestCase {
  public void testFirstUseCasePeter() {
  //    String[] witnesses = new String[] { "The black cat", "The black and white cat", "The black and green cat" };
  //    Colors colors = new Colors(witnesses);
  //    assertEquals(6, colors.numberOfUniqueWords());
  //    assertEquals(Sets.newHashSet(1, 2, 3), colors.getWitnessIndex(1).getWordCodes());
  //    assertEquals(Sets.newHashSet(1, 2, 4, 5, 3), colors.getWitnessIndex(2).getWordCodes());
  //    assertEquals(Sets.newHashSet(1, 2, 4, 6, 3), colors.getWitnessIndex(3).getWordCodes());
  //    Comparison c1 = colors.compareWitness(1, 2);
  //    assertEquals(Lists.newArrayList("and", "white"), c1.getAddedWords());
  //    Comparison c2 = colors.compareWitness(1, 3);
  //    assertEquals(Lists.newArrayList("and", "green"), c2.getAddedWords());
  //    Comparison c3 = colors.compareWitness(2, 3);
  //    assertEquals(Lists.newArrayList("white/green"), c3.getReplacedWords());
  }

  public void testUseCase2() {
  //    String[] witnesses = new String[] { "the black cat", "THE BLACK CAT", "The black cat" };
  //    Colors colors = new Colors(witnesses);
  //    assertEquals(3, colors.numberOfUniqueWords());
  //    assertEquals(Sets.newHashSet(1, 2, 3), colors.getWitnessIndex(1).getWordCodes());
  //    assertEquals(Sets.newHashSet(1, 2, 3), colors.getWitnessIndex(2).getWordCodes());
  //    assertEquals(Sets.newHashSet(1, 2, 3), colors.getWitnessIndex(3).getWordCodes());
  }

  //  public void testThirdUseCasePeter() {
  //    String[] witnesses = new String[] { "He was agast so", "He was agast", "So he was agast" };
  //    Colors colors = new Colors(witnesses);
  //    Comparison c1 = colors.compareWitness(1, 2);
  //    assertEquals(Lists.newArrayList("so"), c1.getRemovedWords());
  //    Comparison c2 = colors.compareWitness(1, 3);
  //    assertTrue(c2.getRemovedWords().toString(), c2.getRemovedWords().isEmpty());
  //    assertTrue(c2.getAddedWords().toString(), c2.getAddedWords().isEmpty());
  //    List<Transposition> transpositions = c2.getTranspositions();
  //    Transposition transposition = transpositions.get(0);
  //    assertEquals(4, transposition.getTransposedWord());
  //    assertEquals(3, transposition.getTranspositionDistance());
  //  }

  public void testUseCase4() {
  //    String[] witnesses = new String[] { "the green bike in the green", "the red bike in the street" };
  //    Colors colors = new Colors(witnesses);
  //    assertEquals(6, colors.numberOfUniqueWords());
  //    assertEquals(Sets.newHashSet(1, 2, 3, 4, 5, 6), colors.getWitnessIndex(1).getWordCodes());
  //    assertEquals(Sets.newHashSet(1, 7, 3, 4, 5, 8), colors.getWitnessIndex(2).getWordCodes());
  //    assertEquals(Sets.newHashSet(1, 2, 3, 4, 7, 8), colors.getWitnessIndex(1).getWordCodes());
  //    assertEquals(Sets.newHashSet(1, 5, 3, 4, 7, 6), colors.getWitnessIndex(2).getWordCodes());
  }

}
