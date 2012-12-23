package eu.interedition.collatex.dekker;

import com.google.common.collect.RowSortedTable;
import eu.interedition.collatex.AbstractTest;
import eu.interedition.collatex.Witness;
import eu.interedition.collatex.Token;
import eu.interedition.collatex.simple.SimpleWitness;
import org.junit.Test;

import java.util.Set;

import static org.junit.Assert.assertEquals;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class TranspositionTest extends AbstractTest {
  @Test
  public void noTransposition() {
    assertEquals(0, collate("no transposition", "no transposition").transpositions().size());
    assertEquals(0, collate("a b", "c a").transpositions().size());
  }

  @Test
  public void oneTransposition() {
    assertEquals(1, collate("a b", "b a").transpositions().size());
  }

  @Test
  public void multipleTranspositions() {
    assertEquals(1, collate("a b c", "b c a").transpositions().size());
  }

  @Test
  public void transposition1() {
    final SimpleWitness[] w = createWitnesses(//
            "the white and black cat", "The black cat",//
            "the black and white cat", "the black and green cat");
    final RowSortedTable<Integer, Witness, Set<Token>> table = table(collate(w));

    assertEquals("|the|white|and|black|cat|", toString(table, w[0]));
    assertEquals("|The| | |black|cat|", toString(table, w[1]));
    assertEquals("|the|black|and|white|cat|", toString(table, w[2]));
    assertEquals("|the|black|and|green|cat|", toString(table, w[3]));
  }

  @Test
  public void transposition2() {
    final SimpleWitness[] w = createWitnesses("He was agast, so", "He was agast", "So he was agast");
    final RowSortedTable<Integer, Witness, Set<Token>> table = table(collate(w));

    assertEquals("| |He|was|agast,|so|", toString(table, w[0]));
    assertEquals("| |He|was|agast| |", toString(table, w[1]));
    assertEquals("|So|he|was|agast| |", toString(table, w[2]));
  }

  @Test
  public void transposition2Reordered() {
    final SimpleWitness[] w = createWitnesses("So he was agast", "He was agast", "He was agast, so");
    final RowSortedTable<Integer, Witness, Set<Token>> table = table(collate(w));

    // TODO: it would be nice if He was agast stayed in one place!
    assertEquals("| | | |So|he|was|agast|", toString(table, w[0]));
    assertEquals("| | | | |He|was|agast|", toString(table, w[1]));
    assertEquals("|He|was|agast,|so| | | |", toString(table, w[2]));
  }
}
