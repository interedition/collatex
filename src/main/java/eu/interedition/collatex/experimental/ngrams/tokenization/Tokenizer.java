package eu.interedition.collatex.experimental.ngrams.tokenization;

import org.apache.commons.collections.iterators.ArrayIterator;

import eu.interedition.collatex.experimental.ngrams.data.Token;

// Note: this tokenizer is very simplistic..
// it does not support streaming
// it does not support trailing whitespace!
public class Tokenizer {
  private final ArrayIterator arrayIterator;
  private int counter;
  private final String sigil;

  public Tokenizer(final String sigil, final String words) {
    this.sigil = sigil;
    final String[] tokens = words.split(" "); // TODO: more chars!
    arrayIterator = new ArrayIterator(tokens);
    counter = 0;
  }

  public boolean hasNext() {
    return arrayIterator.hasNext();
  }

  public Token nextToken() {
    // TODO: I need an array iterator with generic support!
    final String content = (String) arrayIterator.next();
    final Token token = new Token(sigil, content, ++counter);
    return token;
  }
}
