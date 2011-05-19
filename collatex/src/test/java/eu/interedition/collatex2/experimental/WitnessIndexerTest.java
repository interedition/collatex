package eu.interedition.collatex2.experimental;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;

import com.google.common.collect.ListMultimap;

import eu.interedition.collatex2.implementation.CollateXEngine;
import eu.interedition.collatex2.interfaces.INormalizedToken;
import eu.interedition.collatex2.interfaces.IWitness;

public class WitnessIndexerTest {
  private static CollateXEngine engine;

  @BeforeClass
  public static void setup() {
    engine = new MyNewCollateXEngine();
  }

  @Test
  public void testWitnessIndexingDirkVincent2() {
    IWitness a = engine.createWitness("01b", "Its soft light neither daylight nor moonlight nor starlight nor any light he could remember from the days & nights when day followed night & vice versa.");
    IWitness b = engine.createWitness("10a", "Its soft changeless light unlike any light he could remember from the days and nights when day followed hard on night and vice versa.");
    MyNewMatcher matcher = new MyNewMatcher();
    ListMultimap<INormalizedToken, INormalizedToken> matches = matcher.match(a, b);
    MatchResultAnalyzer analyzer = new MatchResultAnalyzer();
    IMatchResult result = analyzer.analyze(a, b);
    MyNewWitnessIndexer indexer = new MyNewWitnessIndexer();
    IWitnessIndex index = indexer.index(b, matches, result);
    List<ITokenSequence> sequences = index.getTokenSequences();
    INormalizedToken soft = b.getTokens().get(1);
    INormalizedToken light = b.getTokens().get(3);
    ITokenSequence expectedSequence = new TokenSequence(soft, light);
    ITokenSequence firstSequence = sequences.get(0);
    assertEquals(expectedSequence, firstSequence);
    INormalizedToken any = b.getTokens().get(5);
    INormalizedToken light2 = b.getTokens().get(6);
    ITokenSequence secondSequence = sequences.get(1);
    expectedSequence = new TokenSequence(any, light2);
    assertEquals(expectedSequence, secondSequence);
  }
  
  //TODO: WitnessIndexing does not work good enough, it can not yet handle
  //TODO: the repetition of "the cat" and "very happy"
  //TODO: the tokensequences generated are too short!
  //@Ignore
  @Test
  public void testWitnessIndexingRepetitionCausedByTransposition() {
    // IWitness a = "the cat is very happy";
    // IWitness b = "very happy is the cat";
    IWitness superbase = engine.createWitness("superbase", "the cat very happy is very happy the cat");
    IWitness c = engine.createWitness("C", "very delitied and happy is the cat");
    MyNewMatcher matcher = new MyNewMatcher();
    ListMultimap<INormalizedToken, INormalizedToken> matches = matcher.match(superbase, c);
    MatchResultAnalyzer analyzer = new MatchResultAnalyzer();
    //TODO: strange that I don't have to pass the matches to the analyzer here!
    IMatchResult result = analyzer.analyze(superbase, c);
    MyNewWitnessIndexer indexer = new MyNewWitnessIndexer();
    IWitnessIndex index = indexer.index(c, matches, result);
    List<ITokenSequence> sequences = index.getTokenSequences();
    System.out.println(sequences);
  }



}
