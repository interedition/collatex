package eu.interedition.collatex2.implementation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import eu.interedition.collatex2.interfaces.IWitness;

public class FactoryTest {
  private Factory factory;

  @Before
  public void setup() {
    factory = new Factory();
  }

  @Test
  public void testGetTokensWithMultiples() {
    final IWitness a = factory.createWitness("A", "the big black cat and the big black rat");
    final IWitness b = factory.createWitness("B", "the big black rat and the small white rat");
    final Set<String> tokensWithMultiples = Factory.getTokensWithMultiples(a, b);
    final String[] expectedTokens = { "the", "big", "black", "rat" };
    assertEquals(expectedTokens.length, tokensWithMultiples.size());
    for (final String expected : expectedTokens) {
      assertContains(tokensWithMultiples, expected);
    }
  }

  //@Test
  public void testGetPhrasesWithMultiples() {
    final IWitness a = factory.createWitness("A", "the big black cat and the big black rat");
    final IWitness b = factory.createWitness("B", "the big black rat and the small white rat");
    final Set<String> tokensWithMultiples = Factory.getPhrasesWithMultiples(a, b);
    final String[] expectedPhrases = { "the big black", "rat" };
    assertEquals(expectedPhrases.length, tokensWithMultiples.size());
    for (final String expected : expectedPhrases) {
      assertContains(tokensWithMultiples, expected);
    }
  }

  private void assertContains(final Set<String> stringSet, final String string) {
    assertTrue(string + " not found in " + stringSet, stringSet.contains(string));

  }

}
