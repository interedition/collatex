package eu.interedition.collatex.experimental.ngrams.data;

public class Token {
  private final String content;
  private String trailingWhitespace;
  private int position;

  // private int characterPosition; // TODO
  // TODO: add sigil!

  public Token(final String content) {
    this.content = content;
  }

  public String getContent() {
    return content;
  }
}
