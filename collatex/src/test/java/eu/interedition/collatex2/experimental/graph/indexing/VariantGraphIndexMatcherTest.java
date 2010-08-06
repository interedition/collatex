package eu.interedition.collatex2.experimental.graph.indexing;

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

  //NOTE: tests taken from IndexMatcherTest!
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
  public void testGetMatchesUsingWitnessIndex() {
    final IWitness witnessA = factory.createWitness("A", "The big black cat and the big black rat");
    final IWitness witnessB = factory.createWitness("B", "The big black");
    final IVariantGraph graph = factory.graph(witnessA);
    VariantGraphIndexMatcher matcher = new VariantGraphIndexMatcher(graph);
    List<ITokenMatch> matches = matcher.getMatches(witnessB);
    System.out.println(matches);
    assertEquals(3, matches.size());
    assertEquals("the: 1 -> 1", matches.get(0).toString());
    assertEquals("big: 2 -> 2", matches.get(1).toString());
    assertEquals("black: 3 -> 3", matches.get(2).toString());
  }



}
