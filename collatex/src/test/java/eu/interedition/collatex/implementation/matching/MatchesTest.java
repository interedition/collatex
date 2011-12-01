package eu.interedition.collatex.implementation.matching;

import static org.junit.Assert.assertEquals;
import eu.interedition.collatex.AbstractTest;
import eu.interedition.collatex.interfaces.IWitness;

import org.junit.Test;

public class MatchesTest extends AbstractTest {

  @Test
  public void test() {
    final IWitness[] w = createWitnesses("john and paul and george and ringo", "john and paul and george and ringo");
    final Matches matches = Matches.between(w[0], w[1], new EqualityTokenComparator());

    int expected_unmatched = 0;
    int expected_unique = 4; // john paul george ringo
    int expected_ambiguous = 9; // 3 x 3 and combinations
    extracted(matches, expected_unmatched, expected_unique, expected_ambiguous);
  }

  private void extracted(final Matches matches, int expected_unmatched, int expected_unique, int expected_ambiguous) {
    assertEquals(expected_unmatched, matches.getUnmatched().size());
    assertEquals(expected_unique, matches.getUnique().size());
    assertEquals(expected_ambiguous, matches.getAmbiguous().size());
    assertEquals(expected_unique + expected_ambiguous + expected_unmatched, matches.getAll().size());
  }
}
