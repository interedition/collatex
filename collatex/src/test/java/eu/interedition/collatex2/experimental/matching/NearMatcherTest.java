package eu.interedition.collatex2.experimental.matching;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.google.common.collect.ListMultimap;

import eu.interedition.collatex2.experimental.MyNewCollateXEngine;
import eu.interedition.collatex2.implementation.CollateXEngine;
import eu.interedition.collatex2.implementation.matching.NearTokenComparator;
import eu.interedition.collatex2.implementation.matching.TokenMatcher;
import eu.interedition.collatex2.interfaces.INormalizedToken;
import eu.interedition.collatex2.interfaces.IWitness;

public class NearMatcherTest {
  
  @Test
  public void testTokenMatchingWithNearTokenComparator() {
    CollateXEngine engine = new MyNewCollateXEngine();
    IWitness a = engine.createWitness("A", "near matching yeah");
    IWitness b = engine.createWitness("B", "nar matching");
    TokenMatcher matcher = new TokenMatcher();
    matcher.setTokenComparator(new NearTokenComparator());
    ListMultimap<INormalizedToken, INormalizedToken> matches = matcher.match(a, b);
    assertEquals(a.getTokens().get(0), matches.get(b.getTokens().get(0)).get(0));
    assertEquals(a.getTokens().get(1), matches.get(b.getTokens().get(1)).get(0));
    assertEquals(2, matches.size());
  }
}
