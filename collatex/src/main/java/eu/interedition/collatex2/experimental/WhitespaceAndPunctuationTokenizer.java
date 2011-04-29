package eu.interedition.collatex2.experimental;

import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.collect.Lists;

import eu.interedition.collatex2.implementation.input.Token;
import eu.interedition.collatex2.interfaces.IToken;
import eu.interedition.collatex2.interfaces.ITokenizer;

public class WhitespaceAndPunctuationTokenizer implements ITokenizer {

  @Override
  public Iterable<IToken> tokenize(String content) {
    //TODO: move compiled pattern out of here!
    // (.+)(\\s+)(.+)
    // (.*)(\\p{Punct}+)(.*)
    Pattern pattern = Pattern.compile("(.*)([\\s+||\\p{Punct}]+)(.*)");
    List<IToken> tokens = Lists.newArrayList(); 
    Matcher matcher = pattern.matcher(content);
    while (matcher.find()) {
      content = matcher.group(1);
      String whitespaceOrPunctuation = matcher.group(2);
      String trail = matcher.group(3);
      //check whitespaceOrPunctuation;
      if (!trail.isEmpty()) {
        tokens.add(0, new Token(trail));
      }
      if (!whitespaceOrPunctuation.trim().isEmpty()) {
        tokens.add(0, new Token(whitespaceOrPunctuation));
      }
      matcher = pattern.matcher(content);
    }
    tokens.add(0, new Token(content));
    // System.out.println(tokens);
 
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
