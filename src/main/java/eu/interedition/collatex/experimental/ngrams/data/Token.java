package eu.interedition.collatex.experimental.ngrams.data;

public class Token {
  private final String sigil;
  private final String content;
  private final int position;

  // private String trailingWhitespace; // TODO
  // private int characterPosition; // TODO

  public Token(final String sigil, final String content, final int position) {
    this.sigil = sigil;
    this.content = content;
    this.position = position;
  }

  public String getSigil() {
    return sigil;
  }

  public String getContent() {
    return content;
  }

  public int getPosition() {
    return position;
  }

}
