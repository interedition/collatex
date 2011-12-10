package eu.interedition.collatex.implementation.alignment;

import com.google.common.collect.RowSortedTable;
import eu.interedition.collatex.AbstractTest;
import eu.interedition.collatex.interfaces.INormalizedToken;
import eu.interedition.collatex.interfaces.IWitness;
import org.junit.Test;

import java.util.SortedSet;

import static org.junit.Assert.assertEquals;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class TranspositionTest extends AbstractTest {
  @Test
  public void noTransposition() {
    assertEquals(0, merge("no transposition", "no transposition").transpositions().size());
    assertEquals(0, merge("a b", "c a").transpositions().size());
  }

  @Test
  public void oneTransposition() {
    assertEquals(1, merge("a b", "b a").transpositions().size());
  }

  @Test
  public void multipleTranspositions() {
    assertEquals(2, merge("a b c", "b c a").transpositions().size());
  }

  @Test
  public void transposition1() {
    final IWitness[] w = createWitnesses(//
            "the white and black cat", "The black cat",//
            "the black and white cat", "the black and green cat");
    final RowSortedTable<Integer, IWitness, SortedSet<INormalizedToken>> table = merge(w).toTable();

    assertEquals("|the|white|and|black|cat|", toString(table, w[0]));
    assertEquals("|The| | |black|cat|", toString(table, w[1]));
    assertEquals("|the|black|and|white|cat|", toString(table, w[2]));
    assertEquals("|the|black|and|green|cat|", toString(table, w[3]));
  }

  @Test
  public void transposition2() {
    final IWitness[] w = createWitnesses("He was agast, so", "He was agast", "So he was agast");
    final RowSortedTable<Integer, IWitness, SortedSet<INormalizedToken>> table = merge(w).toTable();

    assertEquals("| |He|was|agast,|so|", toString(table, w[0]));
    assertEquals("| |He|was|agast| |", toString(table, w[1]));
    assertEquals("|So|he|was|agast| |", toString(table, w[2]));
  }

  @Test
  public void transposition2Reordered() {
    final IWitness[] w = createWitnesses("So he was agast", "He was agast", "He was agast, so");
    final RowSortedTable<Integer, IWitness, SortedSet<INormalizedToken>> table = merge(w).toTable();

    // TODO: it would be nice if He was agast stayed in one place!
    assertEquals("| | | |So|he|was|agast|", toString(table, w[0]));
    assertEquals("| | | | |He|was|agast|", toString(table, w[1]));
    assertEquals("|He|was|agast,|so| | | |", toString(table, w[2]));
  }
}
