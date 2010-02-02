package eu.interedition.collatex.experimental.ngrams.tokenization;

import org.apache.commons.collections.iterators.ArrayIterator;

import eu.interedition.collatex.experimental.ngrams.data.Token;
import eu.interedition.collatex.experimental.ngrams.data.Witness;

// Note: this tokenizer is very simplistic..
// it does not support streaming
// it does not support trailing whitespace!
public class Tokenizer {
  private final String[] tokens; // TODO: remove!
  private final ArrayIterator arrayIterator;

  public Tokenizer(final Witness witness) {
    tokens = witness.getWords().split(" "); // TODO: more chars!
    arrayIterator = new ArrayIterator(tokens);
  }

  public boolean hasNext() {
    return arrayIterator.hasNext();
  }

  public Token nextToken() {
    // TODO: I need an array iterator with generic support!
    final String content = (String) arrayIterator.next();
    final Token token = new Token(content);
    return token;
  }
}
