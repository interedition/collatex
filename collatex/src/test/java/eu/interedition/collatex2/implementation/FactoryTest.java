package eu.interedition.collatex2.implementation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Set;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import com.google.common.collect.Lists;

import eu.interedition.collatex2.interfaces.IWitness;

public class FactoryTest {
  private static CollateXEngine factory;

  @BeforeClass
  public static void setup() {
    factory = new CollateXEngine();
  }

  @Test
  public void testGetTokensWithMultiples() {
    final IWitness a = factory.createWitness("A", "the big black cat and the big black rat");
    final IWitness b = factory.createWitness("B", "the big black rat and the small white rat");
    final Set<String> tokensWithMultiples = CollateXEngine.getTokensWithMultiples(Lists.newArrayList(a, b));
    final String[] expectedTokens = { "the", "big", "black", "rat" };
    assertEquals(expectedTokens.length, tokensWithMultiples.size());
    for (final String expected : expectedTokens) {
      assertContains(tokensWithMultiples, expected);
    }
  }

  @Ignore
  @Test
  public void testGetPhrasesWithMultiples() {
    final IWitness a = factory.createWitness("A", "the big black cat and the big black rat");
    final IWitness b = factory.createWitness("B", "the big black rat and the small white rat");
    final Set<String> tokensWithMultiples = CollateXEngine.getPhrasesWithMultiples(a, b);
    final String[] expectedPhrases = { "the big black", "rat" };
    assertEquals(tokensWithMultiples.toString(), expectedPhrases.length, tokensWithMultiples.size());
    for (final String expected : expectedPhrases) {
      assertContains(tokensWithMultiples, expected);
    }
  }

  private void assertContains(final Set<String> stringSet, final String string) {
    assertTrue(string + " not found in " + stringSet, stringSet.contains(string));
  }

}
