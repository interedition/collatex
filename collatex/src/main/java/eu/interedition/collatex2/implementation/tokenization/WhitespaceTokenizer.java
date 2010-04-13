package eu.interedition.collatex2.implementation.tokenization;

import java.util.Arrays;
import java.util.Iterator;

import eu.interedition.collatex2.implementation.input.Token;
import eu.interedition.collatex2.interfaces.IToken;
import eu.interedition.collatex2.interfaces.ITokenizer;

/**
 * A very simplistic tokenizer.
 * 
 * <p>
 * It does not support streaming it does not support trailing whitespace!
 * </p>
 */
public class WhitespaceTokenizer implements ITokenizer {

  @Override
  public Iterable<IToken> tokenize(final String sigle, String content) {
    final Iterator<String> tokenIterator = Arrays.asList(content.split("\\s+")).iterator();
    return new Iterable<IToken>() {

      @Override
      public Iterator<IToken> iterator() {
        return new Iterator<IToken>() {
          private int counter = 0;

          @Override
          public void remove() {
            throw new UnsupportedOperationException();
          }

          @Override
          public IToken next() {
            return new Token(sigle, tokenIterator.next(), ++counter);
          }

          @Override
          public boolean hasNext() {
            return tokenIterator.hasNext();
          }
        };
      }
    };
  }
}
