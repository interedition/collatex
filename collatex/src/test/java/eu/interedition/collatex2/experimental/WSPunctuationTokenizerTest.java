package eu.interedition.collatex2.experimental;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.util.Iterator;

import org.junit.Test;

import eu.interedition.collatex2.implementation.input.tokenization.WhitespaceAndPunctuationTokenizer;
import eu.interedition.collatex2.interfaces.IToken;

public class WSPunctuationTokenizerTest {
  
  @Test
  public void testTokenizer() {
    String text = "This is a sentence.";
    WhitespaceAndPunctuationTokenizer tokenizer = new WhitespaceAndPunctuationTokenizer();
    Iterable<IToken> tokenize = tokenizer.tokenize(text);
    Iterator<IToken> iterator = tokenize.iterator();
    IToken nextToken = iterator.next();
    assertEquals("This", nextToken.getContent());
    assertEquals(" ", nextToken.getTrailingWhitespace());
    nextToken = iterator.next();
    assertEquals("is", nextToken.getContent());
    assertEquals(" ", nextToken.getTrailingWhitespace());
    nextToken = iterator.next();
    assertEquals("a", nextToken.getContent());
    assertEquals(" ", nextToken.getTrailingWhitespace());
    nextToken = iterator.next();
    assertEquals("sentence", nextToken.getContent());
    assertEquals("", nextToken.getTrailingWhitespace());
    nextToken = iterator.next();
    assertEquals(".", nextToken.getContent());
    assertEquals("", nextToken.getTrailingWhitespace());
    assertFalse(iterator.hasNext());
  }
}
