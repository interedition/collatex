package eu.interedition.collatex.implementation.matching;

import com.google.common.collect.Iterables;
import com.google.common.collect.Multimap;
import eu.interedition.collatex.AbstractTest;
import eu.interedition.collatex.interfaces.INormalizedToken;
import eu.interedition.collatex.interfaces.IWitness;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class NearMatcherTest extends AbstractTest {
  
  @Test
  public void nearTokenMatching() {
    final IWitness[] w = createWitnesses("near matching yeah", "nar matching");
    final Multimap<INormalizedToken, INormalizedToken> matches = Matches.between(w[0], w[1], new EditDistanceTokenComparator()).getAll();

    assertEquals(4, matches.size()); // 2 matches plus start/end marker matches
    assertEquals(w[0].getTokens().get(0), Iterables.get(matches.get(w[1].getTokens().get(0)), 0));
    assertEquals(w[0].getTokens().get(1), Iterables.get(matches.get(w[1].getTokens().get(1)), 0));
  }
}
