package eu.interedition.collatex.implementation.matching;

import static org.junit.Assert.*;

import java.util.Set;

import com.google.common.collect.ListMultimap;

import eu.interedition.collatex.AbstractTest;
import eu.interedition.collatex.implementation.graph.VariantGraph;
import eu.interedition.collatex.implementation.graph.VariantGraphVertex;
import eu.interedition.collatex.interfaces.Token;
import eu.interedition.collatex.interfaces.IWitness;

import org.junit.Test;

public class MatchesTest extends AbstractTest {

  @Test
  public void test1() {
    final IWitness[] w = createWitnesses("john and paul and george and ringo", "john and paul and george and ringo");
    final VariantGraph graph = merge(w[0]);
    final Matches matches = Matches.between(graph.vertices(), w[1].getTokens(), new EqualityTokenComparator());

    int expected_unmatched = 0;
    int expected_unique = 4; // john paul george ringo
    int expected_ambiguous = 3; // 3 ands in 2nd witness
    assertMatches(matches, expected_unmatched, expected_unique, expected_ambiguous);
  }

  @Test
  public void test2() {
    final IWitness[] w = createWitnesses("the white cat", "the black cat");
    final VariantGraph graph = merge(w[0]);
    final Matches matches = Matches.between(graph.vertices(), w[1].getTokens(), new EqualityTokenComparator());

    int expected_unmatched = 1; // black
    int expected_unique = 2; // the & cat
    int expected_ambiguous = 0;
    assertMatches(matches, expected_unmatched, expected_unique, expected_ambiguous);
  }

  private void assertMatches(final Matches matches, int expected_unmatched, int expected_unique, int expected_ambiguous) {
    Set<Token> unmatched = matches.getUnmatched();
    LOG.info("unmatched: {}", unmatched);

    Set<Token> unique = matches.getUnique();
    LOG.info("unique: {}", unique);

    Set<Token> ambiguous = matches.getAmbiguous();
    LOG.info("ambiguous: {}", ambiguous);

    ListMultimap<Token,VariantGraphVertex> all = matches.getAll();
    LOG.info("all: {}", all);

    assertEquals(expected_unmatched, unmatched.size());
    assertEquals(expected_unique, unique.size());
    assertEquals(expected_ambiguous, ambiguous.size());
    //    assertEquals(expected_unique + expected_ambiguous, all.size());
  }
}
