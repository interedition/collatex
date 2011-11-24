package eu.interedition.collatex2.experimental;

import eu.interedition.collatex2.AbstractTest;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class TranspositionTest extends AbstractTest {
  @Test
  public void noTransposition() {
    assertEquals(0, collate("no transposition", "no transposition").getTransposedTokens().size());
    assertEquals(0, collate("a b", "c a").getTransposedTokens().size());
  }

  @Test
  public void oneTransposition() {
    assertEquals(1, collate("a b", "b a").getTransposedTokens().size());
  }

  @Test
  public void multipleTranspositions() {
    assertEquals(2, collate("a b c", "b c a").getTransposedTokens().size());
  }
}
