package eu.interedition.collatex2.experimental;

import static org.junit.Assert.assertEquals;

import java.util.List;

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
}
