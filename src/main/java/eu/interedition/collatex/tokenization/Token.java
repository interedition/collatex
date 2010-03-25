package eu.interedition.collatex.tokenization;

public class Token {
  private final String _original;
  private final String _text;
  private final String _punctuation;

  public Token(String orginal) {
    this(orginal, "", "");
  }

  public Token(String original, String text, String punctuation) {
    this._original = original;
    this._text = text;
    this._punctuation = punctuation;
  }

  public String getText() {
    return _text;
  }

  public String getPunctuation() {
    return _punctuation;
  }

  public String getOriginal() {
    return _original;
  }
}
