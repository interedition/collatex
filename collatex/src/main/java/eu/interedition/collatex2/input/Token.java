package eu.interedition.collatex2.input;

import eu.interedition.collatex2.interfaces.IToken;

public class Token implements IToken {
  private String sigil;
  private String content;
  private int position;

  // private String trailingWhitespace; // TODO
  // private int characterPosition; // TODO

  public Token() {
  }

  public Token(final String sigil, final String content, final int position) {
    this.sigil = sigil;
    this.content = content;
    this.position = position;
  }

  public String getSigil() {
    return sigil;
  }

  public void setSigil(String sigil) {
    this.sigil = sigil;
  }

  public String getContent() {
    return content;
  }

  public void setContent(String content) {
    this.content = content;
  }

  public int getPosition() {
    return position;
  }

  public void setPosition(int position) {
    this.position = position;
  }

  @Override
  public boolean equals(final Object obj) {
    if ((obj != null) && (obj instanceof Token)) {
      final Token token = (Token) obj;
      return sigil.equals(token.sigil) && content.equals(token.content) && (position == token.position);

    }
    return super.equals(obj);
  }

  @Override
  public int hashCode() {
    int hc = 17;
    hc = hc * 59 + sigil.hashCode();
    hc = hc * 59 + content.hashCode();
    hc = hc * 59 + position;
    return hc;
  }

}
