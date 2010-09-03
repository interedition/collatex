package eu.interedition.collatex2.implementation.tokenization;

import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;

import eu.interedition.collatex2.input.NormalizedToken;
import eu.interedition.collatex2.interfaces.INormalizedToken;
import eu.interedition.collatex2.interfaces.IToken;
import eu.interedition.collatex2.interfaces.ITokenNormalizer;

/**
 * Default implementation of a token normalizer.
 * 
 * <p>Lowercases the token and strips punctuation</p>
 *
 */
public class DefaultTokenNormalizer implements ITokenNormalizer {
  private final static Pattern PUNCT = Pattern.compile("\\p{Punct}");

  @Override
  public INormalizedToken apply(IToken token) {
    String normalized = PUNCT.matcher(token.getContent().toLowerCase()).replaceAll("");
    // but if the token content was only punctuation, we end up with nothing, so:
    if (StringUtils.isEmpty(normalized)) {
      normalized = token.getContent();
    }
    return new NormalizedToken(token.getSigil(), token.getContent(), token.getPosition(), normalized);
  }
}
