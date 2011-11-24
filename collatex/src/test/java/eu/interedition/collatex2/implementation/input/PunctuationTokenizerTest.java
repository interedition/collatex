package eu.interedition.collatex2.implementation.input;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.util.Iterator;
import java.util.List;

import com.google.common.collect.Lists;
import org.junit.Test;

import eu.interedition.collatex2.implementation.input.tokenization.WhitespaceAndPunctuationTokenizer;
import eu.interedition.collatex2.interfaces.IToken;

public class PunctuationTokenizerTest {
  
  @Test
  public void tokenizePunctuation() {
    final List<IToken> tokens = Lists.newArrayList(new WhitespaceAndPunctuationTokenizer().tokenize("This is a sentence."));

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
