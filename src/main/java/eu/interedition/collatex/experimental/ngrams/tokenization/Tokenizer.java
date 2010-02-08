package eu.interedition.collatex.experimental.ngrams.tokenization;

import org.apache.commons.collections.iterators.ArrayIterator;

import eu.interedition.collatex.experimental.ngrams.data.Token;
import eu.interedition.collatex.experimental.ngrams.data.Witness;

// Note: this tokenizer is very simplistic..
// it does not support streaming
// it does not support trailing whitespace!
public class Tokenizer {
  private final ArrayIterator arrayIterator;
  private final Witness witness2;
  private int counter;

  public Tokenizer(final Witness witness) {
    witness2 = witness;
    final String[] tokens = witness2.getWords().split(" "); // TODO: more chars!
    arrayIterator = new ArrayIterator(tokens);
    counter = 0;
  }

  public boolean hasNext() {
    return arrayIterator.hasNext();
  }

  public Token nextToken() {
    // TODO: I need an array iterator with generic support!
    final String content = (String) arrayIterator.next();
    final Token token = new Token(witness2.getSigil(), content, counter++);
    return token;
  }
}
