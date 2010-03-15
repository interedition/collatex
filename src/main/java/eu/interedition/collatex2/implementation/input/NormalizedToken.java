package eu.interedition.collatex2.implementation.input;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import eu.interedition.collatex2.interfaces.INormalizedToken;

public class NormalizedToken extends Token implements INormalizedToken {
  private final String normalized;
  private final static Pattern PUNCT = Pattern.compile("\\p{Punct}");

  public NormalizedToken(final String sigil, final String content, final int position, final String normalized) {
    super(sigil, content, position);
    this.normalized = normalized;
  }

  public String getNormalized() {
    return normalized;
  }

  public static INormalizedToken normalize(final Token token) {
    final String content = token.getContent();
    String normalized = content.toLowerCase();
    final Matcher matcher = PUNCT.matcher(normalized);
    final boolean find = matcher.find();
    if (find) {
      normalized = matcher.replaceAll("");
    }
    final NormalizedToken normalizedT = create(token, normalized);
    return normalizedT;
  }

  public static NormalizedToken create(final Token token, final String normalized) {
    return new NormalizedToken(token.getSigil(), token.getContent(), token.getPosition(), normalized);
  }

}
