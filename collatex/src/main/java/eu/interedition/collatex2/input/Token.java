package eu.interedition.collatex2.input;

import eu.interedition.collatex2.interfaces.IToken;

public class Token implements IToken {
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

  @Override
  public boolean equals(final Object obj) {
    if (!(obj instanceof Token)) {
      return false;
    }
    final Token token = (Token) obj;
    final boolean result = sigil.equals(token.sigil) && content.equals(token.content) && position == token.position;
    return result;
  }

  @Override
  public int hashCode() {
    return content.hashCode();
  }

}
