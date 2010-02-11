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

  public static NormalizedToken normalize(final Token token) {
    final String content = token.getContent();
    final String normalized = content.toLowerCase(); // TODO: this is far too simple!
    final NormalizedToken normalizedT = create(token, normalized);
    return normalizedT;
  }

  public static NormalizedToken create(final Token token, final String normalized) {
    return new NormalizedToken(token.getSigil(), token.getContent(), token.getPosition(), normalized);
  }

}
