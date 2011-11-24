package eu.interedition.collatex2.experimental.matching;

import static org.junit.Assert.assertEquals;

import com.google.common.collect.Iterables;
import com.google.common.collect.Multimap;
import eu.interedition.collatex2.AbstractTest;
import eu.interedition.collatex2.implementation.matching.Matches;
import org.junit.Test;

import eu.interedition.collatex2.implementation.CollateXEngine;
import eu.interedition.collatex2.implementation.matching.EditDistanceTokenComparator;
import eu.interedition.collatex2.interfaces.INormalizedToken;
import eu.interedition.collatex2.interfaces.IWitness;

public class NearMatcherTest extends AbstractTest {
  
  @Test
  public void nearTokenMatching() {
    final IWitness[] w = createWitnesses("near matching yeah", "nar matching");
    final Multimap<INormalizedToken, INormalizedToken> matches = Matches.between(w[0], w[1], new EditDistanceTokenComparator()).getAll();

    assertEquals(2, matches.size());
    assertEquals(w[0].getTokens().get(0), Iterables.get(matches.get(w[1].getTokens().get(0)), 0));
    assertEquals(w[0].getTokens().get(1), Iterables.get(matches.get(w[1].getTokens().get(1)), 0));
  }
}
