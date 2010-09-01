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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import eu.interedition.collatex2.implementation.CollateXEngine;
import eu.interedition.collatex2.implementation.PairwiseAlignmentHelper;
import eu.interedition.collatex2.implementation.tokenmatching.TokenIndexMatcher;
import eu.interedition.collatex2.interfaces.IAlignment;
import eu.interedition.collatex2.interfaces.IAlignmentTable;
import eu.interedition.collatex2.interfaces.IColumns;
import eu.interedition.collatex2.interfaces.IMatch;
import eu.interedition.collatex2.interfaces.IWitness;

public class IndexMatcherTest {
  private static CollateXEngine factory;

  @BeforeClass
  public static void setup() {
    factory = new CollateXEngine();
  }

  @Test
  public void testEverythingIsUnique() {
    final IWitness witnessA = factory.createWitness("A", "everything is unique should be no problem");
    final IWitness witnessB = factory.createWitness("B", "everything is unique");
    final IAlignment alignment = PairwiseAlignmentHelper.align(factory, witnessA, witnessB);
    final List<IMatch> matches = alignment.getMatches();
    assertEquals(1, matches.size());
    final IMatch match = matches.get(0);
    assertEquals("everything is unique", match.getNormalized());
    final IColumns columnsA = match.getColumns();
    assertEquals(1, columnsA.getBeginPosition());
    assertEquals(3, columnsA.getEndPosition());
  }

  @Test
  public void testEverythingIsUniqueTwoWitnesses() {
    final IWitness witnessA = factory.createWitness("A", "everything is unique should be no problem");
    final IWitness witnessB = factory.createWitness("B", "this one very different");
    final IWitness witnessC = factory.createWitness("C", "everything is different");
    final IAlignmentTable table = factory.align(witnessA, witnessB);
    final List<IMatch> matches = TokenIndexMatcher.getMatchesUsingWitnessIndex(table, witnessC);
    assertEquals(3, matches.size());
    final IMatch match = matches.get(0);
    assertEquals("everything", match.getNormalized());
    final IColumns columnsA = match.getColumns();
    assertEquals(1, columnsA.getBeginPosition());
    assertEquals(1, columnsA.getEndPosition());
    final IMatch match2 = matches.get(1);
    assertEquals("is", match2.getNormalized());
    final IColumns columnsB = match2.getColumns();
    assertEquals(2, columnsB.getBeginPosition());
    assertEquals(2, columnsB.getEndPosition());
    final IMatch match3 = matches.get(2);
    assertEquals("different", match3.getNormalized());
    final IColumns columnsC = match3.getColumns();
    assertEquals(4, columnsC.getBeginPosition());
    assertEquals(4, columnsC.getEndPosition());
  }

  @Test
  public void testOverlappingMatches() {
    final IWitness witnessA = factory.createWitness("A", "everything is unique should be no problem");
    final IWitness witnessB = factory.createWitness("B", "this one is different");
    final IWitness witnessC = factory.createWitness("C", "everything is different");
    final IAlignmentTable table = factory.align(witnessA, witnessB);
    final List<IMatch> matches = TokenIndexMatcher.getMatchesUsingWitnessIndex(table, witnessC);
    assertEquals(3, matches.size());
    final IMatch match = matches.get(0);
    assertEquals("everything", match.getNormalized());
    final IColumns columnsA = match.getColumns();
    assertEquals(1, columnsA.getBeginPosition());
    assertEquals(1, columnsA.getEndPosition());
    final IMatch match2 = matches.get(1);
    assertEquals("is", match2.getNormalized());
    final IColumns columnsB = match2.getColumns();
    assertEquals(3, columnsB.getBeginPosition());
    assertEquals(3, columnsB.getEndPosition());
    final IMatch match3 = matches.get(2);
    assertEquals("different", match3.getNormalized());
    final IColumns columnsC = match3.getColumns();
    assertEquals(4, columnsC.getBeginPosition());
    assertEquals(4, columnsC.getEndPosition());
  }

  //TODO: to make this work we need ngrams of 4 tokens!
  @Ignore
  @Test
  public void testGetMatchesUsingWitnessIndex() {
    final IWitness witnessA = factory.createWitness("A", "The big black cat and the big black rat");
    final IWitness witnessB = factory.createWitness("B", "The big black");
    final IAlignmentTable table = factory.align(witnessA);
    final IAlignment alignment = factory.createAlignmentUsingIndex(table, witnessB);
    final List<IMatch> matches = alignment.getMatches();
    assertEquals(1, matches.size());
    final IMatch match = matches.get(0);
    assertEquals("the big black", match.getNormalized());
    final IColumns columnsA = match.getColumns();
    assertEquals(1, columnsA.getBeginPosition());
    assertEquals(3, columnsA.getEndPosition());
  }

  //Note: internally this gives # the big black and the big black cat as matches
  @Test
  public void testGetMatchesUsingWitnessIndexWithOverlapping() {
    final IWitness witnessA = factory.createWitness("A", "the big black cat and the big black rat");
    final IWitness witnessB = factory.createWitness("B", "the big black cat");
    final IAlignmentTable table = factory.align(witnessA);
    final IAlignment alignment = factory.createAlignmentUsingIndex(table, witnessB);
    final List<IMatch> matches = alignment.getMatches();
    //    final List<IMatch> matches = Factory.getMatchesUsingWitnessIndex(table, witnessB, new NormalizedLevenshtein());
    assertEquals(1, matches.size());
    final IMatch match = matches.get(0);
    assertEquals("the big black cat", match.getNormalized());
    final IColumns columnsA = match.getColumns();
    assertEquals(1, columnsA.getBeginPosition());
    assertEquals(4, columnsA.getEndPosition());
  }

  //TODO: make convenience method for creation of AlignmentTable on Factory!

  @Test
  public void testOverlappingMatches2() {
    final IWitness witnessA = factory.createWitness("A", "the black cat and the black mat");
    final IWitness witnessB = factory.createWitness("B", "the black dog and the black mat");
    final IAlignmentTable table = factory.align(witnessA);
    final IAlignment alignment = factory.createAlignmentUsingIndex(table, witnessB);
    final List<IMatch> matches = alignment.getMatches();
    assertEquals(2, matches.size());
    final IMatch match = matches.get(0);
    assertEquals("the black", match.getNormalized());
    //    final IColumns columnsA = match.getColumnsA();
    //    assertEquals(1, columnsA.getBeginPosition());
    //    assertEquals(4, columnsA.getEndPosition());
  }

  @Test
  public void testMatchesWithIndex() {
    final IWitness a = factory.createWitness("A", "The black cat");
    final IWitness b = factory.createWitness("B", "The black and white cat");
    final IAlignmentTable table = factory.align(a);
    final IAlignment alignment = factory.createAlignmentUsingIndex(table, b);
    final List<IMatch> matches = alignment.getMatches();
    assertContains(matches, "the black");
    assertContains(matches, "cat");
    assertEquals(2, matches.size());
  }

  final Function<IMatch, String> function = new Function<IMatch, String>() {
    @Override
    public String apply(final IMatch match) {
      return match.getNormalized();
    }
  };

  private void assertContains(final List<IMatch> matches, final String string) {
    final Iterable<String> normalizedMatches = Iterables.transform(matches, function);
    assertTrue(string + " not found in matches: " + Joiner.on(",").join(normalizedMatches), Lists.newArrayList(normalizedMatches).contains(string));
  }

}
