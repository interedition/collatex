package eu.interedition.collatex.experimental.ngrams.data;

public class NormalizedToken extends Token {
  private final String normalized;

  public NormalizedToken(final String sigil, final String content, final int position, final String normalized) {
    super(sigil, content, position);
    this.normalized = normalized;
  }

  public String getNormalized() {
    return normalized;
  }

  public static NormalizedToken create(final Token token, final String normalized) {
    return new NormalizedToken(token.getSigil(), token.getContent(), token.getPosition(), normalized);
  }

}
