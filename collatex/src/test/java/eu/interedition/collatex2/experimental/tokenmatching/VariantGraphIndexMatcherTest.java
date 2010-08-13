package eu.interedition.collatex2.experimental.tokenmatching;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;

import eu.interedition.collatex2.experimental.graph.IVariantGraph;
import eu.interedition.collatex2.implementation.CollateXEngine;
import eu.interedition.collatex2.interfaces.ITokenMatch;
import eu.interedition.collatex2.interfaces.IWitness;

public class VariantGraphIndexMatcherTest {
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
    VariantGraphIndexMatcher matcher = new VariantGraphIndexMatcher(graph);
    List<ITokenMatch> matches = matcher.getMatches(witnessB);
    assertEquals(3, matches.size());
    assertEquals("everything: 1 -> 1", matches.get(0).toString());
    assertEquals("is: 2 -> 2", matches.get(1).toString());
    assertEquals("unique: 3 -> 3", matches.get(2).toString());
  }
  
  @Test
  public void testEverythingIsUniqueTwoWitnesses() {
    final IWitness witnessA = factory.createWitness("A", "everything is unique should be no problem");
    final IWitness witnessB = factory.createWitness("B", "this one very different");
    final IWitness witnessC = factory.createWitness("C", "everything is different");
    IVariantGraph graph = factory.graph(witnessA, witnessB);
    VariantGraphIndexMatcher matcher = new VariantGraphIndexMatcher(graph);
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
    VariantGraphIndexMatcher matcher = new VariantGraphIndexMatcher(graph);
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
    VariantGraphIndexMatcher matcher = new VariantGraphIndexMatcher(graph);
    List<ITokenMatch> matches = matcher.getMatches(witnessB);
    assertEquals(3, matches.size());
    assertEquals("the: 1 -> 1", matches.get(0).toString());
    assertEquals("big: 2 -> 2", matches.get(1).toString());
    assertEquals("black: 3 -> 3", matches.get(2).toString());
  }

  //Note: internally this gives # the big black and the big black cat as matches
  @Test
  public void testGetMatchesUsingWitnessIndexWithOverlapping() {
    final IWitness witnessA = factory.createWitness("A", "the big black cat and the big black rat");
    final IWitness witnessB = factory.createWitness("B", "the big black cat");
    final IVariantGraph graph = factory.graph(witnessA);
    VariantGraphIndexMatcher matcher = new VariantGraphIndexMatcher(graph);
    List<ITokenMatch> matches = matcher.getMatches(witnessB);
    assertEquals(4, matches.size());
    assertEquals("the: 1 -> 1", matches.get(0).toString());
    assertEquals("big: 2 -> 2", matches.get(1).toString());
    assertEquals("black: 3 -> 3", matches.get(2).toString());
    assertEquals("cat: 4 -> 4", matches.get(3).toString());
  }

  @Test
  public void testOverlappingMatches2() {
    final IWitness witnessA = factory.createWitness("A", "the black cat and the black mat");
    final IWitness witnessB = factory.createWitness("B", "the black dog and the black mat");
    final IVariantGraph graph = factory.graph(witnessA);
    VariantGraphIndexMatcher matcher = new VariantGraphIndexMatcher(graph);
    List<ITokenMatch> matches = matcher.getMatches(witnessB);
    assertEquals(6, matches.size());
    assertEquals("the: 1 -> 1", matches.get(0).toString());
    assertEquals("black: 2 -> 2", matches.get(1).toString());
    assertEquals("and: 4 -> 4", matches.get(2).toString());
    assertEquals("the: 5 -> 5", matches.get(3).toString());
    assertEquals("black: 6 -> 6", matches.get(4).toString());
    assertEquals("mat: 7 -> 7", matches.get(5).toString());
  }

  @Test
  public void testMatchesWithIndex() {
    final IWitness witnessA = factory.createWitness("A", "The black cat");
    final IWitness witnessB = factory.createWitness("B", "The black and white cat");
    final IVariantGraph graph = factory.graph(witnessA);
    VariantGraphIndexMatcher matcher = new VariantGraphIndexMatcher(graph);
    List<ITokenMatch> matches = matcher.getMatches(witnessB);
    assertEquals(3, matches.size());
    assertEquals("the: 1 -> 1", matches.get(0).toString());
    assertEquals("black: 2 -> 2", matches.get(1).toString());
    assertEquals("cat: 5 -> 3", matches.get(2).toString());
  }
  
  @Test
  public void testTwoEqualPossibilities1() {
    // test a -> a a
    final IWitness witnessA = factory.createWitness("A", "a");
    final IWitness witnessB = factory.createWitness("B", "a a");
    final IVariantGraph graph = factory.graph(witnessA);
    VariantGraphIndexMatcher matcher = new VariantGraphIndexMatcher(graph);
    List<ITokenMatch> matches = matcher.getMatches(witnessB);
    assertEquals(1, matches.size());
    ITokenMatch match = matches.get(0);
    assertEquals(1, match.getTableToken().getPosition());
    assertEquals(1, match.getWitnessToken().getPosition());
  }

  @Test
  public void testTwoEqualPossibilities2() {
    // test a a -> a
    final IWitness witnessA = factory.createWitness("A", "a a");
    final IWitness witnessB = factory.createWitness("B", "a");
    final IVariantGraph graph = factory.graph(witnessA);
    VariantGraphIndexMatcher matcher = new VariantGraphIndexMatcher(graph);
    List<ITokenMatch> matches = matcher.getMatches(witnessB);
    assertEquals(1, matches.size());
    ITokenMatch match = matches.get(0);
    assertEquals(1, match.getTableToken().getPosition());
    assertEquals(1, match.getWitnessToken().getPosition());
  }




}
