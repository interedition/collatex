package eu.interedition.collatex.experimental.ngrams.data;

public class Witness {
  private final String sigil;
  private final String words;

  public Witness(final String sigil, final String words) {
    this.sigil = sigil;
    this.words = words;
  }

  public String getSigil() {
    return sigil;
  }

  public String getWords() {
    return words;
  }
}
