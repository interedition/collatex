package eu.interedition.collatex.simple;

import com.google.common.collect.Lists;
import eu.interedition.collatex.AbstractTest;
import eu.interedition.collatex.Token;
import org.junit.Test;

import java.util.Iterator;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

public class SimpleWitnessTest extends AbstractTest {

  @Test
  public void normalize() {
    assertNormalized("Hello", "hello");
    assertNormalized("Now!", "now");
    assertNormalized("later?", "later");
    assertNormalized("#$@!?", "#$@!?");
    assertNormalized("&", "&");
  }

  @Test
  public void tokenizePunctuation() {
    final List<String> tokens = new WhitespaceAndPunctuationTokenizer().apply("This is a sentence.");

    assertEquals(5, tokens.size());

    assertEquals("This", tokens.get(0));
    assertEquals("is", tokens.get(1));
    assertEquals("a", tokens.get(2));
    assertEquals("sentence", tokens.get(3));
    assertEquals(".", tokens.get(4));
  }

  private static void assertNormalized(String content, String expected) {
    assertEquals(expected, SimpleWitness.TOKEN_NORMALIZER.apply(content));
  }

}
