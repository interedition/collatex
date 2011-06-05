package eu.interedition.collatex2.experimental;

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
    while (tokenizer.hasMoreTokens()) {
      String trail = tokenizer.nextToken();
      //check whitespaceOrPunctuation;
      if (!trail.trim().isEmpty()) {
        tokens.add(new Token(trail));
//        System.out.println(">"+trail+"<");
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
