package eu.interedition.collatex2.implementation.tokenization;

import java.util.Arrays;
import java.util.Iterator;

import eu.interedition.collatex2.implementation.input.Token;

// Note: this tokenizer is very simplistic..
// it does not support streaming
// it does not support trailing whitespace!
public class Tokenizer {
  private final Iterator<String> arrayIterator;
  private int counter;
  private final String sigil;

  public Tokenizer(final String sigil, final String words) {
    this.sigil = sigil;
    arrayIterator = Arrays.asList(words.split(" ")).iterator(); // TODO more chars!
    counter = 0;
  }

  public boolean hasNext() {
    return arrayIterator.hasNext();
  }

  public Token nextToken() {
    final String content = arrayIterator.next();
    final Token token = new Token(sigil, content, ++counter);
    return token;
  }
}
