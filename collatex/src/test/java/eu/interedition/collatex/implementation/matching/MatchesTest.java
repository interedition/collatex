package eu.interedition.collatex.implementation.matching;

import static org.junit.Assert.*;

import java.util.Set;

import com.google.common.collect.ListMultimap;

import eu.interedition.collatex.AbstractTest;
import eu.interedition.collatex.interfaces.INormalizedToken;
import eu.interedition.collatex.interfaces.IWitness;

import org.junit.Test;

public class MatchesTest extends AbstractTest {

  @Test
  public void test1() {
    final IWitness[] w = createWitnesses("john and paul and george and ringo", "john and paul and george and ringo");
    final Matches matches = Matches.between(w[0], w[1], new EqualityTokenComparator());

    int expected_unmatched = 0;
    int expected_unique = 4; // john paul george ringo
    int expected_ambiguous = 9; // 3 x 3 and combinations
    assertMatches(matches, expected_unmatched, expected_unique, expected_ambiguous);
  }

  @Test
  public void test2() {
    final IWitness[] w = createWitnesses("the white cat", "the black cat");
    final Matches matches = Matches.between(w[0], w[1], new EqualityTokenComparator());

    int expected_unmatched = 1; // black
    int expected_unique = 2; // the & cat
    int expected_ambiguous = 0;
    assertMatches(matches, expected_unmatched, expected_unique, expected_ambiguous);
  }

  private void assertMatches(final Matches matches, int expected_unmatched, int expected_unique, int expected_ambiguous) {
    Set<INormalizedToken> unmatched = matches.getUnmatched();
    LOG.info("unmatched: {}", unmatched);

    Set<INormalizedToken> unique = matches.getUnique();
    LOG.info("unique: {}", unique);

    Set<INormalizedToken> ambiguous = matches.getAmbiguous();
    LOG.info("ambiguous: {}", ambiguous);

    ListMultimap<INormalizedToken, INormalizedToken> all = matches.getAll();
    LOG.info("all: {}", all);

    assertEquals(expected_unmatched, unmatched.size());
    assertEquals(expected_unique, unique.size());
    assertEquals(expected_ambiguous, ambiguous.size());
    assertEquals(expected_unique + expected_ambiguous, all.size());
  }
}
