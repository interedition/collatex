package eu.interedition.collatex2.experimental;

import eu.interedition.collatex2.AbstractTest;
import eu.interedition.collatex2.interfaces.IAlignmentTable;
import eu.interedition.collatex2.interfaces.IWitness;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class TranspositionTest extends AbstractTest {
  @Test
  public void noTransposition() {
    assertEquals(0, merge("no transposition", "no transposition").getTransposedTokens().size());
    assertEquals(0, merge("a b", "c a").getTransposedTokens().size());
  }

  @Test
  public void oneTransposition() {
    assertEquals(1, merge("a b", "b a").getTransposedTokens().size());
  }

  @Test
  public void multipleTranspositions() {
    assertEquals(2, merge("a b c", "b c a").getTransposedTokens().size());
  }

  @Test
  public void transposition1() {
    final IWitness[] w = createWitnesses(//
            "the white and black cat", "The black cat",//
            "the black and white cat", "the black and green cat");
    final IAlignmentTable table = align(w);

    assertEquals("A: |the|white|and|black|cat|", table.getRow(w[0]).toString());
    assertEquals("B: |The| | |black|cat|", table.getRow(w[1]).toString());
    assertEquals("C: |the|black|and|white|cat|", table.getRow(w[2]).toString());
    assertEquals("D: |the|black|and|green|cat|", table.getRow(w[3]).toString());
  }

  @Test
  public void transposition2() {
    final IWitness[] w = createWitnesses("He was agast, so", "He was agast", "So he was agast");
    final IAlignmentTable table = align(w);

    assertEquals("A: | |He|was|agast,|so|", table.getRow(w[0]).toString());
    assertEquals("B: | |He|was|agast| |", table.getRow(w[1]).toString());
    assertEquals("C: |So|he|was|agast| |", table.getRow(w[2]).toString());
  }

  @Test
  public void transposition2Reordered() {
    final IWitness[] w = createWitnesses("So he was agast", "He was agast", "He was agast, so");
    final IAlignmentTable table = align(w);

    // TODO: it would be nice if He was agast stayed in one place!
    assertEquals("A: | | | |So|he|was|agast|", table.getRow(w[0]).toString());
    assertEquals("B: | | | | |He|was|agast|", table.getRow(w[1]).toString());
    assertEquals("C: |He|was|agast,|so| | | |", table.getRow(w[2]).toString());
  }
}
