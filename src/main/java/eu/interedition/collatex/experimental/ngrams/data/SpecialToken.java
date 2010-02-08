package eu.interedition.collatex.experimental.ngrams.data;

public class SpecialToken extends NormalizedToken {

  public SpecialToken(final String sigil, final String content, final int position) {
    super(sigil, content, position, "#");
  }
}
