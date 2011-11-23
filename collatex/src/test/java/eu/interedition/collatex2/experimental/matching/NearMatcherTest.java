package eu.interedition.collatex2.experimental.matching;

import static org.junit.Assert.assertEquals;

import com.google.common.collect.Iterables;
import com.google.common.collect.Multimap;
import eu.interedition.collatex2.implementation.matching.Matches;
import org.junit.Test;

import eu.interedition.collatex2.implementation.CollateXEngine;
import eu.interedition.collatex2.implementation.matching.EditDistanceTokenComparator;
import eu.interedition.collatex2.interfaces.INormalizedToken;
import eu.interedition.collatex2.interfaces.IWitness;

public class NearMatcherTest {
  
  @Test
  public void testTokenMatchingWithNearTokenComparator() {
    CollateXEngine engine = new CollateXEngine();
    IWitness a = engine.createWitness("A", "near matching yeah");
    IWitness b = engine.createWitness("B", "nar matching");
    Multimap<INormalizedToken, INormalizedToken> matches = Matches.between(a, b, new EditDistanceTokenComparator()).getAll();
    assertEquals(a.getTokens().get(0), Iterables.getFirst(matches.get(b.getTokens().get(0)), null));
    assertEquals(a.getTokens().get(1), Iterables.getFirst(matches.get(b.getTokens().get(1)), null));
    assertEquals(2, matches.size());
  }
}
