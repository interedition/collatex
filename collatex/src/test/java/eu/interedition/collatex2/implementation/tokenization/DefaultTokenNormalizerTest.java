package eu.interedition.collatex2.implementation.tokenization;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import eu.interedition.collatex2.implementation.input.tokenization.DefaultTokenNormalizer;

public class DefaultTokenNormalizerTest {
  @Test
  public void testNormalizer1() {
    testNormalization("Hello", "hello");
    testNormalization("Now!", "now");
    testNormalization("later?", "later");
    testNormalization("#$@!?", "#$@!?");
    testNormalization("&", "&");
  }

  private void testNormalization(String content, String expected) {
    assertEquals(expected, new DefaultTokenNormalizer().apply(new MockToken(content)));
  }

}
