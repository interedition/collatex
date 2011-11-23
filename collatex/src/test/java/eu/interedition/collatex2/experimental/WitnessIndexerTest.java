package eu.interedition.collatex2.experimental;

import static org.junit.Assert.assertEquals;

import java.util.List;

import eu.interedition.collatex2.implementation.matching.EqualityTokenComparator;
import eu.interedition.collatex2.implementation.matching.Matches;
import org.junit.BeforeClass;
import org.junit.Test;

import eu.interedition.collatex2.implementation.CollateXEngine;
import eu.interedition.collatex2.implementation.vg_alignment.ITokenSequence;
import eu.interedition.collatex2.implementation.vg_alignment.IWitnessIndex;
import eu.interedition.collatex2.implementation.vg_alignment.WitnessIndexer;
import eu.interedition.collatex2.implementation.vg_alignment.TokenSequence;
import eu.interedition.collatex2.interfaces.INormalizedToken;
import eu.interedition.collatex2.interfaces.IWitness;

public class WitnessIndexerTest {
  private static CollateXEngine engine;

  @BeforeClass
  public static void setup() {
    engine = new CollateXEngine();
  }

  @Test
  public void testWitnessIndexingDirkVincent2() {
    IWitness a = engine.createWitness("01b", "Its soft light neither daylight nor moonlight nor starlight nor any light he could remember from the days & nights when day followed night & vice versa.");
    IWitness b = engine.createWitness("10a", "Its soft changeless light unlike any light he could remember from the days and nights when day followed hard on night and vice versa.");
    Matches result = Matches.between(a, b, new EqualityTokenComparator());
    WitnessIndexer indexer = new WitnessIndexer();
    IWitnessIndex index = indexer.index(b, result);
    List<ITokenSequence> sequences = index.getTokenSequences();
    INormalizedToken soft = b.getTokens().get(1);
    INormalizedToken light = b.getTokens().get(3);
    ITokenSequence expectedSequence = new TokenSequence(true, soft, light);
    ITokenSequence firstSequence = sequences.get(0);
    assertEquals(expectedSequence, firstSequence);
    INormalizedToken any = b.getTokens().get(5);
    INormalizedToken light2 = b.getTokens().get(6);
    ITokenSequence secondSequence = sequences.get(1);
    expectedSequence = new TokenSequence(false, light, any);
    assertEquals(expectedSequence, secondSequence);
    ITokenSequence thirdSequence = sequences.get(2);
    expectedSequence = new TokenSequence(true, any, light2);
    assertEquals(expectedSequence, thirdSequence);
    //NOTE: there are even more sequences
  }
  
  @Test
  public void testWitnessIndexingRepetitionCausedByTransposition() {
    // IWitness a = "the cat is very happy";
    // IWitness b = "very happy is the cat";
    IWitness superbase = engine.createWitness("superbase", "the cat very happy is very happy the cat");
    IWitness c = engine.createWitness("C", "very delitied and happy is the cat");
    //TODO: strange that I don't have to pass the matches to the analyzer here!
    Matches result = Matches.between(superbase, c, new EqualityTokenComparator());
    WitnessIndexer indexer = new WitnessIndexer();
    IWitnessIndex index = indexer.index(c, result);
    List<ITokenSequence> sequences = index.getTokenSequences();
    assertEquals("# very", sequences.get(0).getNormalized());
    assertEquals("very happy is", sequences.get(1).getNormalized());
    assertEquals("# very happy", sequences.get(2).getNormalized());
    assertEquals("happy is", sequences.get(3).getNormalized());
    assertEquals("is the", sequences.get(4).getNormalized());
    assertEquals("the cat #", sequences.get(5).getNormalized());
    assertEquals("is the cat", sequences.get(6).getNormalized());
    assertEquals("cat #", sequences.get(7).getNormalized());
  }



}
