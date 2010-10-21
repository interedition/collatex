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

package eu.interedition.collatex2.matching;

import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.BeforeClass;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import eu.interedition.collatex2.implementation.CollateXEngine;
import eu.interedition.collatex2.implementation.vg_analysis.ISequence;

public class IndexMatcherTest {
  private static CollateXEngine factory;

  @BeforeClass
  public static void setup() {
    factory = new CollateXEngine();
  }

//  @Test
//  public void testEverythingIsUnique() {
//    final IWitness witnessA = factory.createWitness("A", "everything is unique should be no problem");
//    final IWitness witnessB = factory.createWitness("B", "everything is unique");
//    final IAlignment alignment = PairwiseAlignmentHelper.align(factory, witnessA, witnessB);
//    final List<IMatch> matches = alignment.getMatches();
//    assertEquals(1, matches.size());
//    final IMatch match = matches.get(0);
//    assertEquals("everything is unique", match.getNormalized());
//    final IColumns columnsA = match.getColumns();
//    assertEquals(1, columnsA.getBeginPosition());
//    assertEquals(3, columnsA.getEndPosition());
//  }
//
//  @Test
//  public void testEverythingIsUniqueTwoWitnesses() {
//    final IWitness witnessA = factory.createWitness("A", "everything is unique should be no problem");
//    final IWitness witnessB = factory.createWitness("B", "this one very different");
//    final IWitness witnessC = factory.createWitness("C", "everything is different");
//    final IAlignmentTable table = factory.align(witnessA, witnessB);
//    final List<ITokenMatch> matches = factory.alignOldStyle(table, witnessC).getTokenMatches();
//    assertEquals(3, matches.size());
//    final ITokenMatch match = matches.get(0);
//    assertEquals("everything", match.getNormalized());
//    INormalizedToken baseToken = match.getBaseToken();
//    assertEquals(1, baseToken.getPosition());
//    final ITokenMatch match2 = matches.get(1);
//    assertEquals("is", match2.getNormalized());
//    INormalizedToken baseTokenB = match2.getBaseToken();
//    assertEquals(2, baseTokenB.getPosition());
//    final ITokenMatch match3 = matches.get(2);
//    assertEquals("different", match3.getNormalized());
//    INormalizedToken baseTokenC = match3.getBaseToken();
//    assertEquals(4, baseTokenC.getPosition());
//  }
//
//  @Test
//  public void testOverlappingMatches() {
//    final IWitness witnessA = factory.createWitness("A", "everything is unique should be no problem");
//    final IWitness witnessB = factory.createWitness("B", "this one is different");
//    final IWitness witnessC = factory.createWitness("C", "everything is different");
//    final IAlignmentTable table = factory.align(witnessA, witnessB);
//    final List<ITokenMatch> matches = factory.alignOldStyle(table, witnessC).getTokenMatches();
//    assertEquals(3, matches.size());
//    final ITokenMatch match = matches.get(0);
//    assertEquals("everything", match.getNormalized());
//    INormalizedToken baseTokenA = match.getBaseToken();
//    assertEquals(1, baseTokenA.getPosition());
//    final ITokenMatch match2 = matches.get(1);
//    assertEquals("is", match2.getNormalized());
//    INormalizedToken baseTokenB = match2.getBaseToken();
//    assertEquals(3, baseTokenB.getPosition());
//    final ITokenMatch match3 = matches.get(2);
//    assertEquals("different", match3.getNormalized());
//    INormalizedToken baseTokenC = match3.getBaseToken();
//    assertEquals(4, baseTokenC.getPosition());
//  }
//
//  //TODO: to make this work we need ngrams of 4 tokens!
//  @Ignore
//  @Test
//  public void testGetMatchesUsingWitnessIndex() {
//    final IWitness witnessA = factory.createWitness("A", "The big black cat and the big black rat");
//    final IWitness witnessB = factory.createWitness("B", "The big black");
//    final IAlignmentTable table = factory.align(witnessA);
//    final IAnalysis alignment = factory.analyseOldStyle(table, witnessB);
//    final List<ISequence> matches = alignment.getSequences();
//    assertEquals(1, matches.size());
//    final ISequence match = matches.get(0);
//    assertEquals("the big black", match.getNormalized());
//    final IPhrase columnsA = match.getPhraseA();
//    assertEquals(1, columnsA.getBeginPosition());
//    assertEquals(3, columnsA.getEndPosition());
//  }
//
//  //Note: internally this gives # the big black and the big black cat as matches
//  @Test
//  public void testGetMatchesUsingWitnessIndexWithOverlapping() {
//    final IWitness witnessA = factory.createWitness("A", "the big black cat and the big black rat");
//    final IWitness witnessB = factory.createWitness("B", "the big black cat");
//    final IAlignmentTable table = factory.align(witnessA);
//    final IAnalysis alignment = factory.analyseOldStyle(table, witnessB);
//    final List<ISequence> matches = alignment.getSequences();
//    assertEquals(1, matches.size());
//    final ISequence match = matches.get(0);
//    assertEquals("the big black cat", match.getNormalized());
//    final IPhrase columnsA = match.getPhraseA();
//    assertEquals(1, columnsA.getBeginPosition());
//    assertEquals(4, columnsA.getEndPosition());
//  }
//
//  //TODO: make convenience method for creation of AlignmentTable on Factory!
//
//  @Test
//  public void testOverlappingMatches2() {
//    final IWitness witnessA = factory.createWitness("A", "the black cat and the black mat");
//    final IWitness witnessB = factory.createWitness("B", "the black dog and the black mat");
//    final IAlignmentTable table = factory.align(witnessA);
//    final IAnalysis alignment = factory.analyseOldStyle(table, witnessB);
//    final List<ISequence> matches = alignment.getSequences();
//    assertEquals(2, matches.size());
//    final ISequence sequence = matches.get(0);
//    assertEquals("the black", sequence.getNormalized());
//    //    final IColumns columnsA = match.getColumnsA();
//    //    assertEquals(1, columnsA.getBeginPosition());
//    //    assertEquals(4, columnsA.getEndPosition());
//  }
//
//  @Test
//  public void testMatchesWithIndex() {
//    final IWitness a = factory.createWitness("A", "The black cat");
//    final IWitness b = factory.createWitness("B", "The black and white cat");
//    final IAlignmentTable table = factory.align(a);
//    final IAnalysis alignment = factory.analyseOldStyle(table, b);
//    final List<ISequence> matches = alignment.getSequences();
//    assertContains(matches, "the black");
//    assertContains(matches, "cat");
//    assertEquals(2, matches.size());
//  }
//
//  @Ignore
//  @Test
//  public void testDroughtOfMay() {
//    final IWitness a = factory.createWitness("A", "When April with his showers sweet with fruit The drought of March has pierced unto the root");
//    final IWitness b = factory.createWitness("B", "When showers sweet with April fruit The March of drought has pierced to the root");
//    final IWitness c = factory.createWitness("C", "When showers sweet with April fruit The drought of March has pierced the rood");
//    final IAlignmentTable table = factory.align(a, b);
//    final IAnalysis alignment = factory.analyseOldStyle(table, c);
//    final List<ISequence> matches = alignment.getSequences();
//    assertContains(matches, "showers sweet with");
//    assertContains(matches, "has pierced");
//    assertEquals(2, matches.size());
//  }
//
//  @Test
//  public void testTwoEqualPossibilities1() {
//    // test a -> a a
//    final IWitness witnessA = factory.createWitness("A", "a b");
//    final IWitness witnessB = factory.createWitness("B", "a b a b");
//    final IAlignmentTable table = factory.align(witnessA);
//    final List<ITokenMatch> matches = factory.alignOldStyle(table, witnessB).getTokenMatches();
//    assertEquals(2, matches.size());
//    ITokenMatch match = matches.get(0);
//    assertEquals(1, match.getWitnessToken().getPosition());
//  }
//
//  @Test
//  public void testTwoEqualPossibilities2() {
//    // test a -> a a
//    final IWitness witnessA = factory.createWitness("A", "a b a b");
//    final IWitness witnessB = factory.createWitness("B", "a b");
//    final IAlignmentTable table = factory.align(witnessA);
//    final List<ITokenMatch> matches = factory.alignOldStyle(table, witnessB).getTokenMatches();
//    assertEquals(2, matches.size());
//    ITokenMatch match = matches.get(0);
//    assertEquals(1, match.getBaseToken().getPosition());
//  }

  final Function<ISequence, String> function = new Function<ISequence, String>() {
    @Override
    public String apply(final ISequence match) {
      return match.getNormalized();
    }
  };

  private void assertContains(final List<ISequence> matches, final String string) {
    final Iterable<String> normalizedMatches = Iterables.transform(matches, function);
    assertTrue(string + " not found in matches: " + Joiner.on(",").join(normalizedMatches), Lists.newArrayList(normalizedMatches).contains(string));
  }

}
