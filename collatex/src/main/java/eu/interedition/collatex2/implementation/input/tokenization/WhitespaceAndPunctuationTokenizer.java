package eu.interedition.collatex2.implementation.input.tokenization;

import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import com.google.common.collect.Lists;

import eu.interedition.collatex2.implementation.input.Token;
import eu.interedition.collatex2.interfaces.IToken;
import eu.interedition.collatex2.interfaces.ITokenizer;

public class WhitespaceAndPunctuationTokenizer implements ITokenizer {

  @Override
  public Iterable<IToken> tokenize(String content) {
    List<IToken> tokens = Lists.newArrayList(); 
    StringTokenizer tokenizer = new StringTokenizer(content, " ,.-()?;:\n", true);
    Token previous = null;
    while (tokenizer.hasMoreTokens()) {
      String trail = tokenizer.nextToken();
      //check whether token is whitespace or punctuation or actual content;
      if (!trail.trim().isEmpty()) {
        final Token token = new Token(trail);
        tokens.add(token);
        previous = token;
      } else {
        if (previous != null) {
          previous.setTrailingWhitespace(trail);
        }
      }
    }
 
    final Iterator<IToken> tokenIterator = tokens.iterator();
    return new Iterable<IToken>() {

      @Override
      public Iterator<IToken> iterator() {
        return new Iterator<IToken>() {

          @Override
          public void remove() {
            throw new UnsupportedOperationException();
          }

          @Override
          public IToken next() {
            return tokenIterator.next();
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
