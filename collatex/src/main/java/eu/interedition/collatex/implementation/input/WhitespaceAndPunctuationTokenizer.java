package eu.interedition.collatex.implementation.input;

import com.google.common.collect.Lists;
import eu.interedition.collatex.interfaces.Token;
import eu.interedition.collatex.interfaces.ITokenNormalizer;
import eu.interedition.collatex.interfaces.ITokenizer;
import eu.interedition.collatex.interfaces.IWitness;

import java.util.List;
import java.util.StringTokenizer;

public class WhitespaceAndPunctuationTokenizer implements ITokenizer {
  private ITokenNormalizer tokenNormalizer = new DefaultTokenNormalizer();

  @Override
  public List<Token> tokenize(IWitness witness, String content) {
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
