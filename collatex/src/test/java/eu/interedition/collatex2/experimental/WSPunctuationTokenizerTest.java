package eu.interedition.collatex2.experimental;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.util.Iterator;

import org.junit.Test;

import eu.interedition.collatex2.interfaces.IToken;

public class WSPunctuationTokenizerTest {
  @Test
  public void testTokenizer() {
    String text = "This is a sentence.";
    WhitespaceAndPunctuationTokenizer tokenizer = new WhitespaceAndPunctuationTokenizer();
    Iterable<IToken> tokenize = tokenizer.tokenize(text);
    Iterator<IToken> iterator = tokenize.iterator();
    assertEquals("This", iterator.next().getContent());
    assertEquals("is", iterator.next().getContent());
    assertEquals("a", iterator.next().getContent());
    assertEquals("sentence", iterator.next().getContent());
    assertEquals(".", iterator.next().getContent());
    assertFalse(iterator.hasNext());
  }
}
