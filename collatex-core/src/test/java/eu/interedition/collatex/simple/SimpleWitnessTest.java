package eu.interedition.collatex.simple;

import eu.interedition.collatex.AbstractTest;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class SimpleWitnessTest extends AbstractTest {

  @Test
  public void normalize() {
    assertNormalized("Hello", "hello");
    assertNormalized("Now!", "now");
    assertNormalized("later?", "later");
    assertNormalized("#$@!?", "#$@!?");
    assertNormalized("&", "&");
  }

  private static void assertNormalized(String content, String expected) {
    assertEquals(expected, SimpleWitness.TOKEN_NORMALIZER.apply(content));
  }

}
