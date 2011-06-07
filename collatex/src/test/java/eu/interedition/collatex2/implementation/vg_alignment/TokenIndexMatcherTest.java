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

package eu.interedition.collatex2.implementation.vg_alignment;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;

import eu.interedition.collatex2.implementation.CollateXEngine;
import eu.interedition.collatex2.implementation.vg_alignment.TokenIndexMatcher;
import eu.interedition.collatex2.interfaces.ITokenMatch;
import eu.interedition.collatex2.interfaces.IVariantGraph;
import eu.interedition.collatex2.interfaces.IWitness;

public class TokenIndexMatcherTest {
  private static CollateXEngine factory;

  @BeforeClass
  public static void setup() {
    factory = new CollateXEngine();
  }

  @Test
  public void testEverythingIsUnique() {
    final IWitness witnessA = factory.createWitness("A", "everything is unique should be no problem");
    final IWitness witnessB = factory.createWitness("B", "everything is unique");
    IVariantGraph graph = factory.graph(witnessA);
    TokenIndexMatcher matcher = new TokenIndexMatcher(graph);
    List<ITokenMatch> matches = matcher.getMatches(witnessB);
    assertEquals(3, matches.size());
    assertEquals("everything: 1 -> [everything]", matches.get(0).toString());
    assertEquals("is: 2 -> [is]", matches.get(1).toString());
    assertEquals("unique: 3 -> [unique]", matches.get(2).toString());
  }
  
  @Test
  public void testEverythingIsUniqueTwoWitnesses() {
    final IWitness witnessA = factory.createWitness("A", "everything is unique should be no problem");
    final IWitness witnessB = factory.createWitness("B", "this one very different");
    final IWitness witnessC = factory.createWitness("C", "everything is different");
    IVariantGraph graph = factory.graph(witnessA, witnessB);
    TokenIndexMatcher matcher = new TokenIndexMatcher(graph);
    List<ITokenMatch> matches = matcher.getMatches(witnessC);
    assertEquals(3, matches.size());
    assertEquals("everything", matches.get(0).getNormalized());
    assertEquals("is", matches.get(1).getNormalized());
    assertEquals("different", matches.get(2).getNormalized());
  }

  @Test
  public void testOverlappingMatches() {
    final IWitness witnessA = factory.createWitness("A", "everything is unique should be no problem");
    final IWitness witnessB = factory.createWitness("B", "this one is different");
    final IWitness witnessC = factory.createWitness("C", "everything is different");
    IVariantGraph graph = factory.graph(witnessA, witnessB);
    TokenIndexMatcher matcher = new TokenIndexMatcher(graph);
    List<ITokenMatch> matches = matcher.getMatches(witnessC);
    assertEquals(3, matches.size());
    assertEquals("everything", matches.get(0).getNormalized());
    assertEquals("is", matches.get(1).getNormalized());
    assertEquals("different", matches.get(2).getNormalized());
  }

  
  @Test
  public void testGetMatchesUsingWitnessIndex() {
    final IWitness witnessA = factory.createWitness("A", "The big black cat and the big black rat");
    final IWitness witnessB = factory.createWitness("B", "The big black");
    final IVariantGraph graph = factory.graph(witnessA);
    TokenIndexMatcher matcher = new TokenIndexMatcher(graph);
    List<ITokenMatch> matches = matcher.getMatches(witnessB);
    assertEquals(3, matches.size());
    assertEquals("the: 1 -> [the]", matches.get(0).toString());
    assertEquals("big: 2 -> [big]", matches.get(1).toString());
    assertEquals("black: 3 -> [black]", matches.get(2).toString());
  }

  //Note: internally this gives # the big black and the big black cat as matches
  @Test
  public void testGetMatchesUsingWitnessIndexWithOverlapping() {
    final IWitness witnessA = factory.createWitness("A", "the big black cat and the big black rat");
    final IWitness witnessB = factory.createWitness("B", "the big black cat");
    final IVariantGraph graph = factory.graph(witnessA);
    TokenIndexMatcher matcher = new TokenIndexMatcher(graph);
    List<ITokenMatch> matches = matcher.getMatches(witnessB);
    assertEquals(4, matches.size());
    assertEquals("the: 1 -> [the]", matches.get(0).toString());
    assertEquals("big: 2 -> [big]", matches.get(1).toString());
    assertEquals("black: 3 -> [black]", matches.get(2).toString());
    assertEquals("cat: 4 -> [cat]", matches.get(3).toString());
  }

  @Test
  public void testOverlappingMatches2() {
    final IWitness witnessA = factory.createWitness("A", "the black cat and the black mat");
    final IWitness witnessB = factory.createWitness("B", "the black dog and the black mat");
    final IVariantGraph graph = factory.graph(witnessA);
    TokenIndexMatcher matcher = new TokenIndexMatcher(graph);
    List<ITokenMatch> matches = matcher.getMatches(witnessB);
    assertEquals(6, matches.size());
    assertEquals("the: 1 -> [the]", matches.get(0).toString());
    assertEquals("black: 2 -> [black]", matches.get(1).toString());
    assertEquals("and: 4 -> [and]", matches.get(2).toString());
    assertEquals("the: 5 -> [the]", matches.get(3).toString());
    assertEquals("black: 6 -> [black]", matches.get(4).toString());
    assertEquals("mat: 7 -> [mat]", matches.get(5).toString());
  }

  @Test
  public void testMatchesWithIndex() {
    final IWitness witnessA = factory.createWitness("A", "The black cat");
    final IWitness witnessB = factory.createWitness("B", "The black and white cat");
    final IVariantGraph graph = factory.graph(witnessA);
    TokenIndexMatcher matcher = new TokenIndexMatcher(graph);
    List<ITokenMatch> matches = matcher.getMatches(witnessB);
    assertEquals(3, matches.size());
    assertEquals("the: 1 -> [the]", matches.get(0).toString());
    assertEquals("black: 2 -> [black]", matches.get(1).toString());
    assertEquals("cat: 5 -> [cat]", matches.get(2).toString());
  }
  
  @Test
  public void testTwoEqualPossibilities2() {
    // test a a -> a
    final IWitness witnessA = factory.createWitness("A", "a a");
    final IWitness witnessB = factory.createWitness("B", "a");
    final IVariantGraph graph = factory.graph(witnessA);
    TokenIndexMatcher matcher = new TokenIndexMatcher(graph);
    List<ITokenMatch> matches = matcher.getMatches(witnessB);
    assertEquals(1, matches.size());
    ITokenMatch match = matches.get(0);
    assertEquals(graph.getTokens(witnessA).get(0), match.getBaseToken());
    assertEquals(witnessB.getTokens().get(0), match.getWitnessToken());
  }




}
