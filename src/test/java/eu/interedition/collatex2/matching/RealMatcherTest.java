package eu.interedition.collatex2.matching;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Set;

import org.junit.Test;

import com.google.common.base.Function;
import com.google.common.base.Join;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import eu.interedition.collatex2.implementation.Factory;
import eu.interedition.collatex2.implementation.matching.RealMatcher;
import eu.interedition.collatex2.implementation.matching.worddistance.NormalizedLevenshtein;
import eu.interedition.collatex2.implementation.matching.worddistance.WordDistance;
import eu.interedition.collatex2.interfaces.IMatch;
import eu.interedition.collatex2.interfaces.IWitness;

public class RealMatcherTest {
  @Test
  public void testMatchesWithIndex() {
    final Factory factory = new Factory();
    final IWitness a = factory.createWitness("A", "The black cat");
    final IWitness b = factory.createWitness("B", "The black and white cat");
    final WordDistance distanceMeasure = new NormalizedLevenshtein();
    final Set<IMatch> matches = RealMatcher.findMatchesWithIndex(a, b, distanceMeasure);
    assertContains(matches, "the");
    assertContains(matches, "black");
    assertContains(matches, "cat");
    assertEquals(3, matches.size());
  }

  final Function<IMatch, String> function = new Function<IMatch, String>() {
    @Override
    public String apply(final IMatch match) {
      return match.getNormalized();
    }
  };

  private void assertContains(final Set<IMatch> matches, final String string) {
    final Iterable<String> normalizedMatches = Iterables.transform(matches, function);
    assertTrue(string + " not found in matches: " + Join.join(",", normalizedMatches), Lists.newArrayList(normalizedMatches).contains(string));
  }
}
