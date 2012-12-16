package eu.interedition.collatex.matching;

import static org.junit.Assert.*;

import java.util.Set;

import com.google.common.collect.ListMultimap;

import eu.interedition.collatex.AbstractTest;
import eu.interedition.collatex.Token;
import eu.interedition.collatex.VariantGraph;

import eu.interedition.collatex.simple.SimpleWitness;
import org.junit.Test;

public class MatchesTest extends AbstractTest {

  @Test
  public void test1() {
    final SimpleWitness[] w = createWitnesses("john and paul and george and ringo", "john and paul and george and ringo");
    final VariantGraph graph = collate(w[0]);
    final Matches matches = Matches.between(graph.vertices(), w[1].getTokens(), new EqualityTokenComparator());

    int expected_unmatched = 0;
    int expected_unique = 4; // john paul george ringo
    int expected_ambiguous = 3; // 3 ands in 2nd witness
    assertMatches(matches, expected_unmatched, expected_unique, expected_ambiguous);
  }

  @Test
  public void test2() {
    final SimpleWitness[] w = createWitnesses("the white cat", "the black cat");
    final VariantGraph graph = collate(w[0]);
    final Matches matches = Matches.between(graph.vertices(), w[1].getTokens(), new EqualityTokenComparator());

    int expected_unmatched = 1; // black
    int expected_unique = 2; // the & cat
    int expected_ambiguous = 0;
    assertMatches(matches, expected_unmatched, expected_unique, expected_ambiguous);
  }

  private void assertMatches(final Matches matches, int expected_unmatched, int expected_unique, int expected_ambiguous) {
    Set<Token> unmatched = matches.getUnmatched();
    LOG.debug("unmatched: {}", unmatched);

    Set<Token> unique = matches.getUnique();
    LOG.debug("unique: {}", unique);

    Set<Token> ambiguous = matches.getAmbiguous();
    LOG.debug("ambiguous: {}", ambiguous);

    ListMultimap<Token,VariantGraph.Vertex> all = matches.getAll();
    LOG.debug("all: {}", all);

    assertEquals(expected_unmatched, unmatched.size());
    assertEquals(expected_unique, unique.size());
    assertEquals(expected_ambiguous, ambiguous.size());
    //    assertEquals(expected_unique + expected_ambiguous, all.size());
  }
}
