package eu.interedition.collatex.implementation.input;

import eu.interedition.collatex.interfaces.Token;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class PunctuationTokenizerTest {
  
  @Test
  public void tokenizePunctuation() {
    final List<Token> tokens = new WhitespaceAndPunctuationTokenizer().tokenize(null, "This is a sentence.");

    assertEquals(5, tokens.size());

    assertEquals("This", tokens.get(0).getContent());
    assertEquals(" ", tokens.get(0).getTrailingWhitespace());
    assertEquals("is", tokens.get(1).getContent());
    assertEquals(" ", tokens.get(1).getTrailingWhitespace());
    assertEquals("a", tokens.get(2).getContent());
    assertEquals(" ", tokens.get(2).getTrailingWhitespace());
    assertEquals("sentence", tokens.get(3).getContent());
    assertEquals("", tokens.get(3).getTrailingWhitespace());
    assertEquals(".", tokens.get(4).getContent());
    assertEquals("", tokens.get(4).getTrailingWhitespace());
  }
}
