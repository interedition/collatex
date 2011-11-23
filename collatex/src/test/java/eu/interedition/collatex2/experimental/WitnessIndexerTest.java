package eu.interedition.collatex2.experimental;

import static eu.interedition.collatex2.implementation.vg_alignment.TokenLinker.toString;
import static org.junit.Assert.assertEquals;

import java.util.List;

import com.google.common.collect.Lists;
import eu.interedition.collatex2.implementation.matching.EqualityTokenComparator;
import eu.interedition.collatex2.implementation.matching.Matches;
import eu.interedition.collatex2.implementation.vg_alignment.TokenLinker;
import org.junit.BeforeClass;
import org.junit.Test;

import eu.interedition.collatex2.implementation.CollateXEngine;
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
    List<List<INormalizedToken>> sequences = TokenLinker.findUniqueTokenSequences(b, result);
    INormalizedToken soft = b.getTokens().get(1);
    INormalizedToken light = b.getTokens().get(3);
    List<INormalizedToken> expectedSequence = Lists.newArrayList(soft, light);
    List<INormalizedToken> firstSequence = sequences.get(0);
    assertEquals(expectedSequence, firstSequence);
    INormalizedToken any = b.getTokens().get(5);
    INormalizedToken light2 = b.getTokens().get(6);
    List<INormalizedToken> secondSequence = sequences.get(1);
    expectedSequence = Lists.newArrayList(light, any);
    assertEquals(expectedSequence, secondSequence);
    List<INormalizedToken> thirdSequence = sequences.get(2);
    expectedSequence = Lists.newArrayList(any, light2);
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
    List<List<INormalizedToken>> sequences = TokenLinker.findUniqueTokenSequences(c, result);
    assertEquals("# very", TokenLinker.toString(sequences.get(0)));
    assertEquals("very happy is", TokenLinker.toString(sequences.get(1)));
    assertEquals("# very happy", TokenLinker.toString(sequences.get(2)));
    assertEquals("happy is", TokenLinker.toString(sequences.get(3)));
    assertEquals("is the", TokenLinker.toString(sequences.get(4)));
    assertEquals("the cat #", TokenLinker.toString(sequences.get(5)));
    assertEquals("is the cat", TokenLinker.toString(sequences.get(6)));
    assertEquals("cat #", TokenLinker.toString(sequences.get(7)));
  }



}
