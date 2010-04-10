package eu.interedition.collatex2.implementation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.google.common.collect.Lists;

import eu.interedition.collatex2.implementation.alignmenttable.AlignmentTableCreator3;
import eu.interedition.collatex2.implementation.matching.worddistance.NormalizedLevenshtein;
import eu.interedition.collatex2.interfaces.IAlignment;
import eu.interedition.collatex2.interfaces.IAlignmentTable;
import eu.interedition.collatex2.interfaces.IColumns;
import eu.interedition.collatex2.interfaces.IMatch;
import eu.interedition.collatex2.interfaces.IWitness;

public class FactoryTest {
  private Factory factory;

  @Before
  public void setup() {
    factory = new Factory();
  }

  @Test
  public void testGetTokensWithMultiples() {
    final IWitness a = factory.createWitness("A", "the big black cat and the big black rat");
    final IWitness b = factory.createWitness("B", "the big black rat and the small white rat");
    final Set<String> tokensWithMultiples = Factory.getTokensWithMultiples(Lists.newArrayList(a, b));
    final String[] expectedTokens = { "the", "big", "black", "rat" };
    assertEquals(expectedTokens.length, tokensWithMultiples.size());
    for (final String expected : expectedTokens) {
      assertContains(tokensWithMultiples, expected);
    }
  }

  @Ignore
  @Test
  public void testGetPhrasesWithMultiples() {
    final IWitness a = factory.createWitness("A", "the big black cat and the big black rat");
    final IWitness b = factory.createWitness("B", "the big black rat and the small white rat");
    final Set<String> tokensWithMultiples = Factory.getPhrasesWithMultiples(a, b);
    final String[] expectedPhrases = { "the big black", "rat" };
    assertEquals(tokensWithMultiples.toString(), expectedPhrases.length, tokensWithMultiples.size());
    for (final String expected : expectedPhrases) {
      assertContains(tokensWithMultiples, expected);
    }
  }

  private void assertContains(final Set<String> stringSet, final String string) {
    assertTrue(string + " not found in " + stringSet, stringSet.contains(string));
  }

  @Test
  public void testEverythingIsUnique() {
    final IWitness witnessA = factory.createWitness("A", "everything is unique should be no problem");
    final IWitness witnessB = factory.createWitness("B", "everything is unique");
    final IAlignmentTable table = AlignmentTableCreator3.createAlignmentTable(Lists.newArrayList(witnessA), Factory.NULLCALLBACK);
    final IAlignment alignment = factory.createAlignmentUsingIndex(table, witnessB);
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
    final IAlignmentTable table = AlignmentTableCreator3.createAlignmentTable(Lists.newArrayList(witnessA, witnessB), Factory.NULLCALLBACK);
    final List<IMatch> matches = Factory.getMatchesUsingWitnessIndex(table, witnessC, new NormalizedLevenshtein());
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
    final IAlignmentTable table = AlignmentTableCreator3.createAlignmentTable(Lists.newArrayList(witnessA, witnessB), Factory.NULLCALLBACK);
    final List<IMatch> matches = Factory.getMatchesUsingWitnessIndex(table, witnessC, new NormalizedLevenshtein());
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

  @Test
  public void testGetMatchesUsingWitnessIndex() {
    final IWitness witnessA = factory.createWitness("A", "The big black cat and the big black rat");
    final IWitness witnessB = factory.createWitness("B", "The big black");
    final IAlignmentTable table = AlignmentTableCreator3.createAlignmentTable(Lists.newArrayList(witnessA), Factory.NULLCALLBACK);
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
    final IAlignmentTable table = AlignmentTableCreator3.createAlignmentTable(Lists.newArrayList(witnessA), Factory.NULLCALLBACK);
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
    final IAlignmentTable table = AlignmentTableCreator3.createAlignmentTable(Lists.newArrayList(witnessA), Factory.NULLCALLBACK);
    final IAlignment alignment = factory.createAlignmentUsingIndex(table, witnessB);
    final List<IMatch> matches = alignment.getMatches();
    assertEquals(2, matches.size());
    final IMatch match = matches.get(0);
    assertEquals("the black", match.getNormalized());
    //    final IColumns columnsA = match.getColumnsA();
    //    assertEquals(1, columnsA.getBeginPosition());
    //    assertEquals(4, columnsA.getEndPosition());
  }

  @Ignore
  @Test
  public void testJoinOverlappingMatches() {
    // TODO make this testcase
    final List<IMatch> matches = Lists.newArrayList();
    final List<IMatch> joined = Factory.joinOverlappingMatches(matches);
    assertEquals(1, joined.size());
  }
}
