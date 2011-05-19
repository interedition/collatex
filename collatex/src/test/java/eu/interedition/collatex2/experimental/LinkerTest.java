package eu.interedition.collatex2.experimental;

import static org.junit.Assert.assertEquals;

import java.util.Map;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import eu.interedition.collatex2.implementation.CollateXEngine;
import eu.interedition.collatex2.interfaces.INormalizedToken;
import eu.interedition.collatex2.interfaces.IWitness;

public class LinkerTest {
  private static CollateXEngine engine;

  @BeforeClass
  public static void setup() {
    engine = new MyNewCollateXEngine();
  }

  @Test
  public void testDirkVincent4() {
    IWitness a = engine.createWitness("01b", "Its soft light neither daylight nor moonlight nor starlight nor any light he could remember from the days & nights when day followed night & vice versa.");
    IWitness b = engine.createWitness("10a", "Its soft changeless light unlike any light he could remember from the days and nights when day followed hard on night and vice versa.");
    MyNewLinker linker = new MyNewLinker();
    Map<INormalizedToken, INormalizedToken> tokens = linker.link(a, b);
    INormalizedToken itsA = a.getTokens().get(0);
    INormalizedToken itsB = b.getTokens().get(0);
    assertEquals(itsA, tokens.get(itsB));
    INormalizedToken lightA = a.getTokens().get(2);
    INormalizedToken lightB = b.getTokens().get(3);
    assertEquals(lightA, tokens.get(lightB));
    INormalizedToken light2A = a.getTokens().get(11);
    INormalizedToken light2B = b.getTokens().get(6);
    assertEquals(light2A, tokens.get(light2B));
  }
  
  //TODO: Linking does not work good enough, it can not yet handle
  //TODO: the repetition of "the cat" and "very happy"
  @Ignore
  @Test
  public void testLinkingRepetitionCausedByTransposition() {
    // IWitness a = "the cat is very happy";
    // IWitness b = "very happy is the cat";
    IWitness superbase = engine.createWitness("superbase", "the cat very happy is very happy the cat");
    IWitness c = engine.createWitness("C", "very delitied and happy is the cat");
    MyNewLinker linker = new MyNewLinker();
    Map<INormalizedToken, INormalizedToken> tokens = linker.link(superbase, c);
    //System.out.println(tokens);
    INormalizedToken verySB = superbase.getTokens().get(2);
    INormalizedToken veryC = c.getTokens().get(0);
    assertEquals(verySB, tokens.get(veryC));
  }

}
