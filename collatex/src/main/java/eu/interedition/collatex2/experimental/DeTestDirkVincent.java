package eu.interedition.collatex2.experimental;

import static org.junit.Assert.assertEquals;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import com.google.common.collect.ListMultimap;

import eu.interedition.collatex2.implementation.CollateXEngine;
import eu.interedition.collatex2.interfaces.INormalizedToken;
import eu.interedition.collatex2.interfaces.IWitness;

public class DeTestDirkVincent {

  @Test
  public void testDirkVincent() {
    CollateXEngine factory = new CollateXEngine();
    IWitness a = factory.createWitness("01b", "Its soft light neither daylight nor moonlight nor starlight nor any light he could remember from the days & nights when day followed night & vice versa.");
    IWitness b = factory.createWitness("10a", "Its soft changeless light unlike any light he could remember from the days and nights when day followed hard on night and vice versa.");
    MyNewMatcher matcher = new MyNewMatcher();
    ListMultimap<INormalizedToken, INormalizedToken> matches = matcher.match(a, b);
    INormalizedToken its = a.getTokens().get(0);
    INormalizedToken light = a.getTokens().get(2);
    List<INormalizedToken> matchedTokens;
    matchedTokens = matches.get(its);
    assertEquals("Its", matchedTokens.get(0).getContent());
    matchedTokens = matches.get(light);
    assertEquals(2, matchedTokens.size());
  }
  
  @Test
  public void testDirkVincent2() {
    CollateXEngine factory = new CollateXEngine();
    IWitness a = factory.createWitness("01b", "Its soft light neither daylight nor moonlight nor starlight nor any light he could remember from the days & nights when day followed night & vice versa.");
    IWitness b = factory.createWitness("10a", "Its soft changeless light unlike any light he could remember from the days and nights when day followed hard on night and vice versa.");
    MyNewMatcher matcher = new MyNewMatcher();
    ListMultimap<INormalizedToken, INormalizedToken> matches = matcher.match(a, b);
    //TODO: nu moet ik een witness index maken van de base
    //met de tokens erin die een unieke sequence voorstellen voor dubbelvoorkomend woord
    IWitnessIndex index = new MyNewWitnessIndex(a, matches);
    List<ITokenSequence> sequences = index.getTokenSequences();
    INormalizedToken soft = a.getTokens().get(1);
    INormalizedToken light = a.getTokens().get(2);
    INormalizedToken any = a.getTokens().get(10);
    INormalizedToken light2 = a.getTokens().get(11);
    ITokenSequence firstSequence = sequences.get(0);
    ITokenSequence secondSequence = sequences.get(1);
    ITokenSequence expectedSequence = new TokenSequence(soft, light);
    assertEquals(expectedSequence, firstSequence);
    expectedSequence = new TokenSequence(any, light2);
    assertEquals(expectedSequence, secondSequence);
  }

  @Test
  public void testVincentDirk3() {
    CollateXEngine factory = new CollateXEngine();
    IWitness a = factory.createWitness("01b", "Its soft light neither daylight nor moonlight nor starlight nor any light he could remember from the days & nights when day followed night & vice versa.");
    IWitness b = factory.createWitness("10a", "Its soft changeless light unlike any light he could remember from the days and nights when day followed hard on night and vice versa.");
    MyNewMatcher matcher = new MyNewMatcher();
    ListMultimap<INormalizedToken, INormalizedToken> matches = matcher.match(a, b);
    WitnessAfgeleide afgeleider = new WitnessAfgeleide();
    List<INormalizedToken> afgeleideWitness = afgeleider.calculateAfgeleide(b, matches);
    Iterator<INormalizedToken> tokenIterator = afgeleideWitness.iterator();
    assertEquals("Its", tokenIterator.next().getContent());
    assertEquals("soft", tokenIterator.next().getContent());
    assertEquals("light", tokenIterator.next().getContent());
    assertEquals("any", tokenIterator.next().getContent());
    assertEquals("light", tokenIterator.next().getContent());
    assertEquals("he", tokenIterator.next().getContent());
    assertEquals("could", tokenIterator.next().getContent());
  }
  
  
  @Test
  public void testDirkVincent4() {
    CollateXEngine factory = new CollateXEngine();
    IWitness a = factory.createWitness("01b", "Its soft light neither daylight nor moonlight nor starlight nor any light he could remember from the days & nights when day followed night & vice versa.");
    IWitness b = factory.createWitness("10a", "Its soft changeless light unlike any light he could remember from the days and nights when day followed hard on night and vice versa.");
    MyNewLinker linker = new MyNewLinker();
    Map<INormalizedToken, INormalizedToken> tokens = linker.link(a, b);
    INormalizedToken itsA = a.getTokens().get(0);
    INormalizedToken itsB = b.getTokens().get(0);
    assertEquals(itsB, tokens.get(itsA));
    INormalizedToken lightA = a.getTokens().get(2);
    INormalizedToken lightB = b.getTokens().get(3);
    assertEquals(lightB, tokens.get(lightA));
    INormalizedToken light2A = a.getTokens().get(11);
    INormalizedToken light2B = b.getTokens().get(6);
    assertEquals(light2B, tokens.get(light2A));
  }
}
