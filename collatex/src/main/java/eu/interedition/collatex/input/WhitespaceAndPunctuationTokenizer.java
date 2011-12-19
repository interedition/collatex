package eu.interedition.collatex.input;

import com.google.common.collect.Lists;
import eu.interedition.collatex.TokenNormalizer;
import eu.interedition.collatex.Tokenizer;
import eu.interedition.collatex.Witness;
import eu.interedition.collatex.Token;

import java.util.List;
import java.util.StringTokenizer;

public class WhitespaceAndPunctuationTokenizer implements Tokenizer {
  private TokenNormalizer tokenNormalizer = new DefaultTokenNormalizer();

  @Override
  public List<Token> tokenize(Witness witness, String content) {
    List<Token> tokens = Lists.newArrayList();
    StringTokenizer tokenizer = new StringTokenizer(content, " ,.-()?;:\n", true);
    SimpleToken previous = null;
    while (tokenizer.hasMoreTokens()) {
      String trail = tokenizer.nextToken();
      //check whether token is whitespace or punctuation or actual content;
      if (!trail.trim().isEmpty()) {
        final SimpleToken token = new SimpleToken(witness, tokens.size(), trail, tokenNormalizer.apply(trail));
        tokens.add(token);
        previous = token;
      } else {
        if (previous != null) {
          previous.setTrailingWhitespace(trail);
        }
      }
    }
 
    return tokens;
  }
}
