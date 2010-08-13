package eu.interedition.collatex2.experimental.tokenmatching;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import eu.interedition.collatex2.experimental.tokenmatching.legacy.AlignmentTableIndexMatcher;
import eu.interedition.collatex2.implementation.CollateXEngine;
import eu.interedition.collatex2.implementation.PairwiseAlignmentHelper;
import eu.interedition.collatex2.interfaces.IAlignment;
import eu.interedition.collatex2.interfaces.IAlignmentTable;
import eu.interedition.collatex2.interfaces.IColumns;
import eu.interedition.collatex2.interfaces.IMatch;
import eu.interedition.collatex2.interfaces.INormalizedToken;
import eu.interedition.collatex2.interfaces.ITokenMatch;
import eu.interedition.collatex2.interfaces.IWitness;

//TODO: import IAlignment should be removed
//The IndexMatcher should give back the token matches
//In the near future IAlignment will just be a few on two rows of the alignment table
//The IndexMatcher will be the class that matches a witness against the table or the graph
// and returns TokenMatches
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
    AlignmentTableIndexMatcher indexMatcher = new AlignmentTableIndexMatcher(table);
    List<ITokenMatch> matches = indexMatcher.getMatches(witnessC);
    assertEquals(3, matches.size());
    final ITokenMatch match = matches.get(0);
    assertEquals("everything", match.getNormalized());
    final INormalizedToken columnsA = match.getTableToken();
    assertEquals(1, columnsA.getPosition());
    final ITokenMatch match2 = matches.get(1);
    assertEquals("is", match2.getNormalized());
    final INormalizedToken columnsB = match2.getTableToken();
    assertEquals(2, columnsB.getPosition());
    final ITokenMatch match3 = matches.get(2);
    assertEquals("different", match3.getNormalized());
    final INormalizedToken columnsC = match3.getTableToken();
    assertEquals(4, columnsC.getPosition());
  }

  @Test
  public void testOverlappingMatches() {
    final IWitness witnessA = factory.createWitness("A", "everything is unique should be no problem");
    final IWitness witnessB = factory.createWitness("B", "this one is different");
    final IWitness witnessC = factory.createWitness("C", "everything is different");
    final IAlignmentTable table = factory.align(witnessA, witnessB);
    AlignmentTableIndexMatcher indexMatcher = new AlignmentTableIndexMatcher(table);
    final List<ITokenMatch> matches = indexMatcher.getMatches(witnessC);
    assertEquals(3, matches.size());
    final ITokenMatch match = matches.get(0);
    assertEquals("everything", match.getNormalized());
    final INormalizedToken columnsA = match.getTableToken();
    assertEquals(1, columnsA.getPosition());
    final ITokenMatch match2 = matches.get(1);
    assertEquals("is", match2.getNormalized());
    final INormalizedToken columnsB = match2.getTableToken();
    assertEquals(3, columnsB.getPosition());
    final ITokenMatch match3 = matches.get(2);
    assertEquals("different", match3.getNormalized());
    final INormalizedToken columnsC = match3.getTableToken();
    assertEquals(4, columnsC.getPosition());
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
    final IColumns columns = match.getColumns();
    assertEquals(1, columns.getBeginPosition());
    assertEquals(2, columns.getEndPosition());
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
  
  @Test
  public void testTwoEqualPossibilities1() {
    // test a -> a a
    final IWitness a = factory.createWitness("A", "a");
    final IWitness b = factory.createWitness("B", "a a");
    final IAlignmentTable table = factory.align(a);
    AlignmentTableIndexMatcher matcher = new AlignmentTableIndexMatcher(table);
    List<ITokenMatch> matches = matcher.getMatches(b);
    assertEquals(1, matches.size());
    ITokenMatch match = matches.get(0);
    assertEquals(1, match.getTableToken().getPosition());
    assertEquals(1, match.getWitnessToken().getPosition());
  }

  @Test
  public void testTwoEqualPossibilities2() {
    // test a a -> a
    final IWitness a = factory.createWitness("A", "a a");
    final IWitness b = factory.createWitness("B", "a");
    final IAlignmentTable table = factory.align(a);
    AlignmentTableIndexMatcher matcher = new AlignmentTableIndexMatcher(table);
    List<ITokenMatch> matches = matcher.getMatches(b);
    assertEquals(1, matches.size());
    ITokenMatch match = matches.get(0);
    assertEquals(1, match.getTableToken().getPosition());
    assertEquals(1, match.getWitnessToken().getPosition());
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
